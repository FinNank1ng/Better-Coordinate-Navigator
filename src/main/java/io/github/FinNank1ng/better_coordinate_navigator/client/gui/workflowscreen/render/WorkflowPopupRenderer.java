package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render;

import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.Workflow;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowAction;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowStep;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

public class WorkflowPopupRenderer {

    private static final int COLOR_PANEL = 0xFF141C26;
    private static final int COLOR_PANEL_ALT = 0xFF17212D;

    private static final int COLOR_NODE = 0xFF182432;
    private static final int COLOR_NODE_HOVER = 0xFF1D3043;

    private static final int COLOR_BORDER = 0xFF283747;
    private static final int COLOR_ACCENT = 0xFF67B8FF;

    private static final int COLOR_TEXT = 0xFFE8EEF5;
    private static final int COLOR_TEXT_SECONDARY = 0xFFA8B3BF;
    private static final int COLOR_TEXT_MUTED = 0xFF6E7C8B;

    private static final int HEADER_HEIGHT = 52;

    private static final int WORKFLOW_ITEM_HEIGHT = 52;
    private static final int SIDEBAR_EXPANDED_WIDTH = 158;

    private static final int NODE_MENU_WIDTH = 150;
    private static final int NODE_MENU_HEIGHT = 132;

    private static final int ACTION_POPUP_WIDTH = 520;
    private static final int ACTION_POPUP_HEIGHT = 360;

    private static final int MARKER_POPUP_WIDTH = 620;
    private static final int MARKER_POPUP_HEIGHT = 520;

    private static final int RENAME_POPUP_WIDTH = 380;
    private static final int RENAME_POPUP_HEIGHT = 150;

    private final Font font;

    private int screenWidth;
    private int screenHeight;
    private int mouseX;
    private int mouseY;

    public WorkflowPopupRenderer(
            Font font
    ) {
        this.font = font;
    }

