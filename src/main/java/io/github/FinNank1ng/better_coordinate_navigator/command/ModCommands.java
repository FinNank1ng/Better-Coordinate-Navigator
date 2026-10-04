package io.github.FinNank1ng.better_coordinate_navigator.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestManager;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import io.github.FinNank1ng.better_coordinate_navigator.network.OpenBCNMainScreenPacket;
import io.github.FinNank1ng.better_coordinate_navigator.network.QuestSyncHelper;
import io.github.FinNank1ng.better_coordinate_navigator.network.ModPackets;

import net.minecraft.network.chat.ClickEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ModCommands {

    public static final String VERSION = "${file.jarVersion}";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("bcn")
                        .executes(ModCommands::openMainScreen)

                        .then(
                                ModHelpCommands.create()
                        )
                        // list
                        .then(Commands.literal("list")
                                .executes(ModCommands::listMarkers))

                        // 标记点各种操作
                        .then(Commands.literal("marker")

                                // create
                                .then(Commands.literal("create")
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .then(Commands.argument("name", StringArgumentType.string())
                                                        .executes(ModCommands::createMarker)
                                                )
                                        )
                                )

                                // rename
                                .then(Commands.literal("rename")
                                        .then(Commands.argument("oldName", StringArgumentType.string())
                                                .then(Commands.argument("newName", StringArgumentType.string())
                                                        .executes(ModCommands::renameMarker)
                                                )
                                        )
                                )

                                // remove
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("name", StringArgumentType.string())
                                                .executes(ModCommands::removeMarker)
                                        )
                                )
                                // track 标点
                                .then(Commands.literal("track")

                                        /*
                                         * 玩家自己追踪
                                         */
                                        .then(Commands.argument("name", StringArgumentType.string())
                                                .executes(ModCommands::trackMarker
                                                )
                                        )

                                        /*
                                         * 管理员指定玩家追踪
                                         */
                                        .then(Commands.literal("player").requires(source -> source.hasPermission(2)
                                                ).then(Commands.argument("player", EntityArgument.players())
                                                        .then(Commands.argument("name", StringArgumentType.string())
                                                                .executes(ModCommands::trackPlayerMarker
                                                                )
                                                        )
                                                )
                                        )
                                )
                                // untrack
                                .then(Commands.literal("untrack")

                                        /*
                                        /* 玩家自己取消追踪
                                         */
                                        .then(Commands.argument("name", StringArgumentType.string())
                                                .executes(ModCommands::untrackMarker)
                                        )

                                        /*
                                        /* 管理员指定玩家取消追踪
                                         */
                                        .then(Commands.literal("player")
                                                .then(Commands.argument("player", EntityArgument.players())
                                                        .then(Commands.argument("name", StringArgumentType.string())
                                                                .executes(ModCommands::untrackPlayerMarker)
                                                        )
                                                )
                                        )
                                )

                                // clear 所有track的标点
                                .then(Commands.literal("cleartrack")
                                        .executes(ModCommands::clearTrackedMarkers)
                                )
                                // icon
                                .then(Commands.literal("icon")
                                        // 设置图标
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("name", StringArgumentType.string())
                                                        .then(Commands.argument("icon", StringArgumentType.string())
                                                                .executes(
                                                                        ModCommands::setMarkerIcon
                                                                )
                                                        )
                                                )
                                        )

                                        // 清除图标
                                        .then(Commands.literal("clear").then(
                                                        Commands.argument("name", StringArgumentType.string())
                                                                .executes(
                                                                        ModCommands::clearMarkerIcon
                                                                )
                                                )
                                        )
                                )
                                // info
                                .then(Commands.literal("info")
                                        .then(Commands.argument("name", StringArgumentType.string())
                                                .executes(ModCommands::infoMarker)
                                        )
                                )
                        )

                        .then(
                                ModWorkflowCommands.create()
                        )

        );
    }

    private static QuestManager getManager(CommandContext<CommandSourceStack> context) {
        return QuestManager.get(context.getSource().getLevel());
    }

    /**
     * 根据用户输入解析任务点
     */
    private static QuestMarker resolveMarker(
            CommandContext<CommandSourceStack> context,
            String input
    ) {

        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        QuestManager manager = getManager(context);

        String value = input.trim();

        /*
         * 1. 优先尝试 UUID
         */
        try {

            UUID markerId = UUID.fromString(value);

            QuestMarker marker = manager.getMarker(markerId);

            if (marker != null) {
                return marker;
            }

        } catch (IllegalArgumentException ignored) {
            /*
             * 不是 UUID，继续按名称处理
             */
        }

        /*
         * 2. 按名称查找
         */
        List<QuestMarker> matches =
                manager.findMarkers(value);

        /*
         * 没找到
         */
        if (matches.isEmpty()) {

            context.getSource().sendFailure(
                    Component.literal(
                            "§c 未找到任务点 [" + value + "]"
                    )
            );

            return null;
        }

        /*
         * 名称唯一
         */
        if (matches.size() == 1) {
            return matches.get(0);
        }

        /*
         * 名称重复
         */
        context.getSource().sendFailure(
                Component.literal(
                        "§c 找到 " + matches.size()
                                + " 个同名任务点：[" + value + "]"
                )
        );

        for (QuestMarker marker : matches) {

            Component uuidComponent =
                    Component.literal(
                            marker.getId().toString()
                    ).withStyle(
                            style -> style
                                    .withUnderlined(true)
                                    .withClickEvent(
                                            new ClickEvent(
                                                    ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                    marker.getId().toString()
                                            )
                                    )
                    );

            Component line =
                    Component.literal(
                            "§7◆ "
                                    + marker.name
                                    + " §8| §7坐标: §b"
                                    + String.format(
                                    "%.1f %.1f %.1f",
                                    marker.x,
                                    marker.y,
                                    marker.z
                            )
                                    + " §8| §eUUID: §f"
                    ).append(uuidComponent);

            context.getSource().sendFailure(line);
        }

        context.getSource().sendFailure(
                Component.literal(
                        "§7UUID 可直接替代名称用于任务点操作。"
                )
        );

        return null;
    }

    // 列表标点逻辑
    private static int listMarkers(CommandContext<CommandSourceStack> context) {

        QuestManager manager = getManager(context);

        if (manager.getMarkers().isEmpty()) {
            context.getSource().sendSuccess(
                    () -> Component.literal("§7当前没有任何任务点。"),
                    false
            );
            return 0;
        }

        StringBuilder builder = new StringBuilder();

        builder.append("""
                §6§l Marker List
                """);

        builder.append("§e当前任务点数量: §a")
                .append(manager.getMarkers().size())
                .append("\n\n");

        for (QuestMarker marker : manager.getMarkers()) {

            builder.append("§e◆ §f")
                    .append(marker.name);

            boolean tracked = false;

            if(context.getSource().getEntity() instanceof ServerPlayer player){

                tracked = manager.isPlayerTracking(
                        player.getUUID(),
                        marker.getId()
                );

            }

            if(tracked){

                builder.append(" §a[追踪中]");

            }

            builder.append("\n");

            builder.append("§7坐标: §b")
                    .append(String.format(
                            "%.1f %.1f %.1f",
                            marker.x,
                            marker.y,
                            marker.z
                    ))
                    .append("\n");
        }

        context.getSource().sendSuccess(
                () -> Component.literal(builder.toString()),
                false
        );

        return 1;
    }

    private static int openMainScreen(CommandContext<CommandSourceStack> context

    ) throws CommandSyntaxException {

        ServerPlayer player = context.getSource().getPlayerOrException();

        ModPackets.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new OpenBCNMainScreenPacket()
        );

        return 1;
    }

    // 创建标点逻辑
    private static int createMarker(CommandContext<CommandSourceStack> context) {

        Vec3 pos = Vec3Argument.getVec3(context, "pos");

        String name = StringArgumentType.getString(
                context,
                "name"
        );

        QuestManager manager = getManager(context);

        QuestMarker marker = new QuestMarker(
                pos.x,
                pos.y,
                pos.z,
                name
        );

        manager.addMarker(marker);
        // 同步服务端与客户端
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            QuestSyncHelper.syncToPlayer(
                    player,
                    manager
            );
        }

        context.getSource().sendSuccess(
                () -> Component.literal(
                        String.format(
                                """     
                                §a 任务点创建成功
                                §7 名称: §f%s
                                §7 坐标: §b%.1f %.1f %.1f
                                """,
                                name,
                                pos.x,
                                pos.y,
                                pos.z
                        )
                ),
                true
        );

        return 1;
    }

    // 重命名逻辑
    private static int renameMarker(
            CommandContext<CommandSourceStack> context
    ) {

        String oldName =
                StringArgumentType.getString(
                        context,
                        "oldName"
                );

        String newName =
                StringArgumentType.getString(
                        context,
                        "newName"
                );

        QuestMarker marker =
                resolveMarker(
                        context,
                        oldName
                );

        if (marker == null) {
            return 0;
        }

        QuestManager manager =
                getManager(context);

        boolean success =
                manager.renameMarker(
                        marker.getId(),
                        newName
                );

        if (!success) {
            context.getSource().sendFailure(
                    Component.literal(
                            "§c 任务点重命名失败: [" + oldName + "]"
                    )
            );

            return 0;
        }

        // 同步服务端与客户端
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            QuestSyncHelper.syncToPlayer(
                    player,
                    manager
            );
        }

        context.getSource().sendSuccess(
                () -> Component.literal(
                        """
                        §a 任务点重命名成功
                        §7 旧名称: §f%s
                        §7 新名称: §a%s
                        §7 UUID: §8%s
                        """.formatted(
                                oldName,
                                newName,
                                marker.getId()
                        )
                ),
                true
        );

        return 1;
    }

    // 追踪标记点
    private static int trackMarker(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer player =
                context.getSource()
                        .getPlayerOrException();

        String name =
                StringArgumentType.getString(
                        context,
                        "name"
                );

        QuestMarker marker =
                resolveMarker(
                        context,
                        name
                );

        if (marker == null) {
            return 0;
        }

        QuestManager manager = getManager(context);

        boolean success =
                manager.trackPlayerMarker(
                        player.getUUID(),
                        marker.getId()
                );

        if (!success) {

            context.getSource().sendFailure(
                    Component.literal(
                            "§c 无法追踪任务点 [" + marker.name + "]"
                    )
            );

            return 0;
        }

        QuestSyncHelper.syncToPlayer(
                player,
                manager
        );

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "§a 已追踪: [" + marker.name + "]"
                                + " §8(UUID: "
                                + marker.getId()
                                + ")"
                ),
                false
        );

        return 1;
    }

    // 取消追踪某个标记点
    private static int untrackMarker(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer player =
                context.getSource()
                        .getPlayerOrException();

        String name =
                StringArgumentType.getString(
                        context,
                        "name"
                );

        QuestMarker marker =
                resolveMarker(
                        context,
                        name
                );

        if (marker == null) {
            return 0;
        }

        QuestManager manager = getManager(context);

        boolean success =
                manager.untrackPlayerMarker(
                        player.getUUID(),
                        marker.getId()
                );

        if (!success) {

            context.getSource().sendFailure(
                    Component.literal(
                            "§c 未追踪任务点 [" + marker.name + "]"
                    )
            );

            return 0;
        }

        QuestSyncHelper.syncToPlayer(
                player,
                manager
        );

        context.getSource().sendSuccess(
                () -> Component.literal(
                        """
                        §6 已取消追踪
                        §7目标: §f%s
                        §7UUID: §8%s
                        """.formatted(
                                marker.name,
                                marker.getId()
                        )
                ),
                false
        );

        return 1;
    }

    // 清除自己的全部追踪
    private static int clearTrackedMarkers(
            CommandContext<CommandSourceStack> context
    )
            throws CommandSyntaxException {

        ServerPlayer player =
                context.getSource()
                        .getPlayerOrException();

        QuestManager manager = getManager(context);

        manager.clearPlayerTrackers(
                player.getUUID()
        );

        QuestSyncHelper.syncToPlayer(
                player,
                manager
        );

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "§6 已清除全部任务追踪"
                ),
                false
        );

        return 1;
    }

    private static int trackPlayerMarker(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        Collection<ServerPlayer> players =
                EntityArgument.getPlayers(
                        context,
                        "player"
                );

        String playerNames =
                players.stream()
                        .map(player -> player.getName().getString())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");

        String name =
                StringArgumentType.getString(
                        context,
                        "name"
                );

        QuestMarker marker =
                resolveMarker(
                        context,
                        name
                );

        if (marker == null) {
            return 0;
        }

        QuestManager manager = getManager(context);

        for (ServerPlayer player : players) {

            manager.trackPlayerMarker(
                    player.getUUID(),
                    marker.getId()
            );

            QuestSyncHelper.syncToPlayer(
                    player,
                    manager
            );
        }

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "§a已设置玩家§d["+playerNames+"] §a追踪: §b["+marker.name+"]"
                ),
                true
        );

        return 1;
    }


    private static int untrackPlayerMarker(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        Collection<ServerPlayer> players =
                EntityArgument.getPlayers(
                        context,
                        "player"
                );

        String playerNames =
                players.stream()
                        .map(player -> player.getName().getString())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");

        String name =
                StringArgumentType.getString(
                        context,
                        "name"
                );

        QuestMarker marker =
                resolveMarker(
                        context,
                        name
                );

        if (marker == null) {
            return 0;
        }

        QuestManager manager = getManager(context);

        for (ServerPlayer player : players) {

            manager.untrackPlayerMarker(
                    player.getUUID(),
                    marker.getId()
            );

            QuestSyncHelper.syncToPlayer(
                    player,
                    manager
            );
        }

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "§6 已取消玩家 §d["
                                + playerNames
                                + "] §6追踪: §b["
                                + marker.name
                                + "]"
                ),
                true
        );

        return 1;
    }

    // 设置对HUD标点的自定义图像
    private static int setMarkerIcon(
            CommandContext<CommandSourceStack> context
    ){

        String name = StringArgumentType.getString(
                context,
                "name"
        );

        String icon = StringArgumentType.getString(
                context,
                "icon"
        );

        ServerPlayer player = context.getSource().getPlayer();

        QuestManager manager = QuestManager.get(player.serverLevel());

        QuestMarker marker = resolveMarker(
                context, name
        );

        if(marker == null){

            context.getSource().sendFailure(
                    Component.literal(
                            "§c 不存在的标点: "
                                    + name
                    )
            );

            return 0;
        }

        marker.iconName = icon;

        manager.setDirty();

        QuestSyncHelper.syncToPlayer(
                player,
                manager
        );

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "§6 标点 §b["+name+"] §6自定义图标修改为 §b["+icon+"] "
                ),
                true
        );

        return 1;
    }

    private static int clearMarkerIcon(
            CommandContext<CommandSourceStack> context
    ){

        String name = StringArgumentType.getString(
                context,
                "name"
        );

        ServerPlayer player = context.getSource().getPlayer();

        QuestManager manager = QuestManager.get(player.serverLevel());

        QuestMarker marker = resolveMarker(
                context,
                name
        );

        if(marker == null){

            context.getSource().sendFailure(
                    Component.literal(
                            "§c 不存在的标点: "
                                    + name
                    )
            );

            return 0;
        }

        marker.iconName = "";

        manager.setDirty();

        QuestSyncHelper.syncToPlayer(
                player,
                manager
        );

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "§7 标点 §b["+name+"] §7已恢复默认图标"
                ),
                true
        );

        return 1;
    }

    // 标记点信息
    private static int infoMarker(
            CommandContext<CommandSourceStack> context
    ) {

        String name =
                StringArgumentType.getString(
                        context,
                        "name"
                );

        QuestManager manager = getManager(context);

        QuestMarker marker = resolveMarker(
                context, name
        );

        if (marker == null) {
            return 0;
        }

        /*
         * 当前玩家追踪状态
         */
        boolean tracked = false;

        if (context.getSource().getEntity()
                instanceof ServerPlayer player) {

            tracked =
                    manager.isPlayerTracking(
                            player.getUUID(),
                            marker.getId()
                    );
        }

        String text = """
            §6§l Marker Info
            §e名称: §f%s
            §eUUID: §8%s
            §e坐标: §b%.1f %.1f %.1f
            §e描述: §7%s
            §e图标: %s
            §e状态: %s
            """
                .formatted(
                        marker.name,
                        marker.getId(),
                        marker.x,
                        marker.y,
                        marker.z,
                        marker.description == null
                                || marker.description.isEmpty()
                                ? "无"
                                : marker.description,
                        marker.iconName == null
                                || marker.iconName.isEmpty()
                                ? "§7默认 ◆"
                                : "§a" + marker.iconName,
                        tracked
                                ? "§a正在追踪"
                                : "§7未追踪"
                );

        context.getSource()
                .sendSuccess(
                        () -> Component.literal(text),
                        false
                );

        return 1;
    }

    // 移除标点逻辑
    private static int removeMarker(
            CommandContext<CommandSourceStack> context
    ) {

        String name =
                StringArgumentType.getString(
                        context,
                        "name"
                );

        QuestManager manager =
                getManager(context);

        QuestMarker marker =
                resolveMarker(
                        context,
                        name
                );

        if (marker == null) {
            return 0;
        }

        boolean removed =
                manager.removeMarker(
                        marker.getId()
                );

        if (removed) {

            // 同步服务端与客户端
            if (context.getSource().getEntity()
                    instanceof ServerPlayer player) {

                QuestSyncHelper.syncToPlayer(
                        player,
                        manager
                );
            }

            context.getSource().sendSuccess(
                    () -> Component.literal(
                            """
                            §a 已删除标点
                            §7 名称: §f%s
                            §7 UUID: §8%s
                            """.formatted(
                                    marker.name,
                                    marker.getId()
                            )
                    ),
                    true
            );

        } else {

            context.getSource().sendFailure(
                    Component.literal(
                            "§c 删除标点失败 [" + marker.name + "]"
                    )
            );
        }

        return removed ? 1 : 0;
    }
}
