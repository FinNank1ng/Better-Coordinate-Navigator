
package io.github.FinNank1ng.better_coordinate_navigator.network;

import io.github.FinNank1ng.better_coordinate_navigator.client.ClientDataSyncManager;
import io.github.FinNank1ng.better_coordinate_navigator.compat.xaero.XaeroClientSync;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static com.mojang.text2speech.Narrator.LOGGER;

public class QuestDataUpdatePacket {

    /**
     * 世界中的全部标点
     */
    public final List<QuestMarker> markers;

    /**
     * 当前玩家追踪的标点 UUID
     */
    public final List<UUID> trackedMarkerIds;

    /**
     * 创建同步数据包
     */
    public QuestDataUpdatePacket(
            List<QuestMarker> markers,
            List<UUID> trackedMarkerIds
    ) {

        this.markers = markers == null
                ? new ArrayList<>()
                : new ArrayList<>(markers);

        this.trackedMarkerIds = trackedMarkerIds == null
                ? new ArrayList<>()
                : new ArrayList<>(trackedMarkerIds);
    }

    /**
     * 从网络读取
     */
    public QuestDataUpdatePacket(FriendlyByteBuf buf) {

        /*
         * 标点数据
         */
        int size = buf.readInt();

        markers = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            markers.add(new QuestMarker(buf));
        }

        /*
         * 玩家追踪数据
         */
        int trackedSize = buf.readInt();

        trackedMarkerIds = new ArrayList<>(trackedSize);

        for (int i = 0; i < trackedSize; i++) {
            trackedMarkerIds.add(buf.readUUID());
        }
    }

    /**
     * 编码
     */
    public void encode(FriendlyByteBuf buf) {

        /*
         * 标点数据
         */
        buf.writeInt(markers.size());

        for (QuestMarker marker : markers) {
            marker.encode(buf);
        }

        /*
         * 玩家追踪数据
         */
        buf.writeInt(trackedMarkerIds.size());

        for (UUID markerId : trackedMarkerIds) {
            buf.writeUUID(markerId);
        }
    }

    /**
     * 客户端处理
     */
    public static void handle(
            QuestDataUpdatePacket msg,
            Supplier<NetworkEvent.Context> ctx
    ) {

        LOGGER.debug(
                "[BCN] Packet Received: {} markers",
                msg.markers.size()
        );

        ctx.get().enqueueWork(() -> {

            /*
             * 获取之前追踪的 UUID
             */
            List<UUID> oldTrackedIds =
                    new ArrayList<>(
                            ClientQuestCache.getTrackedMarkerIds()
                    );

            /*
             * 删除已经取消追踪的 BCN waypoint
             */
            for (UUID oldId : oldTrackedIds) {

                if (!msg.trackedMarkerIds.contains(oldId)) {

                    XaeroClientSync.removeMarker(oldId);

                    LOGGER.debug(
                            "[BCN] Removed Xaero waypoint: {}",
                            oldId
                    );
                }
            }

            /*
             * 更新标点缓存
             */
            ClientQuestCache.set(msg.markers);

            /*
             * 更新 UUID 追踪缓存
             */
            ClientQuestCache.setTrackedMarkerIds(
                    msg.trackedMarkerIds
            );

            /**
             * 通知客户端相关系统刷新
             */
            ClientDataSyncManager.refreshAll();

            LOGGER.debug(
                    "[BCN] Cache Updated: {} markers, {} tracked",
                    ClientQuestCache.getMarkers().size(),
                    ClientQuestCache.getTrackedMarkerIds().size()
            );
        });

        ctx.get().setPacketHandled(true);
    }
}
