package io.github.FinNank1ng.better_coordinate_navigator.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import io.github.FinNank1ng.better_coordinate_navigator.util.ModVersion;

public final class ModHelpCommands {

    private static final int PAGE_COUNT = 3;

    private ModHelpCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> create() {

        return Commands.literal("help")
                .executes(
                        ModHelpCommands::showHelp
                )
                .then(
                        Commands.argument(
                                "page",
                                IntegerArgumentType.integer(
                                        1,
                                        PAGE_COUNT
                                )
                        ).executes(
                                ModHelpCommands::showHelp
                        )
                );
    }

    private static int showHelp(
            CommandContext<CommandSourceStack> context
    ) {

        int page =
                context.getNodes().stream()
                        .anyMatch(
                                node ->
                                        node.getNode()
                                                .getName()
                                                .equals("page")
                        )
                        ? IntegerArgumentType.getInteger(
                        context,
                        "page"
                )
                        : 1;

        MutableComponent message =
                switch (page) {
                    case 1 -> pageOne();
                    case 2 -> pageTwo();
                    case 3 -> pageThree();
                    default -> Component.literal(
                            "§c 无效的帮助页面"
                    );
                };

        message.append(
                createNavigation(
                        page
                )
        );

        context.getSource().sendSuccess(
                () -> message,
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static MutableComponent pageOne() {

        return Component.literal(
                "§6§lBetter Coordinate Navigator\n"
                        + "§7作者: §f星丶白羽莲 §8(FinNank1ng / ShirohaRen)\n"
                        + "§7版本: §e"
                        + ModVersion.getVersion()
                        + "\n\n"

                        + "§e§l任务点管理\n\n"

                        + "§a/bcn list\n"
                        + "§7查看所有任务点\n\n"

                        + "§a/bcn marker create <pos> <name>\n"
                        + "§7创建任务点\n\n"

                        + "§a/bcn marker remove <name>\n"
                        + "§7删除任务点\n\n"

                        + "§a/bcn marker rename <old> <new>\n"
                        + "§7重命名任务点\n\n"

                        + "§a/bcn marker info <name>\n"
                        + "§7查看任务点详细信息"
        );
    }

    private static MutableComponent pageTwo() {

        return Component.literal(
                "§6§lBetter Coordinate Navigator\n\n"

                        + "§e§l自定义图标\n\n"

                        + "§a/bcn marker icon set <name> <icon>\n"
                        + "§7设置任务点自定义图标\n\n"

                        + "§a/bcn marker icon clear <name>\n"
                        + "§7清除任务点自定义图标\n\n"

                        + "§7图标文件位置\n"
                        + "§f.minecraft/better_coordinate_navigator/Picture/\n\n"

                        + "§b§l任务追踪\n\n"

                        + "§a/bcn marker track <name>\n"
                        + "§7开始追踪任务点\n\n"

                        + "§a/bcn marker track player <player> <name>\n"
                        + "§7由管理员为指定玩家设置追踪\n\n"

                        + "§a/bcn marker untrack <name>\n"
                        + "§7取消指定任务点追踪\n\n"

                        + "§a/bcn marker untrack player <player> <name>\n"
                        + "§7由管理员为指定玩家取消追踪\n\n"

                        + "§a/bcn marker cleartrack\n"
                        + "§7取消全部任务追踪"
        );
    }

    private static MutableComponent pageThree() {

        return Component.literal(
                "§6§lBetter Coordinate Navigator\n\n"

                        + "§d§l工作流\n\n"

                        + "§a/bcn workflow start <workflow>\n"
                        + "§7启动或继续自己的工作流\n\n"

                        + "§a/bcn workflow pause <workflow>\n"
                        + "§7暂停工作流并保留当前步骤\n\n"

                        + "§a/bcn workflow stop <workflow>\n"
                        + "§7停止工作流并回到 Step 0\n\n"

                        + "§a/bcn workflow reset <workflow>\n"
                        + "§7清除运行进度\n\n"

                        + "§a/bcn workflow start <workflow> <player>\n"
                        + "§7为指定玩家启动工作流\n\n"

                        + "§a/bcn workflow pause <workflow> <player>\n"
                        + "§7暂停指定玩家的工作流\n\n"

                        + "§a/bcn workflow stop <workflow> <player>\n"
                        + "§7停止指定玩家的工作流\n\n"

                        + "§a/bcn workflow reset <workflow> <player>\n"
                        + "§7重置指定玩家的工作流\n\n"

                        + "§7<workflow> 支持名称或 UUID\n"
                        + "§7名称重复时使用 UUID 区分\n"
                        + "§7工作流命令需要管理员权限"
        );
    }

    private static MutableComponent createNavigation(
            int page
    ) {

        MutableComponent navigation =
                Component.literal("\n\n");

        if (page > 1) {

            navigation.append(
                    createButton(
                            "‹ 上一页",
                            "/bcn help "
                                    + (page - 1)
                    )
            );
        }

        navigation.append(
                Component.literal(
                        "   §8[ "
                                + page
                                + " / "
                                + PAGE_COUNT
                                + " ]   "
                )
        );

        if (page < PAGE_COUNT) {

            navigation.append(
                    createButton(
                            "下一页 ›",
                            "/bcn help "
                                    + (page + 1)
                    )
            );
        }

        return navigation;
    }

    private static MutableComponent createButton(
            String text,
            String command
    ) {

        return Component.literal(
                text
        ).withStyle(
                style ->
                        style
                                .withColor(
                                        ChatFormatting.AQUA
                                )
                                .withUnderlined(
                                        true
                                )
                                .withClickEvent(
                                        new ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                command
                                        )
                                )
                                .withHoverEvent(
                                        new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                Component.literal(
                                                        "点击切换页面"
                                                )
                                        )
                                )
        );
    }
}