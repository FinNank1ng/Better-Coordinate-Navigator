package io.github.FinNank1ng.better_coordinate_navigator.network;

import io.github.FinNank1ng.better_coordinate_navigator.client.ClientDataSyncManager;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.mojang.text2speech.Narrator.LOGGER;

public class QuestDataUpdatePacket {

    /*
     * 世界中的全部标点
     */
    public final List<QuestMarker> markers;

    /*
     * 当前玩家追踪的标点名称
     */
    public final List<String> trackedMarkers;

    /*
     * 创建同步数据包
     */
    public QuestDataUpdatePacket(
            List<QuestMarker> markers,
            List<String> trackedMarkers
    ) {

        this.markers = new ArrayList<>(markers);

        this.trackedMarkers = new ArrayList<>(trackedMarkers);
    }

    /**
     * 从网络读取
     */
    public QuestDataUpdatePacket(
            FriendlyByteBuf buf
    ) {

        /*
         * 标点数据
         */
        int size = buf.readInt();

        markers = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {

            markers.add(
                    new QuestMarker(buf)
            );
        }

        /*
         * 玩家追踪数据
         */
        int trackedSize = buf.readInt();

        trackedMarkers = new ArrayList<>(trackedSize);

        for (int i = 0; i < trackedSize; i++) {

            trackedMarkers.add(
                    buf.readUtf()
            );
        }
    }

    /**
     * 编码
     */
    public void encode(
            FriendlyByteBuf buf
    ) {

        /*
         * 标点数据
         */
        buf.writeInt(
                markers.size()
        );

        for (QuestMarker marker : markers) {

            marker.encode(buf);
        }

        /*
         * 玩家追踪数据
         */
        buf.writeInt(
                trackedMarkers.size()
        );

        for (String name : trackedMarkers) {

            buf.writeUtf(name);
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
             * 更新标点缓存
             */
            ClientQuestCache.set(
                    msg.markers
            );

            /*
             * 更新当前玩家追踪状态
             */
            ClientQuestCache.setTrackedMarkers(
                    msg.trackedMarkers
            );

            /*
             * 通知客户端相关系统刷新
             */
            ClientDataSyncManager.refreshAll();

            LOGGER.debug(
                    "[BCN] Cache Updated: {} markers",
                    ClientQuestCache
                            .getMarkers()
                            .size()
            );
        });

        ctx.get().setPacketHandled(true);
    }
}