package io.github.FinNank1ng.better_coordinate_navigator.client.gui;

import io.github.FinNank1ng.better_coordinate_navigator.client.gui.WorkflowScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BCNMainScreen extends Screen {

    public BCNMainScreen() {
        super(Component.literal("Better Coordinate Navigator"));
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        int buttonWidth = 180;
        int buttonHeight = 20;
        int buttonX = centerX - buttonWidth / 2;

        int startY = 70;
        int spacing = 28;

        addRenderableWidget(
                Button.builder(
                        Component.literal("地图 / 路线"),
                        button -> minecraft.setScreen(
                                new ComingSoonScreen(this, "地图 / 路线")
                        )
                ).bounds(
                        buttonX,
                        startY,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("标点管理"),
                        button -> minecraft.setScreen(
                                new ComingSoonScreen(this, "标点管理")
                        )
                ).bounds(
                        buttonX,
                        startY + spacing,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("工作流"),
                        button -> minecraft.setScreen(
                                new WorkflowScreen(this)
                        )
                ).bounds(
                        buttonX,
                        startY + spacing * 2,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("追踪"),
                        button -> minecraft.setScreen(
                                new ComingSoonScreen(this, "追踪")
                        )
                ).  bounds(
                        buttonX,
                        startY + spacing * 3,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("设置"),
                        button -> minecraft.setScreen(
                                new ComingSoonScreen(this, "设置")
                        )
                ).bounds(
                        buttonX,
                        startY + spacing * 4,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("关闭"),
                        button -> onClose()
                ).bounds(
                        buttonX,
                        startY + spacing * 5 + 10,
                        buttonWidth,
                        buttonHeight
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
                Component.literal("Better Coordinate Navigator"),
                this.width / 2,
                30,
                0xFFFFFF
        );

        guiGraphics.drawCenteredString(
                this.font,
                Component.literal("BCN"),
                this.width / 2,
                45,
                0xAAAAAA
        );

        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );
    }
}