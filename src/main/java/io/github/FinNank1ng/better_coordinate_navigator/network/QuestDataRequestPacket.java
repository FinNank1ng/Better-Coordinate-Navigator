package io.github.FinNank1ng.better_coordinate_navigator.network;

import io.github.FinNank1ng.better_coordinate_navigator.data.QuestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

import static com.mojang.text2speech.Narrator.LOGGER;
import static io.github.FinNank1ng.better_coordinate_navigator.network.ModPackets.CHANNEL;

public class QuestDataRequestPacket {

    public QuestDataRequestPacket() {
    }

    public QuestDataRequestPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(
            QuestDataRequestPacket msg,
            Supplier<NetworkEvent.Context> ctx
    ) {

        ctx.get().enqueueWork(() -> {

            LOGGER.debug("[BCN] Request Packet Received");

            ServerPlayer player = ctx.get().getSender();

            if (player == null) {
                LOGGER.debug("[BCN] Player NULL");
                return;
            }

            QuestManager manager =
                    QuestManager.get(
                            () -> player.serverLevel().getDataStorage()
                    );
            LOGGER.debug(
                    "[BCN] Manager Hash = "
                            + System.identityHashCode(manager)
            );
            LOGGER.debug(
                    "[BCN] Sending "
                            + manager.getMarkers().size()
                            + " markers"
            );

            CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new QuestDataUpdatePacket(
                            manager.getMarkers()
                    )
            );
        });

        ctx.get().setPacketHandled(true);
    }
}