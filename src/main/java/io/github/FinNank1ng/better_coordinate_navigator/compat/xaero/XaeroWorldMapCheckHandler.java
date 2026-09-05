package io.github.FinNank1ng.better_coordinate_navigator.compat.xaero;

import io.github.FinNank1ng.better_coordinate_navigator.client.ConfigLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class XaeroWorldMapCheckHandler {

    private XaeroWorldMapCheckHandler() {
    }

    public static void check() {

        if (!ConfigLoader.CONFIG.checkXaero) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        if (XaeroCompat.isWorldMapLoaded()) {

            mc.player.sendSystemMessage(
                    Component.literal("§6[Better Coordinate Navigator] & [Xaero's World Map]")
            );

            mc.player.sendSystemMessage(
                    Component.literal("§a已检测到 Xaero's World Map")
            );

        } else {

            mc.player.sendSystemMessage(
                    Component.literal("§6[Better Coordinate Navigator] & [Xaero's World Map]")
            );

            mc.player.sendSystemMessage(
                    Component.literal("§7未检测到 Xaero's World Map")
            );

            mc.player.sendSystemMessage(
                    Component.literal("§7BCN 基础导航功能不受影响。")
            );
        }
    }
}