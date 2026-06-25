package io.github.FinNank1ng.better_coordinate_navigator.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import io.github.FinNank1ng.better_coordinate_navigator.data.QuestManager;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import io.github.FinNank1ng.better_coordinate_navigator.network.QuestSyncHelper;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("bcn")

                        .then(Commands.literal("help")
                                .executes(ModCommands::showHelp))
                        // list
                        .then(Commands.literal("list")
                                .executes(ModCommands::listMarkers))

                        // 标记点各种操作
                        .then(Commands.literal("marker")

                                // create
                                .then(Commands.literal("create")
                                        .then(
                                                Commands.argument("pos", Vec3Argument.vec3())
                                                        .then(
                                                                Commands.argument(
                                                                                "name",
                                                                                StringArgumentType.greedyString()
                                                                        )
                                                                        .executes(ModCommands::createMarker)
                                                        )
                                        )
                                )

                                // rename
                                .then(Commands.literal("rename")
                                        .then(Commands.argument("oldName", StringArgumentType.string())
                                                .then(Commands.argument("newName", StringArgumentType.greedyString())
                                                        .executes(ModCommands::renameMarker)
                                                )
                                        )
                                )

                                // remove
                                .then(Commands.literal("remove")
                                        .then(
                                                Commands.argument(
                                                                "name",
                                                                StringArgumentType.greedyString()
                                                        )
                                                        .executes(ModCommands::removeMarker)
                                        )
                                )
                                .then(Commands.literal("track")
                                        .then(
                                                Commands.argument(
                                                                "name",
                                                                StringArgumentType.greedyString()
                                                        )
                                                        .executes(ModCommands::trackMarker)
                                        )
                                )

                                // untrack
                                .then(Commands.literal("untrack")
                                        .executes(ModCommands::untrackMarker)
                                )

                                // info
                                .then(Commands.literal("info")
                                        .then(
                                                Commands.argument(
                                                                "name",
                                                                StringArgumentType.greedyString()
                                                        )
                                                        .executes(ModCommands::infoMarker)
                                        )
                                )
                        )
        );
    }

    private static QuestManager getManager(CommandContext<CommandSourceStack> context) {
        return QuestManager.get(
                () -> context.getSource()
                        .getLevel()
                        .getDataStorage()
        );
    }

    private static int showHelp(
            CommandContext<CommandSourceStack> context
    ) {

        context.getSource().sendSuccess(
                () -> Component.literal("""
                    §6§l========== Better coordinate navigator ==========
                    
                    §7作者: §f星丶白羽莲 §8(FinNank1ng / ShirohaRen)
                    
                    §e[任务点管理]
                    
                    §a/bcn list
                    §7查看所有任务点
                    
                    §a/bcn marker create <pos> <name>
                    §7创建任务点
                    
                    §a/bcn marker remove <name>
                    §7删除任务点
                    
                    §a/bcn marker rename <old> <new>
                    §7重命名任务点
                    
                    §a/bcn marker info <name>
                    §7查看任务点详细信息
                    
                    §b[任务追踪]
                    
                    §a/bcn marker track <name>
                    §7开始追踪任务点
                    
                    §a/bcn marker untrack
                    §7取消当前追踪
                    §8------------------------------------
                    
                    """),
                false
        );

        return 1;
    }

    // 列表标点逻辑
    private static int listMarkers(CommandContext<CommandSourceStack> context) {

        QuestManager manager = getManager(context);

        if (manager.getMarkers().isEmpty()) {
            context.getSource().sendSuccess(
                    () -> Component.literal("当前没有任何任务点。"),
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

            if (marker.tracked) {
                builder.append(" §a[追踪中]");
            }

            builder.append("\n");

            builder.append("  §7坐标: §b")
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
        // 新name 替换 老name
        String oldName =
                StringArgumentType.getString(context, "oldName");

        String newName =
                StringArgumentType.getString(context, "newName");

        QuestManager manager =
                getManager(context);

        boolean success =
                manager.renameMarker(oldName, newName);

        // 未找到原本有的name (老name)
        if (!success) {
            context.getSource().sendFailure(
                    Component.literal(
                            "§c 未找到任务点 [" + oldName + "]"
                    )
            );
            return 0;
        }

        // 同步服务端与客户端
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            QuestSyncHelper.syncToPlayer(player, manager);
        }

        // 有原本的name则可替换新name
        context.getSource().sendSuccess(
                () -> Component.literal(
                        """
                        §a 任务点重命名成功
                        
                        §7 旧名称: §f%s
                        §7 新名称: §a%s
                        """
                                .formatted(
                                        oldName,
                                        newName
                                )
                ),
                true
        );

        return 1;
    }

    // 追踪标记点
    private static int trackMarker(
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
                manager.getMarker(name);

        if (marker == null) {

            context.getSource().sendFailure(
                    Component.literal(
                            "§c 未找到任务点 [" + name + "]"
                    )
            );

            return 0;
        }

        manager.setTrackedMarker(name);

        if (context.getSource()
                .getEntity() instanceof ServerPlayer player) {

            QuestSyncHelper.syncToPlayer(
                    player,
                    manager
            );
        }

        context.getSource().sendSuccess(
                () -> Component.literal(
                        """
                        §a 已开始追踪
                        
                        §7 目标: §f%s
                        """
                                .formatted(name)
                ),
                true
        );

        return 1;
    }

    // 取消追踪标记点
    private static int untrackMarker(
            CommandContext<CommandSourceStack> context
    ) {

        QuestManager manager =
                getManager(context);

        manager.clearTrackedMarker();

        if (context.getSource()
                .getEntity() instanceof ServerPlayer player) {

            QuestSyncHelper.syncToPlayer(
                    player,
                    manager
            );
        }

        context.getSource().sendSuccess(
                () -> Component.literal(
                        """
                        §6 已取消任务追踪
                        """
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

        QuestManager manager =
                getManager(context);

        QuestMarker marker =
                manager.getMarker(name);

        if (marker == null) {

            context.getSource()
                    .sendFailure(
                            Component.literal(
                                    "未找到任务点 [" + name + "]"
                            )
                    );

            return 0;
        }

        String text = """
        §6§l========== Marker Info ==========
        §e名称
        §f%s
        
        §e坐标
        §b%.1f %.1f %.1f
        
        §e描述
        §7%s
        
        §e状态
        %s
        §8===================================
        """
                .formatted(
                        marker.name,
                        marker.x,
                        marker.y,
                        marker.z,
                        marker.description.isBlank()
                                ? "暂无描述"
                                : marker.description,
                        marker.tracked
                                ? "§a 追踪中"
                                : "§7 未追踪"
                );

        context.getSource()
                .sendSuccess(
                        () -> Component.literal(text),
                        false
                );

        return 1;
    }

    // 移除标点逻辑
    private static int removeMarker(CommandContext<CommandSourceStack> context) {

        String name = StringArgumentType.getString(
                context,
                "name"
        );

        QuestManager manager = getManager(context);

        boolean removed = manager.removeMarker(name);

        if (removed) {
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
                            §a 已删除任务点
                            
                            §7 名称: §f%s
                            """
                                    .formatted(name)
                    ),
                    true
            );

        } else {

            context.getSource().sendFailure(
                    Component.literal(
                            "§c 未找到任务点 [" + name + "]"
                    )
            );
        }

        return removed ? 1 : 0;
    }
}