    /*
     * 更新弹窗渲染上下文
     */
    public void updateContext(
            int screenWidth,
            int screenHeight,
            int mouseX,
            int mouseY
    ) {

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    /*
     * 绘制节点右键菜单
     */
    public void renderNodeMenu(
            GuiGraphics graphics,
            int x,
            int y,
            WorkflowStep step
    ) {

        if (step == null) {
            return;
        }

        drawPanel(
                graphics,
                x,
                y,
                NODE_MENU_WIDTH,
                NODE_MENU_HEIGHT
        );

        drawMenuItem(
                graphics,
                x + 6,
                y + 7,
                NODE_MENU_WIDTH - 12,
                24,
                "设置目标标点",
                inside(
                        mouseX,
                        mouseY,
                        x + 6,
                        y + 7,
                        NODE_MENU_WIDTH - 12,
                        24
                )
        );

        drawMenuItem(
                graphics,
                x + 6,
                y + 34,
                NODE_MENU_WIDTH - 12,
                24,
                "添加动作",
                inside(
                        mouseX,
                        mouseY,
                        x + 6,
                        y + 34,
                        NODE_MENU_WIDTH - 12,
                        24
                )
        );

        drawMenuItem(
                graphics,
                x + 6,
                y + 61,
                NODE_MENU_WIDTH - 12,
                24,
                "复制节点",
                inside(
                        mouseX,
                        mouseY,
                        x + 6,
                        y + 61,
                        NODE_MENU_WIDTH - 12,
                        24
                )
        );

        drawMenuItem(
                graphics,
                x + 6,
                y + 88,
                NODE_MENU_WIDTH - 12,
                24,
                "删除节点",
                inside(
                        mouseX,
                        mouseY,
                        x + 6,
                        y + 88,
                        NODE_MENU_WIDTH - 12,
                        24
                )
        );
    }

    /*
     * 绘制 Action 选择器
     */
    public void renderActionPicker(
            GuiGraphics graphics
    ) {

        drawOverlay(
                graphics
        );

        int x =
                (
                        screenWidth
                                - ACTION_POPUP_WIDTH
                )
                        / 2;

        int y =
                (
                        screenHeight
                                - ACTION_POPUP_HEIGHT
                )
                        / 2;

        drawPanel(
                graphics,
                x,
                y,
                ACTION_POPUP_WIDTH,
                ACTION_POPUP_HEIGHT
        );

        graphics.drawString(
                font,
                Component.literal(
                        "添加动作"
                ),
                x + 20,
                y + 20,
                COLOR_TEXT
        );

        graphics.drawString(
                font,
                Component.literal(
                        "选择一个动作加入当前流程节点"
                ),
                x + 20,
                y + 40,
                COLOR_TEXT_MUTED
        );

        drawActionTypeCard(
                graphics,
                x + 20,
                y + 74,
                ACTION_POPUP_WIDTH - 40,
                "发送消息",
                "向当前玩家发送文本消息",
                WorkflowAction.Type.MESSAGE
        );

        drawActionTypeCard(
                graphics,
                x + 20,
                y + 132,
                ACTION_POPUP_WIDTH - 40,
                "执行命令",
                "执行 Minecraft 或第三方模组命令",
                WorkflowAction.Type.EXECUTE_COMMAND
        );

        drawActionTypeCard(
                graphics,
                x + 20,
                y + 190,
                ACTION_POPUP_WIDTH - 40,
                "发放物品",
                "给予玩家指定数量的物品",
                WorkflowAction.Type.GIVE_ITEM
        );

        drawActionTypeCard(
                graphics,
                x + 20,
                y + 248,
                ACTION_POPUP_WIDTH - 40,
                "播放声音",
                "播放指定 Minecraft 音效",
                WorkflowAction.Type.SOUND
        );

        drawPanelButton(
                graphics,
                x + ACTION_POPUP_WIDTH - 104,
                y + ACTION_POPUP_HEIGHT - 38,
                84,
                26,
                "取消",
                inside(
                        mouseX,
                        mouseY,
                        x + ACTION_POPUP_WIDTH - 104,
                        y + ACTION_POPUP_HEIGHT - 38,
                        84,
                        26
                ),
                COLOR_TEXT
        );
    }

    /*
     * 绘制 Action 类型卡片
     */
    private void drawActionTypeCard(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            String title,
            String description,
            WorkflowAction.Type type
    ) {

        boolean hovered =
                inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        width,
                        48
                );

        drawPanelCard(
                graphics,
                x,
                y,
                width,
                48,
                hovered
                        ? COLOR_NODE_HOVER
                        : COLOR_NODE,
                hovered
                        ? COLOR_ACCENT
                        : COLOR_BORDER
        );

        graphics.drawString(
                font,
                Component.literal(
                        title
                ),
                x + 14,
                y + 9,
                hovered
                        ? COLOR_ACCENT
                        : COLOR_TEXT
        );

        graphics.drawString(
                font,
                Component.literal(
                        description
                ),
                x + 14,
                y + 28,
                COLOR_TEXT_MUTED
        );
    }

