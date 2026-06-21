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

    public QuestDataUpdatePacket(List<QuestMarker> markers) {
        this.markers = markers;
    }

    public QuestDataUpdatePacket(
            FriendlyByteBuf buf
    ) {

        int size = buf.readInt();

        markers = new ArrayList<>();

        for (int i = 0; i < size; i++) {

            markers.add(new QuestMarker(buf)

            );
        }
    }

    public void encode(
            FriendlyByteBuf buf
    ) {

        buf.writeInt(markers.size());

        for (QuestMarker marker : markers) {

            marker.encode(buf);
        }
    }

    public static void handle(
            QuestDataUpdatePacket msg,
            Supplier<NetworkEvent.Context> ctx
    ) {

        LOGGER.debug(
                "[BCN] Packet Received: "
                        + msg.markers.size()
        );

        ctx.get().enqueueWork(() -> {

            ClientQuestCache.set(msg.markers);

            LOGGER.debug(
                    "[BCN] Cache Updated: "
                            + ClientQuestCache.getMarkers().size()
            );

        });

        ctx.get().setPacketHandled(true);
    }
}