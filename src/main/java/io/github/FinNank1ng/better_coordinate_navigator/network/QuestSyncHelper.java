package io.github.FinNank1ng.better_coordinate_navigator.network;

import io.github.FinNank1ng.better_coordinate_navigator.data.QuestManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import static com.mojang.text2speech.Narrator.LOGGER;

public class QuestSyncHelper {

    public static void syncToPlayer(
            ServerPlayer player,
            QuestManager manager
    ) {

        LOGGER.debug(
                "[BCN] Sync To Player : "
                        + manager.getMarkers().size()
        );

        ModPackets.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new QuestDataUpdatePacket(
                        manager.getMarkers()
                )
        );
    }
}