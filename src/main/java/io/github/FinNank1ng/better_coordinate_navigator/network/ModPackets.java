package io.github.FinNank1ng.better_coordinate_navigator.network;
import io.github.FinNank1ng.better_coordinate_navigator.better_coordinate_navigator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;


public class ModPackets {

    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(
                            better_coordinate_navigator.MODID,
                            "main"
                    ),
                    () -> VERSION,
                    VERSION::equals,
                    VERSION::equals
            );

    public static void registerPackets() {

        int id = 0;

        CHANNEL.registerMessage(
                id++,
                QuestDataRequestPacket.class,
                QuestDataRequestPacket::encode,
                QuestDataRequestPacket::new,
                QuestDataRequestPacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                QuestDataUpdatePacket.class,
                QuestDataUpdatePacket::encode,
                QuestDataUpdatePacket::new,
                QuestDataUpdatePacket::handle
        );
    }
}