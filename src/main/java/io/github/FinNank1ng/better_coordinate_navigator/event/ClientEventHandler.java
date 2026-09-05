package io.github.FinNank1ng.better_coordinate_navigator.event;

import io.github.FinNank1ng.better_coordinate_navigator.compat.xaero.XaeroWorldMapCheckHandler;
import io.github.FinNank1ng.better_coordinate_navigator.util.VersionCheckHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.mojang.text2speech.Narrator.LOGGER;
import static io.github.FinNank1ng.better_coordinate_navigator.better_coordinate_navigator.MODID;

@Mod.EventBusSubscriber(
        modid = MODID,
        value = Dist.CLIENT
)
public final class ClientEventHandler {

    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onClientLoggingIn(
            ClientPlayerNetworkEvent.LoggingIn event
    ) {

        LOGGER.info("[BCN] Client logging in detected.");

        VersionCheckHandler.checkForUpdate();

        XaeroWorldMapCheckHandler.check();
    }
}