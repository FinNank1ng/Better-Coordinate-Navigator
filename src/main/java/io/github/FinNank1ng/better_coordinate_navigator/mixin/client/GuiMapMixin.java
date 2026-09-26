package io.github.FinNank1ng.better_coordinate_navigator.mixin.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.GuiMap;

@Mixin(GuiMap.class)
public abstract class GuiMapMixin {

    /*
     * Xaero 原生按钮
     */

    @Shadow
    private Button settingsButton;

    @Shadow
    private Button exportButton;

    @Shadow
    private Button waypointsButton;

    @Shadow
    private Button renderWaypointsButton;

    @Shadow
    private Button playersButton;

    @Shadow
    private Button radarButton;

    @Shadow
    private Button claimsButton;

    @Shadow
    private Button zoomInButton;

    @Shadow
    private Button zoomOutButton;

    @Shadow
    private Button keybindingsButton;

    @Shadow
    private Button caveModeButton;

    @Shadow
    private Button dimensionToggleButton;

    @Shadow
    private Button hopButton;

    @Shadow
    private Button attachedCameraButton;

    /*
     * BCN 状态
     */

    /**
     * 是否处于 BCN 道路编辑模式。
     */
    private boolean bcnRoadEditor = false;

    /**
     * BCN 主按钮。
     */
    private Button bcnButton;

    /**
     * BCN 退出编辑按钮。
     */
    private Button bcnExitButton;

    /*
     * GuiMap 初始化
     */

    @Inject(method = "init", at = @At("TAIL"))
    private void betterCoordinateNavigator$init(CallbackInfo ci) {
        System.out.println("[BCN] GuiMapMixin init injected!");

        GuiMap self = (GuiMap) (Object) this;

        /*
         * BCN 主按钮
         */

        bcnButton = Button.builder(
                Component.literal("BCN道路编辑"),
                button -> {

                    bcnRoadEditor = true;

                    /*
                     * 进入 BCN 编辑模式时
                     */
                    self.closeRightClick();

                    updateBcnUI();
                }
        ).bounds(5, 40, 60, 20).build();

        /*
         * BCN 退出编辑按钮
         */

        bcnExitButton = Button.builder(
                Component.literal("退出编辑"),
                button -> {

                    bcnRoadEditor = false;

                    updateBcnUI();
                }
        ).bounds(5, 40, 60, 20).build();

        /*
         * 加入 Xaero Gui
         */

        self.addRenderableWidget(bcnButton);
        self.addRenderableWidget(bcnExitButton);

        /*
         * 根据当前模式初始化 UI
         */

        updateBcnUI();
    }

    /*
     * 鼠标点击监听
     */

    @Inject(
            method = "mouseClicked",
            at = @At("HEAD")
    )
    private void betterCoordinateNavigator$mouseClicked(
            double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!bcnRoadEditor) {
            return;
        }

        System.out.println(
                "[BCN] mouseClicked"
                        + " editor=" + bcnRoadEditor
                        + " x=" + mouseX
                        + " y=" + mouseY
                        + " button=" + button
        );
    }


    /*
     * BCN UI 状态更新
     */

    private void updateBcnUI() {
        if (bcnButton == null || bcnExitButton == null) {
            return;
        }

        /*
         * 普通模式
         */

        if (!bcnRoadEditor) {

            bcnButton.visible = true;
            bcnExitButton.visible = false;

            setXaeroButtonsVisible(true);

            return;
        }

        /*
         * BCN 道路编辑模式
         */

        bcnButton.visible = false;
        bcnExitButton.visible = true;

        setXaeroButtonsVisible(false);
    }

    /*
     * 控制 Xaero 原生 UI
     */

    private void setXaeroButtonsVisible(boolean visible) {

        if (settingsButton != null) {
            settingsButton.visible = visible;
        }

        if (exportButton != null) {
            exportButton.visible = visible;
        }

        if (waypointsButton != null) {
            waypointsButton.visible = visible;
        }

        if (renderWaypointsButton != null) {
            renderWaypointsButton.visible = visible;
        }

        if (playersButton != null) {
            playersButton.visible = visible;
        }

        if (radarButton != null) {
            radarButton.visible = visible;
        }

        if (claimsButton != null) {
            claimsButton.visible = visible;
        }

        if (zoomInButton != null) {
            zoomInButton.visible = visible;
        }

        if (zoomOutButton != null) {
            zoomOutButton.visible = visible;
        }

        if (keybindingsButton != null) {
            keybindingsButton.visible = visible;
        }

        if (caveModeButton != null) {
            caveModeButton.visible = visible;
        }

        if (dimensionToggleButton != null) {
            dimensionToggleButton.visible = visible;
        }

        if (hopButton != null) {
            hopButton.visible = visible;
        }

        if (attachedCameraButton != null) {
            attachedCameraButton.visible = visible;
        }
    }
}

