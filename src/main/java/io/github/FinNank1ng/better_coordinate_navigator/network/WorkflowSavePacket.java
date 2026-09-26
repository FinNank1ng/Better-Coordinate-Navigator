package io.github.FinNank1ng.better_coordinate_navigator.network;

import io.github.FinNank1ng.better_coordinate_navigator.permission.BCNPermissions;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.Workflow;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowAction;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowDataManager;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowManager;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowRuntime;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowSavedData;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowStep;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static com.mojang.text2speech.Narrator.LOGGER;
import static io.github.FinNank1ng.better_coordinate_navigator.network.ModPackets.CHANNEL;

/*
 * 客户端请求保存工作流
 */
public class WorkflowSavePacket {

    private static final int MAX_WORKFLOWS = 256;
    private static final int MAX_STEPS_PER_WORKFLOW = 512;
    private static final int MAX_ACTIONS_PER_STEP = 128;

    private static final int MAX_NAME_LENGTH = 80;
    private static final int MAX_ACTION_DATA_LENGTH = 2048;

    private final List<Workflow> workflows;

    public WorkflowSavePacket(
            List<Workflow> workflows
    ) {
        this.workflows =
                new ArrayList<>(
                        workflows == null
                                ? List.of()
                                : workflows
                );
    }

    public WorkflowSavePacket(
            FriendlyByteBuf buffer
    ) {

        int workflowCount =
                buffer.readVarInt();

        if (workflowCount < 0
                || workflowCount > MAX_WORKFLOWS) {

            throw new IllegalArgumentException(
                    "工作流数量超出限制"
            );
        }

        List<Workflow> result =
                new ArrayList<>(
                        workflowCount
                );

        for (int i = 0;
             i < workflowCount;
             i++) {

            result.add(
                    readWorkflow(buffer)
            );
        }

        this.workflows = result;
    }

    /*
     * 编码工作流保存数据
     */
    public static void encode(
            WorkflowSavePacket message,
            FriendlyByteBuf buffer
    ) {

        if (message.workflows.size()
                > MAX_WORKFLOWS) {

            throw new IllegalArgumentException(
                    "工作流数量超出限制"
            );
        }

        buffer.writeVarInt(
                message.workflows.size()
        );

        for (Workflow workflow :
                message.workflows) {

            writeWorkflow(
                    buffer,
                    workflow
            );
        }
    }

    /*
     * 处理保存请求
     */
    public static void handle(
            WorkflowSavePacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {

        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() -> {

            ServerPlayer player =
                    context.getSender();

            if (player == null) {

                LOGGER.warn(
                        "[BCN] Workflow save rejected: player is null"
                );

                return;
            }

            /*
             * 服务端权限检查
             */
            if (!BCNPermissions.hasAdminPermission(
                    player
            )) {

                LOGGER.warn(
                        "[BCN] Workflow save rejected: {} has no permission",
                        player.getGameProfile()
                                .getName()
                );

                return;
            }

            try {

                validateWorkflows(
                        message.workflows
                );

                saveWorkflows(
                        player,
                        message.workflows
                );

                LOGGER.info(
                        "[BCN] Workflow data saved by {}: {} workflows",
                        player.getGameProfile()
                                .getName(),
                        message.workflows.size()
                );

                /*
                 * 保存成功后重新发送服务端最终数据
                 */
                WorkflowSavedData savedData =
                        WorkflowDataManager.get(
                                player.server.overworld()
                        );

                CHANNEL.send(
                        PacketDistributor.PLAYER.with(
                                () -> player
                        ),
                        new WorkflowDataUpdatePacket(
                                savedData.getWorkflows(),
                                true
                        )
                );

            } catch (Exception exception) {

                LOGGER.error(
                        "[BCN] Failed to save workflow data",
                        exception
                );
            }
        });

        context.setPacketHandled(true);
    }

