package io.github.FinNank1ng.better_coordinate_navigator.render;

import org.slf4j.Logger;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import com.mojang.math.Axis;
import com.mojang.logging.LogUtils;

import io.github.FinNank1ng.better_coordinate_navigator.client.ClientConfig;
import io.github.FinNank1ng.better_coordinate_navigator.client.ConfigLoader;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import net.minecraft.client.renderer.GameRenderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "better_coordinate_navigator",
        value = Dist.CLIENT
)
public class QuestMarkerWorldRenderer {

    private static final Logger LOGGER =
            LogUtils.getLogger();

    @SubscribeEvent
    public static void onRender(
            RenderLevelStageEvent event
    ) {


        ClientConfig.WorldMarker config =
                ConfigLoader.CONFIG.WorldMarker;



        /*
         * 总开关
         */
        if(!config.enabled){

            return;

        }



        if (event.getStage()
                != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {

            return;

        }

        Camera camera = event.getCamera();

        double camX = camera.getPosition().x;

        double camY = camera.getPosition().y;

        double camZ = camera.getPosition().z;

        PoseStack poseStack = event.getPoseStack();

        for (QuestMarker marker : ClientQuestCache.getMarkers()) {

            /*
             * 追踪
             */
            if (!marker.tracked) {

                continue;

            }

            double dx = marker.x - camera.getPosition().x;

            double dy = marker.y - camera.getPosition().y;

            double dz = marker.z - camera.getPosition().z;



            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            /*
             * 最大渲染距离
             *
             * -1 = 无限
             */
            if(config.renderDistance != -1 && distance > config.renderDistance){continue;}

            float time = (System.currentTimeMillis() % 100000) / 1000.0F;

            /*
             * 浮动动画
             */
            double offsetY = 0;

            if(config.floatAnimation){

                offsetY = Math.sin(time * 2.0F) * config.floatHeight;

            }


            poseStack.pushPose();

            poseStack.translate(
                    marker.x - camX,
                    marker.y + config.diamondHeight + offsetY - camY,
                    marker.z - camZ
            );

            /*
             * 标记大小
             */
            poseStack.scale(
                    config.scale,
                    config.scale,
                    config.scale
            );

            /*
             * marker 自转（只影响 diamond）
             */
            poseStack.mulPose(Axis.YP.rotationDegrees(time * config.rotationSpeed));

            /*
             * 实体任务点
             */
            renderDiamond(poseStack, config.glow);

            /*
             * LOD文字
             */

            // 6m以内完全不渲染
            if (distance <= 6.05) {

                poseStack.popPose();

                continue;

            }

            poseStack.pushPose();

            poseStack.translate(0, config.nameHeight, 0);

            /*
             * 抵消 marker 自转
             */
            poseStack.mulPose(Axis.YP.rotationDegrees(-time * config.rotationSpeed));

            /*
             * 面向玩家
             */
            poseStack.mulPose(camera.rotation());

            poseStack.scale(
                    -0.03F,
                    -0.03F,
                    0.03F
            );


            Font font =
                    Minecraft.getInstance()
                            .font;

            String text = "";


            if(config.showName){

                text += marker.name;

            }


            if(config.showDistance){

                if(!text.isEmpty()){

                    text += " ";

                }

                text += "[" + (int)distance + "m]";

            }




            /*
             * 没有文字则不渲染
             */
            if(text.isEmpty()){

                poseStack.popPose();
                poseStack.popPose();

                continue;
            }

            float width = font.width(text) / 2f;
            /*
             * 6 ~ 12m 渐显
             */
            float alphaFactor = (float) ((distance - 6.0) / 6.0);

            // clamp
            alphaFactor = Math.max(0.0F, Math.min(1.0F, alphaFactor));

            int alpha = (int) (alphaFactor * 255);
            int color = (alpha << 24) | 0xFFFF00;

            font.drawInBatch(
                    text,
                    -width,
                    0,
                    color,
                    false,
                    poseStack.last().pose(),
                    Minecraft.getInstance().renderBuffers().bufferSource(),
                    Font.DisplayMode.NORMAL,
                    0,
                    15728880
            );

            Minecraft.getInstance()
                    .renderBuffers()
                    .bufferSource()
                    .endBatch();

            poseStack.popPose();

            poseStack.popPose();
        }
    }

    private static void renderDiamond(PoseStack poseStack, boolean glow) {

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.getBuilder();

        float time = (System.currentTimeMillis() % 2000L) / 2000.0f;
        float pulse = 0.55f + 0.45f * (float)Math.sin(time * Math.PI * 2.0);

        Matrix4f matrix = poseStack.last().pose();

        float top = 1.55F;
        float bottom = -1.55F;

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);



        /*
         * GLOW SHELL
         */
        if(glow){

            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();

            buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

            float glowScale = 1.25F;

            addDiamond(buffer, matrix,
                    top * glowScale,
                    bottom * glowScale,
                    glowScale * glowScale,
                    255, 170, 40, (int) (90 * pulse)
            );

            tess.end();

        }


        /*
         * CORE
         */
        RenderSystem.enableDepthTest();

        RenderSystem.depthMask(true);

        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        addDiamond(buffer, matrix,
                top,
                bottom,
                1.25F, 255, 220, 90, 220
        );

        tess.end();

        RenderSystem.enableCull();

        RenderSystem.disableBlend();

    }
    private static void addDiamond(
            BufferBuilder buffer,
            Matrix4f matrix,
            float top,
            float bottom,
            float scale,
            int r,
            int g,
            int b,
            int a
    ) {


        float s = scale;



        // top
        addTri(
                buffer,
                matrix,
                r,g,b,a,
                0, top, 0,
                0, 0, -s,
                s, 0, 0
        );


        addTri(
                buffer,
                matrix,
                r,g,b,a,
                0, top, 0,
                s, 0, 0,
                0, 0, s
        );


        addTri(
                buffer,
                matrix,
                r,g,b,a,
                0, top, 0,
                0, 0, s,
                -s, 0, 0
        );


        addTri(
                buffer,
                matrix,
                r,g,b,a,
                0, top, 0,
                -s, 0, 0,
                0, 0, -s
        );



        // bottom
        addTri(
                buffer,
                matrix,
                r,g,b,a,
                0, bottom, 0,
                s, 0, 0,
                0, 0, -s
        );


        addTri(
                buffer,
                matrix,
                r,g,b,a,
                0, bottom, 0,
                0, 0, s,
                s, 0, 0
        );


        addTri(
                buffer,
                matrix,
                r,g,b,a,
                0, bottom, 0,
                -s, 0, 0,
                0, 0, s
        );


        addTri(
                buffer,
                matrix,
                r,g,b,a,
                0, bottom, 0,
                0, 0, -s,
                -s, 0, 0
        );

    }


    private static void addTri(
            BufferBuilder buffer,
            Matrix4f matrix,
            int r, int g, int b, int a,
            float x1,float y1,float z1,
            float x2,float y2,float z2,
            float x3,float y3,float z3
    ) {
        buffer.vertex(matrix, x1, y1, z1).color(r,g,b,a).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r,g,b,a).endVertex();
        buffer.vertex(matrix, x3, y3, z3).color(r,g,b,a).endVertex();
    }
}