    /*
     * 绘制目标标点选择器
     */
    public void renderMarkerPicker(
            GuiGraphics graphics,
            List<QuestMarker> markers,
            double markerScroll
    ) {

        drawOverlay(
                graphics
        );

        int x =
                (
                        screenWidth
                                - MARKER_POPUP_WIDTH
                )
                        / 2;

        int y =
                (
                        screenHeight
                                - MARKER_POPUP_HEIGHT
                )
                        / 2;

        drawPanel(
                graphics,
                x,
                y,
                MARKER_POPUP_WIDTH,
                MARKER_POPUP_HEIGHT
        );

        graphics.drawString(
                font,
                Component.literal(
                        "选择目标标点"
                ),
                x + 20,
                y + 20,
                COLOR_TEXT
        );

        graphics.drawString(
                font,
                Component.literal(
                        "从已有任务点中选择流程目标"
                ),
                x + 20,
                y + 40,
                COLOR_TEXT_MUTED
        );

        int listTop =
                y + 92;

        int listBottom =
                y
                        + MARKER_POPUP_HEIGHT
                        - 46;

        int cardHeight = 58;
        int gap = 8;

        int contentHeight =
                markers.size()
                        * (
                        cardHeight
                                + gap
                );

        int viewportHeight =
                listBottom
                        - listTop;

        double maxScroll =
                Math.max(
                        0,
                        contentHeight
                                - viewportHeight
                );

        markerScroll =
                clamp(
                        markerScroll,
                        0,
                        maxScroll
                );

        int cardY =
                listTop
                        - (int) markerScroll;

        for (QuestMarker marker : markers) {

            if (cardY + cardHeight >= listTop
                    && cardY <= listBottom) {

                boolean hovered =
                        inside(
                                mouseX,
                                mouseY,
                                x + 20,
                                cardY,
                                MARKER_POPUP_WIDTH - 40,
                                cardHeight
                        );

                drawPanelCard(
                        graphics,
                        x + 20,
                        cardY,
                        MARKER_POPUP_WIDTH - 40,
                        cardHeight,
                        hovered
                                ? COLOR_NODE_HOVER
                                : COLOR_NODE,
                        hovered
                                ? COLOR_ACCENT
                                : COLOR_BORDER
                );

                String name =
                        marker.name;

                if (name.length() > 55) {

                    name =
                            name.substring(
                                    0,
                                    55
                            )
                                    + "...";
                }

                graphics.drawString(
                        font,
                        Component.literal(
                                name
                        ),
                        x + 34,
                        cardY + 12,
                        hovered
                                ? COLOR_ACCENT
                                : COLOR_TEXT
                );

                graphics.drawString(
                        font,
                        Component.literal(
                                String.format(
                                        Locale.ROOT,
                                        "X %.1f   Y %.1f   Z %.1f",
                                        marker.x,
                                        marker.y,
                                        marker.z
                                )
                        ),
                        x + 34,
                        cardY + 34,
                        COLOR_TEXT_SECONDARY
                );
            }

            cardY +=
                    cardHeight
                            + gap;
        }

        if (markers.isEmpty()) {

            graphics.drawString(
                    font,
                    Component.literal(
                            "没有找到任务点"
                    ),
                    x + 20,
                    listTop + 24,
                    COLOR_TEXT
            );
        }

        drawPanelButton(
                graphics,
                x + MARKER_POPUP_WIDTH - 110,
                y + MARKER_POPUP_HEIGHT - 38,
                90,
                26,
                "取消",
                inside(
                        mouseX,
                        mouseY,
                        x + MARKER_POPUP_WIDTH - 110,
                        y + MARKER_POPUP_HEIGHT - 38,
                        90,
                        26
                ),
                COLOR_TEXT
        );
    }

    /*
     * 绘制工作流菜单
     */
    public void renderWorkflowMenu(
            GuiGraphics graphics,
            Workflow workflow,
            List<Workflow> visibleWorkflows,
            double sidebarScroll
    ) {

        if (workflow == null) {
            return;
        }

        int itemIndex =
                visibleWorkflows.indexOf(
                        workflow
                );

        if (itemIndex < 0) {
            return;
        }

        int listTop =
                HEADER_HEIGHT + 52;

        int itemY =
                listTop
                        + itemIndex
                        * WORKFLOW_ITEM_HEIGHT
                        - (int) sidebarScroll;

        int menuWidth = 128;
        int menuHeight = 72;

        int menuX =
                SIDEBAR_EXPANDED_WIDTH
                        - menuWidth
                        - 8;

        int menuY =
                Math.max(
                        HEADER_HEIGHT + 8,
                        itemY
                                + WORKFLOW_ITEM_HEIGHT
                );

        drawPanel(
                graphics,
                menuX,
                menuY,
                menuWidth,
                menuHeight
        );

        drawMenuItem(
                graphics,
                menuX + 6,
                menuY + 7,
                menuWidth - 12,
                24,
                "重命名",
                inside(
                        mouseX,
                        mouseY,
                        menuX + 6,
                        menuY + 7,
                        menuWidth - 12,
                        24
                )
        );

        drawMenuItem(
                graphics,
                menuX + 6,
                menuY + 34,
                menuWidth - 12,
                24,
                "删除",
                inside(
                        mouseX,
                        mouseY,
                        menuX + 6,
                        menuY + 34,
                        menuWidth - 12,
                        24
                )
        );
    }

