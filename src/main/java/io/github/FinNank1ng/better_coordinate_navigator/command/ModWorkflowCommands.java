package io.github.FinNank1ng.better_coordinate_navigator.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.FinNank1ng.better_coordinate_navigator.workflow.Workflow;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowManager;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowRuntime;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public final class ModWorkflowCommands {

    private ModWorkflowCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> create() {

        return Commands.literal("workflow")
                .requires(
                        source ->
                                source.hasPermission(2)
                )

                .then(
                        createStartCommand()
                )

                .then(
                        createPauseCommand()
                )

                .then(
                        createStopCommand()
                )

                .then(
                        createResetCommand()
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createStartCommand() {

        return Commands.literal("start")
                .then(
                        Commands.argument(
                                        "workflow",
                                        StringArgumentType.string()
                                )
                                .executes(
                                        ModWorkflowCommands::startWorkflow
                                )
                                .then(
                                        Commands.argument(
                                                        "player",
                                                        EntityArgument.player()
                                                )
                                                .executes(
                                                        ModWorkflowCommands::startWorkflowForPlayer
                                                )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createPauseCommand() {

        return Commands.literal("pause")
                .then(
                        Commands.argument(
                                        "workflow",
                                        StringArgumentType.string()
                                )
                                .executes(
                                        ModWorkflowCommands::pauseWorkflow
                                )
                                .then(
                                        Commands.argument(
                                                        "player",
                                                        EntityArgument.player()
                                                )
                                                .executes(
                                                        ModWorkflowCommands::pauseWorkflowForPlayer
                                                )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createStopCommand() {

        return Commands.literal("stop")
                .then(
                        Commands.argument(
                                        "workflow",
                                        StringArgumentType.string()
                                )
                                .executes(
                                        ModWorkflowCommands::stopWorkflow
                                )
                                .then(
                                        Commands.argument(
                                                        "player",
                                                        EntityArgument.player()
                                                )
                                                .executes(
                                                        ModWorkflowCommands::stopWorkflowForPlayer
                                                )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createResetCommand() {

        return Commands.literal("reset")
                .then(
                        Commands.argument(
                                        "workflow",
                                        StringArgumentType.string()
                                )
                                .executes(
                                        ModWorkflowCommands::resetWorkflow
                                )
                                .then(
                                        Commands.argument(
                                                        "player",
                                                        EntityArgument.player()
                                                )
                                                .executes(
                                                        ModWorkflowCommands::resetWorkflowForPlayer
                                                )
                                )
                );
    }


    private static int startWorkflow(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer player =
                context.getSource()
                        .getPlayerOrException();

        Workflow workflow =
                resolveWorkflow(
                        context,
                        StringArgumentType.getString(
                                context,
                                "workflow"
                        )
                );

        if (workflow == null) {
            return 0;
        }

        return startWorkflowForTarget(
                context,
                player,
                workflow
        );
    }

    private static int startWorkflowForPlayer(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer player =
                EntityArgument.getPlayer(
                        context,
                        "player"
                );

        Workflow workflow =
                resolveWorkflow(
                        context,
                        StringArgumentType.getString(
                                context,
                                "workflow"
                        )
                );

        if (workflow == null) {
            return 0;
        }

        return startWorkflowForTarget(
                context,
                player,
                workflow
        );
    }

    private static int startWorkflowForTarget(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player,
            Workflow workflow
    ) {

        WorkflowManager manager =
                WorkflowRuntime.getManager();

        boolean success =
                manager.startWorkflow(
                        player,
                        workflow.getId()
                );

        if (!success) {

            WorkflowManager.WorkflowProgress progress =
                    manager.getProgress(
                            player,
                            workflow.getId()
                    );

            String reason;

            if (workflow.isEmpty()) {

                reason = "工作流没有任何 Step";

            } else if (progress != null
                    && progress.isRunning()) {

                reason = "工作流已经在运行";

            } else if (progress != null
                    && progress.isCompleted()) {

                reason = "工作流已经完成，请先 reset";

            } else {

                reason = "未知原因";
            }

            sendFailure(
                    context,
                    "工作流启动失败",
                    workflow,
                    player,
                    reason
            );

            return 0;
        }

        sendSuccess(
                context,
                "工作流启动成功",
                workflow,
                player
        );

        return Command.SINGLE_SUCCESS;
    }


    private static int pauseWorkflow(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer player =
                context.getSource()
                        .getPlayerOrException();

        Workflow workflow =
                resolveWorkflow(
                        context,
                        StringArgumentType.getString(
                                context,
                                "workflow"
                        )
                );

        if (workflow == null) {
            return 0;
        }

        return pauseWorkflowForTarget(
                context,
                player,
                workflow
        );
    }

    private static int pauseWorkflowForPlayer(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer player =
                EntityArgument.getPlayer(
                        context,
                        "player"
                );

        Workflow workflow =
                resolveWorkflow(
                        context,
                        StringArgumentType.getString(
                                context,
                                "workflow"
                        )
                );

        if (workflow == null) {
            return 0;
        }

        return pauseWorkflowForTarget(
                context,
                player,
                workflow
        );
    }

    private static int pauseWorkflowForTarget(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player,
            Workflow workflow
    ) {

        WorkflowManager manager =
                WorkflowRuntime.getManager();

        boolean success =
                manager.pauseWorkflow(
                        player,
                        workflow.getId()
                );

        if (!success) {

            sendFailure(
                    context,
                    "工作流暂停失败",
                    workflow,
                    player,
                    "当前工作流没有处于运行状态"
            );

            return 0;
        }

        sendSuccess(
                context,
                "工作流已暂停",
                workflow,
                player
        );

        return Command.SINGLE_SUCCESS;
    }


    private static int stopWorkflow(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer player =
                context.getSource()
                        .getPlayerOrException();

        Workflow workflow =
                resolveWorkflow(
                        context,
                        StringArgumentType.getString(
                                context,
                                "workflow"
                        )
                );

        if (workflow == null) {
            return 0;
        }

        return stopWorkflowForTarget(
                context,
                player,
                workflow
        );
    }

    private static int stopWorkflowForPlayer(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer player =
                EntityArgument.getPlayer(
                        context,
                        "player"
                );

        Workflow workflow =
                resolveWorkflow(
                        context,
                        StringArgumentType.getString(
                                context,
                                "workflow"
                        )
                );

        if (workflow == null) {
            return 0;
        }

        return stopWorkflowForTarget(
                context,
                player,
                workflow
        );
    }

    private static int stopWorkflowForTarget(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player,
            Workflow workflow
    ) {

        WorkflowManager manager =
                WorkflowRuntime.getManager();

        boolean success =
                manager.stopWorkflow(
                        player,
                        workflow.getId()
                );

        if (!success) {

            sendFailure(
                    context,
                    "工作流停止失败",
                    workflow,
                    player,
                    "当前工作流没有处于运行或暂停状态"
            );

            return 0;
        }

        sendSuccess(
                context,
                "工作流已停止",
                workflow,
                player
        );

        return Command.SINGLE_SUCCESS;
    }


    private static int resetWorkflow(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer player =
                context.getSource()
                        .getPlayerOrException();

        Workflow workflow =
                resolveWorkflow(
                        context,
                        StringArgumentType.getString(
                                context,
                                "workflow"
                        )
                );

        if (workflow == null) {
            return 0;
        }

        return resetWorkflowForTarget(
                context,
                player,
                workflow
        );
    }

    private static int resetWorkflowForPlayer(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        ServerPlayer player =
                EntityArgument.getPlayer(
                        context,
                        "player"
                );

        Workflow workflow =
                resolveWorkflow(
                        context,
                        StringArgumentType.getString(
                                context,
                                "workflow"
                        )
                );

        if (workflow == null) {
            return 0;
        }

        return resetWorkflowForTarget(
                context,
                player,
                workflow
        );
    }

    private static int resetWorkflowForTarget(
            CommandContext<CommandSourceStack> context,
            ServerPlayer player,
            Workflow workflow
    ) {

        WorkflowManager manager =
                WorkflowRuntime.getManager();

        boolean success =
                manager.resetWorkflow(
                        player,
                        workflow.getId()
                );

        if (!success) {

            sendFailure(
                    context,
                    "工作流重置失败",
                    workflow,
                    player,
                    "当前没有可重置的运行进度"
            );

            return 0;
        }

        sendSuccess(
                context,
                "工作流已重置",
                workflow,
                player
        );

        return Command.SINGLE_SUCCESS;
    }


    private static Workflow resolveWorkflow(
            CommandContext<CommandSourceStack> context,
            String input
    ) {

        WorkflowManager manager =
                WorkflowRuntime.getManager();

        if (input == null
                || input.isBlank()) {

            sendFailure(
                    context,
                    "工作流解析失败",
                    null,
                    null,
                    "工作流名称不能为空"
            );

            return null;
        }

        try {

            Workflow workflow =
                    manager.resolveWorkflow(
                            input
                    );

            if (workflow != null) {
                return workflow;
            }

            sendFailure(
                    context,
                    "未找到 Workflow",
                    null,
                    null,
                    input
            );

            return null;

        } catch (IllegalStateException exception) {

            List<Workflow> matches =
                    manager.findWorkflowsByName(
                            input
                    );

            sendDuplicateWorkflowMessage(
                    context,
                    input,
                    matches
            );

            return null;
        }
    }


    private static void sendDuplicateWorkflowMessage(
            CommandContext<CommandSourceStack> context,
            String name,
            List<Workflow> matches
    ) {

        MutableComponent message =
                Component.literal(
                        "§c 存在同名 Workflow: §f"
                                + name
                                + "\n"
                                + "§7请使用下面的 UUID 区分:"
                );

        for (int index = 0;
             index < matches.size();
             index++) {

            Workflow workflow =
                    matches.get(index);

            String uuid =
                    workflow.getId().toString();

            MutableComponent uuidComponent =
                    Component.literal(
                            uuid
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
                                                            ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                            uuid
                                                    )
                                            )
                                            .withHoverEvent(
                                                    new HoverEvent(
                                                            HoverEvent.Action.SHOW_TEXT,
                                                            Component.literal(
                                                                    "点击复制 UUID"
                                                            )
                                                    )
                                            )
                    );

            message.append(
                    Component.literal(
                            "\n"
                                    + (index + 1)
                                    + ". §f"
                                    + workflow.getName()
                                    + " §7"
                    )
            );

            message.append(
                    uuidComponent
            );
        }

        context.getSource().sendFailure(
                message
        );
    }


    private static void sendSuccess(
            CommandContext<CommandSourceStack> context,
            String title,
            Workflow workflow,
            ServerPlayer player
    ) {

        context.getSource().sendSuccess(
                () ->
                        Component.literal(
                                "§a "
                                        + title
                                        + "\n"
                                        + "§7Workflow: §f"
                                        + workflow.getName()
                                        + "\n"
                                        + "§7玩家: §f"
                                        + player.getName().getString()
                        ),
                true
        );
    }

    private static void sendFailure(
            CommandContext<CommandSourceStack> context,
            String title,
            Workflow workflow,
            ServerPlayer player,
            String reason
    ) {

        MutableComponent message =
                Component.literal(
                        "§c "
                                + title
                );

        if (workflow != null) {

            message.append(
                    Component.literal(
                            "\n§7Workflow: §f"
                                    + workflow.getName()
                    )
            );
        }

        if (player != null) {

            message.append(
                    Component.literal(
                            "\n§7玩家: §f"
                                    + player.getName().getString()
                    )
            );
        }

        message.append(
                Component.literal(
                        "\n§7原因: §f"
                                + reason
                )
        );

        context.getSource().sendFailure(
                message
        );
    }
}