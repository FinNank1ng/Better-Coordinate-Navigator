package io.github.FinNank1ng.better_coordinate_navigator.network;

import io.github.FinNank1ng.better_coordinate_navigator.data.QuestManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;

import static com.mojang.text2speech.Narrator.LOGGER;

public class QuestSyncHelper {

    public static void syncToPlayer(
            ServerPlayer player,
            QuestManager manager
    ) {

        LOGGER.debug(
                "[BCN] Sync To Player : "
                        + player.getName().getString()
        );

        ModPackets.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),

                new QuestDataUpdatePacket(

                        new ArrayList<>(manager.getMarkers()),

                        manager.getPlayerTrackedMarkers(
                                player.getUUID()
                        )

                )
        );
    }
}