    /*
     * 绘制重命名对话框
     */
    public void renderRenameDialog(
            GuiGraphics graphics,
            EditBox renameBox
    ) {

        drawOverlay(
                graphics
        );

        int x =
                (
                        screenWidth
                                - RENAME_POPUP_WIDTH
                )
                        / 2;

        int y =
                (
                        screenHeight
                                - RENAME_POPUP_HEIGHT
                )
                        / 2;

        drawPanel(
                graphics,
                x,
                y,
                RENAME_POPUP_WIDTH,
                RENAME_POPUP_HEIGHT
        );

        graphics.drawString(
                font,
                Component.literal(
                        "重命名工作流"
                ),
                x + 20,
                y + 22,
                COLOR_TEXT
        );

        drawInputFrame(
                graphics,
                x + 20,
                y + 56,
                RENAME_POPUP_WIDTH - 40,
                28,
                renameBox != null
                        && renameBox.isFocused()
        );

        drawPanelButton(
                graphics,
                x + RENAME_POPUP_WIDTH - 190,
                y + 106,
                76,
                26,
                "取消",
                inside(
                        mouseX,
                        mouseY,
                        x + RENAME_POPUP_WIDTH - 190,
                        y + 106,
                        76,
                        26
                ),
                COLOR_TEXT
        );

        drawPanelButton(
                graphics,
                x + RENAME_POPUP_WIDTH - 104,
                y + 106,
                76,
                26,
                "确定",
                inside(
                        mouseX,
                        mouseY,
                        x + RENAME_POPUP_WIDTH - 104,
                        y + 106,
                        76,
                        26
                ),
                COLOR_ACCENT
        );
    }

    /*
     * 绘制遮罩
     */
    private void drawOverlay(
            GuiGraphics graphics
    ) {

        graphics.fill(
                0,
                0,
                screenWidth,
                screenHeight,
                0x99000000
        );
    }

    /*
     * 绘制通用面板
     */
    private void drawPanel(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height
    ) {

        graphics.fill(
                x + 5,
                y + 5,
                x + width + 5,
                y + height + 5,
                0x66000000
        );

        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                COLOR_PANEL
        );

        graphics.fill(
                x,
                y,
                x + width,
                y + 2,
                COLOR_BORDER
        );

        graphics.fill(
                x,
                y + height - 2,
                x + width,
                y + height,
                COLOR_BORDER
        );

        graphics.fill(
                x,
                y,
                x + 2,
                y + height,
                COLOR_BORDER
        );

        graphics.fill(
                x + width - 2,
                y,
                x + width,
                y + height,
                COLOR_BORDER
        );
    }

    /*
     * 绘制通用卡片
     */
    private void drawPanelCard(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int fill,
            int border
    ) {

        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                fill
        );

        graphics.fill(
                x,
                y,
                x + width,
                y + 1,
                border
        );

        graphics.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                border
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + height,
                border
        );

        graphics.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                border
        );
    }

    /*
     * 绘制通用按钮
     */
    private void drawPanelButton(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            String text,
            boolean hovered,
            int accent
    ) {

        int fill =
                hovered
                        ? COLOR_NODE_HOVER
                        : COLOR_PANEL_ALT;

        int border =
                hovered
                        ? accent
                        : COLOR_BORDER;

        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                fill
        );

        graphics.fill(
                x,
                y,
                x + width,
                y + 1,
                border
        );

        graphics.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                border
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + height,
                border
        );

        graphics.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                border
        );

        int textWidth =
                font.width(text);

        graphics.drawString(
                font,
                Component.literal(text),
                x + (
                        width
                                - textWidth
                ) / 2,
                y + (
                        height
                                - 8
                ) / 2,
                hovered
                        ? accent
                        : COLOR_TEXT
        );
    }

    /*
     * 绘制菜单项
     */
    private void drawMenuItem(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            String text,
            boolean hovered
    ) {

        int fill =
                hovered
                        ? COLOR_NODE_HOVER
                        : COLOR_PANEL;

        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                fill
        );

        graphics.drawString(
                font,
                Component.literal(text),
                x + 10,
                y + 8,
                hovered
                        ? COLOR_ACCENT
                        : COLOR_TEXT
        );
    }

    /*
     * 绘制输入框背景
     */
    private void drawInputFrame(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            boolean focused
    ) {

        int border =
                focused
                        ? COLOR_ACCENT
                        : COLOR_BORDER;

        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                COLOR_PANEL_ALT
        );

        graphics.fill(
                x,
                y,
                x + width,
                y + 1,
                border
        );

        graphics.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                border
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + height,
                border
        );

        graphics.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                border
        );
    }

    /*
     * 判断鼠标是否位于区域内
     */
    private boolean inside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {

        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }

    /*
     * 限制数值范围
     */
    private double clamp(
            double value,
            double min,
            double max
    ) {

        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }
}