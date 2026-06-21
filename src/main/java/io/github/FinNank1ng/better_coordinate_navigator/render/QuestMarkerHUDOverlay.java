package io.github.FinNank1ng.better_coordinate_navigator.render;

import com.mojang.logging.LogUtils;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.network.ModPackets;
import io.github.FinNank1ng.better_coordinate_navigator.network.QuestDataRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

public class QuestMarkerHUDOverlay {

    private static final Logger LOGGER =
            LogUtils.getLogger();

    /**
     * 防止每帧发包
     */
    private static boolean requested = false;

    @SubscribeEvent
    public static void onHud(RenderGuiEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        /*
         * 客户端没数据时请求一次
         */
        if (!requested && ClientQuestCache.getMarkers().isEmpty()) {

            requested = true;

            LOGGER.info("[BCN] Requesting Quest Data");

            ModPackets.CHANNEL.sendToServer(
                    new QuestDataRequestPacket()
            );
        }

        GuiGraphics g = event.getGuiGraphics();

    }
}