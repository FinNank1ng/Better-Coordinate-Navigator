package io.github.FinNank1ng.better_coordinate_navigator.network;

import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.mojang.text2speech.Narrator.LOGGER;


public class QuestDataUpdatePacket {


    public final List<QuestMarker> markers;

    /**
     * 当前玩家追踪的标点
     */
    public final List<String> trackedMarkers;

    public QuestDataUpdatePacket(
            List<QuestMarker> markers,
            List<String> trackedMarkers
    ){

        this.markers = markers;

        this.trackedMarkers = trackedMarkers;

    }

    public QuestDataUpdatePacket(FriendlyByteBuf buf) {

        int size = buf.readInt();

        markers = new ArrayList<>();

        for (int i = 0; i < size; i++) {

            markers.add(new QuestMarker(buf));

        }

        int trackedSize = buf.readInt();

        trackedMarkers = new ArrayList<>();

        for(int i = 0; i < trackedSize; i++){

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
    ){

        /*
         * 标点数据
         */
        buf.writeInt(markers.size());

        for (QuestMarker marker : markers){

            marker.encode(buf);

        }

        /*
         * 玩家追踪数据
         */
        buf.writeInt(trackedMarkers.size());

        for(String name : trackedMarkers){

            buf.writeUtf(name);

        }

    }

    /**
     * 客户端处理
     */
    public static void handle(
            QuestDataUpdatePacket msg,
            Supplier<NetworkEvent.Context> ctx
    ){

        LOGGER.debug(
                "[BCN] Packet Received: "
                        + msg.markers.size()
        );

        ctx.get().enqueueWork(() -> {

            ClientQuestCache.set(
                    msg.markers
            );

            ClientQuestCache.setTrackedMarkers(
                    msg.trackedMarkers
            );

            LOGGER.debug(
                    "[BCN] Cache Updated: "
                            +
                            ClientQuestCache.getMarkers().size()
            );

        });

        ctx.get().setPacketHandled(true);

    }

}