package io.github.FinNank1ng.better_coordinate_navigator.network;

import io.github.FinNank1ng.better_coordinate_navigator.permission.BCNPermissions;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowDataManager;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowSavedData;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

import static com.mojang.text2speech.Narrator.LOGGER;
import static io.github.FinNank1ng.better_coordinate_navigator.network.ModPackets.CHANNEL;

/*
 * 客户端请求工作流数据
 */
public class WorkflowDataRequestPacket {

    public WorkflowDataRequestPacket() {
    }

    public WorkflowDataRequestPacket(
            FriendlyByteBuf buf
    ) {
    }

    public void encode(
            FriendlyByteBuf buf
    ) {
    }

    /*
     * 处理客户端工作流数据请求
     */
    public static void handle(
            WorkflowDataRequestPacket msg,
            Supplier<NetworkEvent.Context> ctx
    ) {

        ctx.get().enqueueWork(() -> {

            LOGGER.debug(
                    "[BCN] Workflow Data Request Received"
            );

            ServerPlayer player =
                    ctx.get().getSender();

            if (player == null) {

                LOGGER.debug(
                        "[BCN] Workflow Request Player NULL"
                );

                return;
            }

            /*
             * 服务端统一使用主世界保存全局工作流数据
             */
            WorkflowSavedData savedData =
                    WorkflowDataManager.get(
                            player.server.overworld()
                    );

            /*
             * 判断当前玩家是否拥有工作流管理权限
             */
            boolean canManage =
                    BCNPermissions.hasAdminPermission(
                            player
                    );

            LOGGER.debug(
                    "[BCN] Sending {} workflows to {}",
                    savedData.getWorkflows().size(),
                    player.getGameProfile().getName()
            );

            LOGGER.debug(
                    "[BCN] Workflow management permission: {}",
                    canManage
            );

            /*
             * 将工作流数据和玩家权限一起发送到客户端
             */
            CHANNEL.send(
                    PacketDistributor.PLAYER.with(
                            () -> player
                    ),
                    new WorkflowDataUpdatePacket(
                            savedData.getWorkflows(),
                            canManage
                    )
            );
        });

        ctx.get().setPacketHandled(true);
    }
}