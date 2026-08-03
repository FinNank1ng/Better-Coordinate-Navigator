package io.github.FinNank1ng.better_coordinate_navigator.render;

import io.github.FinNank1ng.better_coordinate_navigator.client.IconManager;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import io.github.FinNank1ng.better_coordinate_navigator.client.ConfigLoader;
import io.github.FinNank1ng.better_coordinate_navigator.client.ClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "better_coordinate_navigator",
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class QuestMarkerHUDRenderer {

    /**
     * 与屏幕边缘保持距离
     */
    private static final int EDGE_MARGIN = 50;


    public static void render(
            GuiGraphics g,
            Minecraft mc
    ) {


        if (ClientQuestCache
                .getMarkers()
                .isEmpty()) {

            return;
        }


        for (QuestMarker marker : ClientQuestCache.getMarkers()) {


            /*
             * 追踪
             */
            if (!ClientQuestCache.isTracked(marker.name)) {
                continue;
            }


            renderMarker(
                    g,
                    mc,
                    marker
            );

        }

    }

    private static void renderMarker(
            GuiGraphics g,
            Minecraft mc,
            QuestMarker marker
    ) {

        double dx = marker.x - mc.player.getX();

        double dy = marker.y - mc.player.getY();

        double dz = marker.z - mc.player.getZ();

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        /*
         * 非激活任务
         */
        if (!marker.active) {
            return;
        }

        /*
         * 超近距离交给世界标点
         */
        if (distance < 16) {
            return;
        }

        /*
         * 目标朝向
         */
        double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0D;

        /*
         * 玩家朝向
         */
        float playerYaw =
                mc.player.getYRot();

        double relativeYaw =
                targetYaw - playerYaw;

        while (relativeYaw > 180) {
            relativeYaw -= 360;
        }

        while (relativeYaw < -180) {
            relativeYaw += 360;
        }

        drawNavigationMarker(
                g,
                mc,
                marker,
                distance,
                relativeYaw,
                dy
        );
    }

    private static void drawNavigationMarker(
            GuiGraphics g,
            Minecraft mc,
            QuestMarker marker,
            double distance,
            double relativeYaw,
            double dy
    ) {

        ClientConfig.HUD config = ConfigLoader.CONFIG.HUD;



        /*
         * 距离限制
         *
         * -1 代表无限距离
         */
        if(config.hideDistance > 0 && distance > config.hideDistance){

            return;

        }


        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;

        int centerY = screenHeight / 2;

        double radians = Math.toRadians(relativeYaw);



        /*
         * 单位方向向量
         */
        double dirX = Math.sin(radians);

        double dirY = -Math.cos(radians);


        double scaleX = (centerX - EDGE_MARGIN) / Math.abs(dirX == 0 ? 0.0001 : dirX);

        double scaleY = (centerY - EDGE_MARGIN) / Math.abs(dirY == 0 ? 0.0001 : dirY);

        double scale = Math.min(scaleX, scaleY);

        int x = centerX + (int)(dirX * scale);

        int y = centerY + (int)(dirY * scale);



        /*
         * 高度差修正
         */
        int heightOffset = (int)Math.max(-30, Math.min(30, dy * 0.4));

        y -= heightOffset;



        /*
         * 自定义图标绘制
         */
        boolean iconDrawn = false;

        if(config.showIcon && marker.iconName != null && !marker.iconName.isEmpty()) {

            var icon = IconManager.getIcon(marker.iconName);

            if(icon != null) {

                IconManager.drawIcon(
                        g,
                        icon,
                        x,
                        y
                );

                iconDrawn = true;

            }

        }



        /*
         * 没有自定义图标
         * 使用默认菱形
         */
        if(!iconDrawn && config.showIcon) {

            String defaultIcon = "◆";

            g.drawString(
                    mc.font,
                    defaultIcon,
                    x,
                    y,
                    0xFFD700,
                    true
            );

        }

        String distanceText = (int)distance + "m";

        String heightText;


        if(dy > 3){

            heightText = "↑" + (int)dy + "m";

        }
        else if(dy < -3){

            heightText = "↓" + (int)Math.abs(dy) + "m";

        }
        else{

            heightText = "≈";

        }




        /*
         * 名字
         */
        if(config.showName){

            g.drawString(
                    mc.font,
                    marker.name,
                    x -
                            mc.font.width(marker.name)
                                    /2,
                    y + 12,
                    0xFFFFFF,
                    true
            );

        }




        /*
         * 距离
         */
        if(config.showDistance){

            g.drawString(
                    mc.font,
                    distanceText,
                    x -
                            mc.font.width(distanceText)
                                    /2,
                    y + 22,
                    0xAAAAAA,
                    true
            );

        }




        /*
         * 高度差
         */
        if(config.showHeight){

            g.drawString(
                    mc.font,
                    heightText,
                    x -
                            mc.font.width(heightText)
                                    /2,
                    y + 32,
                    0x55FF55,
                    true
            );

        }

    }
}