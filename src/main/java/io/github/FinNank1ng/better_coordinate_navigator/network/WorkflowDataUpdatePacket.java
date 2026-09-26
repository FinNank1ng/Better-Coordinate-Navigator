package io.github.FinNank1ng.better_coordinate_navigator.network;

import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.WorkflowScreen;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientWorkflowPermission;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientWorkflowCache;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.Workflow;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowAction;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowStep;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static com.mojang.text2speech.Narrator.LOGGER;

/*
 * 服务器同步工作流数据到客户端
 */
public class WorkflowDataUpdatePacket {

    /*
     * 服务器中的全部工作流
     */
    public final List<Workflow> workflows;

    /*
     * 当前玩家是否拥有工作流管理权限
     */
    public final boolean canManage;

    /*
     * 创建同步数据包
     */
    public WorkflowDataUpdatePacket(
            List<Workflow> workflows,
            boolean canManage
    ) {

        this.workflows =
                workflows == null
                        ? new ArrayList<>()
                        : new ArrayList<>(
                        workflows
                );

        this.canManage = canManage;
    }

    /*
     * 从网络读取
     */
    public WorkflowDataUpdatePacket(
            FriendlyByteBuf buf
    ) {

        /*
         * 读取顺序必须与 encode 完全一致
         */
        canManage = buf.readBoolean();

        int workflowCount = buf.readInt();

        if (workflowCount < 0) {
            throw new IllegalArgumentException(
                    "工作流数量不能小于 0"
            );
        }

        workflows =
                new ArrayList<>(
                        workflowCount
                );

        for (int i = 0;
             i < workflowCount;
             i++) {

            workflows.add(
                    readWorkflow(buf)
            );
        }
    }

    /*
     * 编码
     */
    public void encode(
            FriendlyByteBuf buf
    ) {

        /*
         * 写入顺序必须与构造器读取顺序一致
         */
        buf.writeBoolean(
                canManage
        );

        buf.writeInt(
                workflows.size()
        );

        for (Workflow workflow :
                workflows) {

            writeWorkflow(
                    buf,
                    workflow
            );
        }
    }

    /*
     * 写入一个 Workflow
     */
    private static void writeWorkflow(
            FriendlyByteBuf buf,
            Workflow workflow
    ) {

        buf.writeUUID(
                workflow.getId()
        );

        buf.writeUtf(
                workflow.getName()
        );

        buf.writeInt(
                workflow.getStepCount()
        );

        for (WorkflowStep step :
                workflow.getSteps()) {

            writeStep(
                    buf,
                    step
            );
        }
    }

    /*
     * 读取一个 Workflow
     */
    private static Workflow readWorkflow(
            FriendlyByteBuf buf
    ) {

        UUID id = buf.readUUID();

        String name = buf.readUtf();

        Workflow workflow =
                new Workflow(
                        id,
                        name
                );

        int stepCount = buf.readInt();

        if (stepCount < 0) {
            throw new IllegalArgumentException(
                    "工作流节点数量不能小于 0"
            );
        }

        for (int i = 0;
             i < stepCount;
             i++) {

            workflow.addStep(
                    readStep(buf)
            );
        }

        return workflow;
    }

    /*
     * 写入一个 WorkflowStep
     */
    private static void writeStep(
            FriendlyByteBuf buf,
            WorkflowStep step
    ) {

        buf.writeUUID(
                step.getId()
        );

        buf.writeUUID(
                step.getMarkerId()
        );

        buf.writeDouble(
                step.getTriggerRadius()
        );

        buf.writeInt(
                step.getActionCount()
        );

        for (WorkflowAction action :
                step.getActions()) {

            writeAction(
                    buf,
                    action
            );
        }
    }

    /*
     * 读取一个 WorkflowStep
     */
    private static WorkflowStep readStep(
            FriendlyByteBuf buf
    ) {

        UUID id = buf.readUUID();

        UUID markerId = buf.readUUID();

        double triggerRadius = buf.readDouble();

        WorkflowStep step = new WorkflowStep(
                id,
                markerId,
                triggerRadius
        );

        int actionCount =
                buf.readInt();

        if (actionCount < 0) {
            throw new IllegalArgumentException(
                    "Action 数量不能小于 0"
            );
        }

        for (int i = 0;
             i < actionCount;
             i++) {

            step.addAction(
                    readAction(buf)
            );
        }

        return step;
    }

    /*
     * 写入一个 WorkflowAction
     */
    private static void writeAction(
            FriendlyByteBuf buf,
            WorkflowAction action
    ) {

        buf.writeEnum(
                action.getType()
        );

        buf.writeUtf(
                action.getData()
        );

        buf.writeInt(
                action.getCount()
        );
    }

    /*
     * 读取一个 WorkflowAction
     */
    private static WorkflowAction readAction(
            FriendlyByteBuf buf
    ) {

        WorkflowAction.Type type = buf.readEnum(
                WorkflowAction.Type.class
        );

        String data = buf.readUtf();

        int count = buf.readInt();

        return WorkflowAction.of(
                type,
                data,
                count
        );
    }

    /*
     * 客户端处理
     */
    public static void handle(
            WorkflowDataUpdatePacket msg,
            Supplier<NetworkEvent.Context> ctx
    ) {

        LOGGER.debug(
                "[BCN] Workflow Data Packet Received: {} workflows, canManage={}",
                msg.workflows.size(),
                msg.canManage
        );

        ctx.get().enqueueWork(() -> {

            ClientWorkflowCache.set(
                    msg.workflows
            );

            ClientWorkflowPermission.setCanManage(
                    msg.canManage
            );

            LOGGER.debug(
                    "[BCN] Workflow Cache Updated: {} workflows",
                    ClientWorkflowCache.getWorkflows().size()
            );

            LOGGER.debug(
                    "[BCN] Workflow Permission Updated: {}",
                    msg.canManage
            );

            /*
             * 如果当前正在工作流编辑界面, 则通知界面服务器已经返回最新数据
             */
            Minecraft minecraft =
                    Minecraft.getInstance();

            if (minecraft.screen
                    instanceof WorkflowScreen screen) {

                screen.onWorkflowDataUpdated();
            }
        });

        ctx.get().setPacketHandled(true);
    }
}