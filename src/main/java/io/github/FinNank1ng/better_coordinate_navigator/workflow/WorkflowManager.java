package io.github.FinNank1ng.better_coordinate_navigator.workflow;

import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 工作流运行管理器
 */
public class WorkflowManager {

    private final Map<UUID, Workflow> workflows =
            new HashMap<>();

    private final Map<UUID, WorkflowProgress> progress =
            new HashMap<>();


    /*
     * 注册工作流
     */
    public void registerWorkflow(
            Workflow workflow
    ) {

        Objects.requireNonNull(
                workflow,
                "Workflow 不能为 null"
        );

        workflows.put(
                workflow.getId(),
                workflow
        );
    }


    /*
     * 删除工作流
     */
    public void removeWorkflow(
            UUID workflowId
    ) {

        Objects.requireNonNull(
                workflowId,
                "workflowId 不能为 null"
        );

        workflows.remove(workflowId);

        progress.entrySet().removeIf(
                entry ->
                        entry.getValue()
                                .workflowId()
                                .equals(workflowId)
        );
    }


    public Workflow getWorkflow(
            UUID workflowId
    ) {

        return workflows.get(workflowId);
    }


    /*
     * 启动一个工作流
     */
    public boolean startWorkflow(
            ServerPlayer player,
            UUID workflowId
    ) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        Objects.requireNonNull(
                workflowId,
                "workflowId 不能为 null"
        );

        Workflow workflow =
                workflows.get(workflowId);

        if (workflow == null) {
            return false;
        }

        if (workflow.isEmpty()) {
            return false;
        }

        progress.put(
                player.getUUID(),
                new WorkflowProgress(
                        workflowId,
                        0
                )
        );

        System.out.println(
                "[BCN] 玩家 "
                        + player.getGameProfile().getName()
                        + " 开始工作流 "
                        + workflow.getName()
        );

        return true;
    }


    /*
     * 停止当前工作流
     */
    public void stopWorkflow(
            ServerPlayer player
    ) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        progress.remove(
                player.getUUID()
        );
    }


    public WorkflowProgress getProgress(
            ServerPlayer player
    ) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        return progress.get(
                player.getUUID()
        );
    }


    public boolean isRunning(
            ServerPlayer player
    ) {

        return progress.containsKey(
                player.getUUID()
        );
    }


    /*
     * 每个服务器 Tick 调用
     */
    public void tick(
            ServerPlayer player
    ) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        WorkflowProgress currentProgress =
                progress.get(
                        player.getUUID()
                );

        if (currentProgress == null) {
            return;
        }

        Workflow workflow =
                workflows.get(
                        currentProgress.workflowId()
                );

        if (workflow == null) {

            progress.remove(
                    player.getUUID()
            );

            return;
        }

        int stepIndex =
                currentProgress.stepIndex();

        if (stepIndex >= workflow.getStepCount()) {

            completeWorkflow(
                    player,
                    workflow
            );

            return;
        }

        WorkflowStep step =
                workflow.getStep(stepIndex);

        QuestMarker marker =
                findMarker(
                        player,
                        step.getMarkerId()
                );

        if (marker == null) {

            System.out.println(
                    "[BCN] 工作流 "
                            + workflow.getName()
                            + " 的步骤 "
                            + stepIndex
                            + " 找不到对应标点"
            );

            return;
        }

        if (!isInsideMarker(
                player,
                marker,
                step.getTriggerRadius()
        )) {
            return;
        }

        onStepReached(
                player,
                workflow,
                step,
                stepIndex
        );
    }

    /*
     * 玩家到达步骤
     */
    private void onStepReached(
            ServerPlayer player,
            Workflow workflow,
            WorkflowStep step,
            int stepIndex
    ) {

        System.out.println(
                "[BCN] 玩家 "
                        + player.getGameProfile().getName()
                        + " 到达工作流 "
                        + workflow.getName()
                        + " 步骤 "
                        + stepIndex
        );

        executeActions(
                player,
                step
        );

        int nextStep =
                stepIndex + 1;

        if (nextStep >= workflow.getStepCount()) {

            completeWorkflow(
                    player,
                    workflow
            );

            return;
        }

        progress.put(
                player.getUUID(),
                new WorkflowProgress(
                        workflow.getId(),
                        nextStep
                )
        );

        System.out.println(
                "[BCN] 下一工作流步骤: "
                        + nextStep
        );
    }


    /*
     * 执行步骤中的全部动作
     */
    private void executeActions(
            ServerPlayer player,
            WorkflowStep step
    ) {

        for (WorkflowAction action :
                step.getActions()) {

            try {

                executeAction(
                        player,
                        action
                );

            } catch (Exception exception) {

                System.err.println(
                        "[BCN] 工作流 Action 执行失败: "
                                + action
                );

                exception.printStackTrace();
            }
        }
    }


    /*
     * 执行单个 Action
     */
    private void executeAction(
            ServerPlayer player,
            WorkflowAction action
    ) {

        switch (action.getType()) {

            case EXECUTE_COMMAND -> {

                String command =
                        action.getData();

                if (command.startsWith("/")) {
                    command =
                            command.substring(1);
                }

                player.server
                        .getCommands()
                        .performPrefixedCommand(
                                player.createCommandSourceStack(),
                                command
                        );
            }


            case MESSAGE -> {

                player.sendSystemMessage(
                        net.minecraft.network.chat.Component
                                .literal(
                                        action.getData()
                                )
                );
            }


            case GIVE_ITEM -> {

                String command =
                        "give @s "
                                + action.getData()
                                + " "
                                + action.getCount();

                player.server
                        .getCommands()
                        .performPrefixedCommand(
                                player.createCommandSourceStack(),
                                command
                        );
            }


            case ENABLE_MARKER -> {

                // V1 先由标点状态系统接管
                System.out.println(
                        "[BCN] ENABLE_MARKER "
                                + action.getData()
                );
            }


            case DISABLE_MARKER -> {

                // V1 先由标点状态系统接管
                System.out.println(
                        "[BCN] DISABLE_MARKER "
                                + action.getData()
                );
            }


            case SOUND -> {

                // V1 后续接 Minecraft Sound API
                System.out.println(
                        "[BCN] SOUND "
                                + action.getData()
                );
            }
        }
    }


    /*
     * 查找标点
     */
    private QuestMarker findMarker(
            ServerPlayer player,
            UUID markerId
    ) {

        QuestManager manager =
                QuestManager.get(
                        player.serverLevel()
                );

        for (QuestMarker marker :
                manager.getMarkers()) {

            /*
             * 这里需要 QuestMarker.getId()
             */
            if (marker.getId().equals(markerId)) {
                return marker;
            }
        }

        return null;
    }


    /*
     * 判断玩家是否进入标点触发范围
     */
    private boolean isInsideMarker(
            ServerPlayer player,
            QuestMarker marker,
            double radius
    ) {

        double dx =
                player.getX() - marker.x;

        double dy =
                player.getY() - marker.y;

        double dz =
                player.getZ() - marker.z;

        return dx * dx
                + dy * dy
                + dz * dz
                <= radius * radius;
    }


    private void completeWorkflow(
            ServerPlayer player,
            Workflow workflow
    ) {

        progress.remove(
                player.getUUID()
        );

        System.out.println(
                "[BCN] 玩家 "
                        + player.getGameProfile().getName()
                        + " 完成工作流 "
                        + workflow.getName()
        );
    }


    public record WorkflowProgress(
            UUID workflowId,
            int stepIndex
    ) {
    }
}