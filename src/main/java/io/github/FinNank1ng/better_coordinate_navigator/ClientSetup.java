package io.github.FinNank1ng.better_coordinate_navigator;


import io.github.FinNank1ng.better_coordinate_navigator.client.IconManager;
import io.github.FinNank1ng.better_coordinate_navigator.render.QuestMarkerHUDOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(
        modid = better_coordinate_navigator.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class ClientSetup {


    @SubscribeEvent
    public static void onClientSetup(
            FMLClientSetupEvent event
    ){


        /*
         * 创建客户端自定义资源文件夹
         */
        IconManager.init();



        /*
         * HUD注册
         */
        MinecraftForge.EVENT_BUS.register(
                QuestMarkerHUDOverlay.class
        );


    }

}