    /*
     * 保存工作流到服务端
     */
    private static void saveWorkflows(
            ServerPlayer player,
            List<Workflow> workflows
    ) {

        WorkflowSavedData savedData =
                WorkflowDataManager.get(
                        player.server.overworld()
                );

        /*
         * 删除客户端已经不存在的工作流
         */
        Set<UUID> incomingIds =
                new HashSet<>();

        for (Workflow workflow :
                workflows) {

            incomingIds.add(
                    workflow.getId()
            );
        }

        List<Workflow> existing =
                new ArrayList<>(
                        savedData.getWorkflows()
                );

        for (Workflow workflow :
                existing) {

            if (!incomingIds.contains(
                    workflow.getId()
            )) {

                savedData.removeWorkflow(
                        workflow.getId()
                );
            }
        }

        /*
         * 添加或替换工作流
         */
        for (Workflow workflow :
                workflows) {

            if (savedData.getWorkflows()
                    .stream()
                    .anyMatch(
                            existingWorkflow ->
                                    existingWorkflow
                                            .getId()
                                            .equals(
                                                    workflow
                                                            .getId()
                                            )
                    )) {

                savedData.replaceWorkflow(
                        workflow
                );

            } else {

                savedData.addWorkflow(
                        workflow
                );
            }
        }

        /*
         * 同步运行时管理器
         */
        WorkflowManager manager =
                WorkflowRuntime.getManager();

        List<Workflow> current =
                new ArrayList<>(
                        manager.getWorkflows()
                );

        Set<UUID> managerIds =
                new HashSet<>();

        for (Workflow workflow :
                workflows) {

            managerIds.add(
                    workflow.getId()
            );
        }

        for (Workflow workflow :
                current) {

            if (!managerIds.contains(
                    workflow.getId()
            )) {

                manager.removeWorkflow(
                        workflow.getId()
                );
            }
        }

        for (Workflow workflow :
                workflows) {

            if (current.stream()
                    .anyMatch(
                            existingWorkflow ->
                                    existingWorkflow
                                            .getId()
                                            .equals(
                                                    workflow
                                                            .getId()
                                            )
                    )) {

                manager.replaceWorkflow(
                        workflow
                );

            } else {

                manager.registerWorkflow(
                        workflow
                );
            }
        }
    }

    /*
     * 校验工作流集合
     */
    private static void validateWorkflows(
            List<Workflow> workflows
    ) {

        if (workflows == null) {

            throw new IllegalArgumentException(
                    "工作流数据不能为 null"
            );
        }

        if (workflows.size()
                > MAX_WORKFLOWS) {

            throw new IllegalArgumentException(
                    "工作流数量超出限制"
            );
        }

        Set<UUID> workflowIds =
                new HashSet<>();

        for (Workflow workflow :
                workflows) {

            validateWorkflow(
                    workflow
            );

            if (!workflowIds.add(
                    workflow.getId()
            )) {

                throw new IllegalArgumentException(
                        "存在重复的工作流 UUID"
                );
            }
        }
    }

    /*
     * 校验单个工作流
     */
    private static void validateWorkflow(
            Workflow workflow
    ) {

        if (workflow == null) {

            throw new IllegalArgumentException(
                    "工作流不能为 null"
            );
        }

        if (workflow.getName() == null
                || workflow.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "工作流名称不能为空"
            );
        }

        if (workflow.getName().length()
                > MAX_NAME_LENGTH) {

            throw new IllegalArgumentException(
                    "工作流名称过长"
            );
        }

        if (workflow.getStepCount()
                > MAX_STEPS_PER_WORKFLOW) {

            throw new IllegalArgumentException(
                    "工作流节点数量超出限制"
            );
        }

        Set<UUID> stepIds =
                new HashSet<>();

