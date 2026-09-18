package io.github.FinNank1ng.better_coordinate_navigator.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ComingSoonScreen extends Screen {

    private final Screen parent;
    private final String featureName;

    public ComingSoonScreen(Screen parent, String featureName) {
        super(Component.literal("Coming Soon"));
        this.parent = parent;
        this.featureName = featureName;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        addRenderableWidget(
                Button.builder(
                        Component.literal("返回"),
                        button -> onClose()
                ).bounds(
                        centerX - 90,
                        this.height / 2 + 50,
                        180,
                        20
                ).build()
        );
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(
                this.font,
                Component.literal("Coming Soon"),
                this.width / 2,
                this.height / 2 - 45,
                0xFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.literal(featureName),
                this.width / 2,
                this.height / 2 - 20,
                0xAAAAAA
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.literal("该功能正在开发中"),
                this.width / 2,
                this.height / 2 + 5,
                0xAAAAAA
        );

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }
}