package io.github.FinNank1ng.better_coordinate_navigator.network;

import io.github.FinNank1ng.better_coordinate_navigator.client.gui.BCNMainScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenBCNMainScreenPacket {

    public OpenBCNMainScreenPacket() {
    }

    public static void encode(
            OpenBCNMainScreenPacket message,
            net.minecraft.network.FriendlyByteBuf buffer
    ) {
    }

    public OpenBCNMainScreenPacket(
            net.minecraft.network.FriendlyByteBuf buffer
    ) {
    }

    public static void handle(
            OpenBCNMainScreenPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(
                    new BCNMainScreen()
            );
        });

        context.setPacketHandled(true);
    }
}