        for (WorkflowStep step :
                workflow.getSteps()) {

            if (step == null) {

                throw new IllegalArgumentException(
                        "工作流节点不能为 null"
                );
            }

            if (!stepIds.add(
                    step.getId()
            )) {

                throw new IllegalArgumentException(
                        "工作流中存在重复的节点 UUID"
                );
            }

            if (step.getMarkerId() == null) {

                throw new IllegalArgumentException(
                        "工作流节点目标标点不能为 null"
                );
            }

            if (step.getTriggerRadius() <= 0.0D) {

                throw new IllegalArgumentException(
                        "工作流节点触发半径必须大于 0"
                );
            }

            if (step.getActions().size()
                    > MAX_ACTIONS_PER_STEP) {

                throw new IllegalArgumentException(
                        "单个节点 Action 数量超出限制"
                );
            }

            for (WorkflowAction action :
                    step.getActions()) {

                validateAction(
                        action
                );
            }
        }
    }

    /*
     * 校验工作流动作
     */
    private static void validateAction(
            WorkflowAction action
    ) {

        if (action == null) {

            throw new IllegalArgumentException(
                    "工作流 Action 不能为 null"
            );
        }

        if (action.getData() == null) {

            throw new IllegalArgumentException(
                    "工作流 Action 数据不能为 null"
            );
        }

        if (action.getData().length()
                > MAX_ACTION_DATA_LENGTH) {

            throw new IllegalArgumentException(
                    "工作流 Action 数据过长"
            );
        }

        if (action.getCount() <= 0) {

            throw new IllegalArgumentException(
                    "工作流 Action 数量必须大于 0"
            );
        }
    }

    /*
     * 写入工作流
     */
    private static void writeWorkflow(
            FriendlyByteBuf buffer,
            Workflow workflow
    ) {

        validateWorkflow(
                workflow
        );

        buffer.writeUUID(
                workflow.getId()
        );

        buffer.writeUtf(
                workflow.getName(),
                MAX_NAME_LENGTH
        );

        buffer.writeVarInt(
                workflow.getStepCount()
        );

        for (WorkflowStep step :
                workflow.getSteps()) {

            buffer.writeUUID(
                    step.getId()
            );

            buffer.writeUUID(
                    step.getMarkerId()
            );

            buffer.writeDouble(
                    step.getTriggerRadius()
            );

            buffer.writeVarInt(
                    step.getActions().size()
            );

            for (WorkflowAction action :
                    step.getActions()) {

                buffer.writeEnum(
                        action.getType()
                );

                buffer.writeUtf(
                        action.getData(),
                        MAX_ACTION_DATA_LENGTH
                );

                buffer.writeVarInt(
                        action.getCount()
                );
            }
        }
    }

    /*
     * 读取工作流
     */
    private static Workflow readWorkflow(
            FriendlyByteBuf buffer
    ) {

        UUID workflowId =
                buffer.readUUID();

        String name =
                buffer.readUtf(
                        MAX_NAME_LENGTH
                );

        Workflow workflow =
                new Workflow(
                        workflowId,
                        name
                );

        int stepCount =
                buffer.readVarInt();

        if (stepCount < 0
                || stepCount > MAX_STEPS_PER_WORKFLOW) {

            throw new IllegalArgumentException(
                    "节点数量超出限制"
            );
        }

        for (int i = 0;
             i < stepCount;
             i++) {

            UUID stepId =
                    buffer.readUUID();

            UUID markerId =
                    buffer.readUUID();

            double triggerRadius =
                    buffer.readDouble();

            WorkflowStep step =
                    new WorkflowStep(
                            stepId,
                            markerId,
                            triggerRadius
                    );

            int actionCount =
                    buffer.readVarInt();

            if (actionCount < 0
                    || actionCount
                    > MAX_ACTIONS_PER_STEP) {

                throw new IllegalArgumentException(
                        "Action 数量超出限制"
                );
            }

            for (int j = 0;
                 j < actionCount;
                 j++) {

                WorkflowAction.Type type =
                        buffer.readEnum(
                                WorkflowAction.Type.class
                        );

                String data =
                        buffer.readUtf(
                                MAX_ACTION_DATA_LENGTH
                        );

                int count =
                        buffer.readVarInt();

                step.addAction(
                        WorkflowAction.of(
                                type,
                                data,
                                count
                        )
                );
            }

            workflow.addStep(
                    step
            );
        }

        return workflow;
    }
}