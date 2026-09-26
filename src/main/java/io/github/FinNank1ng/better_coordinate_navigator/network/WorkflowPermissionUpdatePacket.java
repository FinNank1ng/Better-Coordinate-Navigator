package io.github.FinNank1ng.better_coordinate_navigator.network;

import io.github.FinNank1ng.better_coordinate_navigator.data.ClientWorkflowPermission;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.mojang.text2speech.Narrator.LOGGER;

/*
 * 服务器同步工作流管理权限到客户端
 */
public class WorkflowPermissionUpdatePacket {

    private final boolean canManage;

    /*
     * 创建权限同步数据包
     */
    public WorkflowPermissionUpdatePacket(
            boolean canManage
    ) {
        this.canManage = canManage;
    }

    /*
     * 从网络读取
     */
    public WorkflowPermissionUpdatePacket(
            FriendlyByteBuf buf
    ) {
        this.canManage =
                buf.readBoolean();
    }

    /*
     * 编码
     */
    public void encode(
            FriendlyByteBuf buf
    ) {
        buf.writeBoolean(
                canManage
        );
    }

    /*
     * 获取权限状态
     */
    public boolean canManage() {
        return canManage;
    }

    /*
     * 客户端处理
     */
    public static void handle(
            WorkflowPermissionUpdatePacket msg,
            Supplier<NetworkEvent.Context> ctx
    ) {

        LOGGER.debug(
                "[BCN] Workflow Permission Received: {}",
                msg.canManage
        );

        ctx.get().enqueueWork(() -> {

            ClientWorkflowPermission.setCanManage(
                    msg.canManage
            );

        });

        ctx.get().setPacketHandled(true);
    }
}