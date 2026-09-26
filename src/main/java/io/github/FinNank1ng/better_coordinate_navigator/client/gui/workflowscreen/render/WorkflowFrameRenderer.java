package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render;

import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.WorkflowScreenState;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientWorkflowPermission;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.Workflow;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/*
 * 工作流界面框架渲染器
 */
public class WorkflowFrameRenderer {

    private static final int COLOR_HEADER = 0xFF101720;
    private static final int COLOR_PANEL = 0xFF141C26;
    private static final int COLOR_PANEL_ALT = 0xFF17212D;

    private static final int COLOR_NODE_HOVER = 0xFF1D3043;

    private static final int COLOR_BORDER = 0xFF283747;
    private static final int COLOR_ACCENT = 0xFF67B8FF;

    private static final int COLOR_TEXT = 0xFFE8EEF5;
    private static final int COLOR_TEXT_SECONDARY = 0xFFA8B3BF;
    private static final int COLOR_TEXT_MUTED = 0xFF6E7C8B;
    private static final int COLOR_SUCCESS = 0xFF72D69A;

    private static final int COLOR_DISABLED = 0xFF4E5A66;
    private static final int COLOR_DISABLED_TEXT = 0xFF687582;

    private static final int HEADER_HEIGHT = 42;
    private static final int FOOTER_HEIGHT = 34;

    private static final int SIDEBAR_EXPANDED_WIDTH = 158;

    private static final int HEADER_SIDE_PADDING = 18;
    private static final int HEADER_BUTTON_WIDTH = 54;
    private static final int HEADER_BUTTON_HEIGHT = 20;

    private static final int TOOLBAR_TOP_OFFSET = 12;

    private final WorkflowScreenState state;
    private final Font font;

    public WorkflowFrameRenderer(
            WorkflowScreenState state,
            Font font
    ) {

        this.state = state;
        this.font = font;
    }

    /*
     * 绘制整个工作流界面框架
     */
    public void render(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight,
            int sidebarWidth,
            int mouseX,
            int mouseY
    ) {

        drawHeader(
                graphics,
                screenWidth,
                mouseX,
                mouseY
        );

        drawCanvasToolbar(
                graphics,
                sidebarWidth,
                mouseX,
                mouseY
        );

        drawFooter(
                graphics,
                screenWidth,
                screenHeight
        );
    }

    /*
     * 顶部标题栏
     */
    private void drawHeader(
            GuiGraphics graphics,
            int screenWidth,
            int mouseX,
            int mouseY
    ) {

        graphics.fill(
                0,
                0,
                screenWidth,
                HEADER_HEIGHT,
                COLOR_HEADER
        );

        graphics.fill(
                0,
                HEADER_HEIGHT - 2,
                screenWidth,
                HEADER_HEIGHT,
                COLOR_BORDER
        );

        /*
         * 标题文字统一垂直居中
         */
        int headerTextY =
                (HEADER_HEIGHT - font.lineHeight) / 2;

        graphics.drawString(
                font,
                Component.literal("BCN"),
                18,
                headerTextY,
                COLOR_TEXT
        );

        graphics.drawString(
                font,
                Component.literal("/"),
                48,
                headerTextY,
                COLOR_TEXT_MUTED
        );

        graphics.drawString(
                font,
                Component.literal("工作流"),
                64,
                headerTextY,
                COLOR_ACCENT
        );

        graphics.drawString(
                font,
                Component.literal("·"),
                116,
                headerTextY,
                COLOR_TEXT_MUTED
        );

        String workflowName =
                state.getCurrentWorkflow() == null
                        ? "未选择工作流"
                        : state.getCurrentWorkflow()
                        .getName();

        if (workflowName.length() > 28) {

            workflowName =
                    workflowName.substring(
                            0,
                            28
                    ) + "...";
        }

        graphics.drawString(
                font,
                Component.literal(
                        workflowName
                ),
                134,
                headerTextY,
                COLOR_TEXT_SECONDARY
        );

        int testX =
                screenWidth
                        - 155;

        int saveX =
                screenWidth
                        - 82;

        int headerButtonY =
                (HEADER_HEIGHT
                        - HEADER_BUTTON_HEIGHT)
                        / 2;

        boolean canManage =
                canManageWorkflows();

        drawHeaderControl(
                graphics,
                testX,
                headerButtonY,
                HEADER_BUTTON_WIDTH,
                HEADER_BUTTON_HEIGHT,
                "测试",
                canManage
                        ? COLOR_ACCENT
                        : COLOR_DISABLED,
                mouseX,
                mouseY,
                canManage
        );

        drawHeaderControl(
                graphics,
                saveX,
                headerButtonY,
                HEADER_BUTTON_WIDTH,
                HEADER_BUTTON_HEIGHT,
                "保存",
                canManage
                        ? COLOR_SUCCESS
                        : COLOR_DISABLED,
                mouseX,
                mouseY,
                canManage
        );
    }

    /*
     * 画布工具栏
     */
    private void drawCanvasToolbar(
            GuiGraphics graphics,
            int sidebarWidth,
            int mouseX,
            int mouseY
    ) {

        int x =
                sidebarWidth
                        + 12;

        int y =
                HEADER_HEIGHT
                        + TOOLBAR_TOP_OFFSET;

        boolean canManage =
                canManageWorkflows();

        drawPanelButton(
                graphics,
                x,
                y,
                44,
                20,
                "+",
                canManage
                        && inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        44,
                        20
                ),
                COLOR_ACCENT,
                canManage
        );

        drawPanelButton(
                graphics,
                x + 70,
                y,
                44,
                20,
                "适应",
                inside(
                        mouseX,
                        mouseY,
                        x + 70,
                        y,
                        52,
                        22
                ),
                COLOR_TEXT,
                true
        );
    }

    /*
     * 底部状态栏
     */
    private void drawFooter(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight
    ) {

        int y =
                screenHeight
                        - FOOTER_HEIGHT;

        graphics.fill(
                0,
                y,
                screenWidth,
                screenHeight,
                COLOR_HEADER
        );

        graphics.fill(
                0,
                y,
                screenWidth,
                y + 1,
                COLOR_BORDER
        );

        graphics.drawString(
                font,
                Component.literal(
                        "● "
                                + state.getStatusText()
                ),
                16,
                y + 10,
                state.getStatusColor()
        );

        Workflow workflow =
                state.getCurrentWorkflow();

        String info =
                workflow == null
                        ? "未选择工作流"
                        : "当前工作流："
                        + workflow.getName()
                        + "    "
                        + workflow.getStepCount()
                        + " 个节点";

        int infoWidth =
                font.width(info);

        graphics.drawString(
                font,
                Component.literal(info),
                Math.max(
                        250,
                        screenWidth / 2
                                - infoWidth / 2
                ),
                y + 10,
                COLOR_TEXT_SECONDARY
        );

        String help =
                "中键平移 · 滚轮缩放 · 拖动节点 · Delete 删除";

        int helpWidth =
                font.width(help);

        graphics.drawString(
                font,
                Component.literal(help),
                screenWidth
                        - helpWidth
                        - 14,
                y + 10,
                COLOR_TEXT_MUTED
        );
    }

    /*
     * 顶部按钮
     */
    private void drawHeaderControl(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            String text,
            int accent,
            int mouseX,
            int mouseY,
            boolean enabled
    ) {

        drawPanelButton(
                graphics,
                x,
                y,
                width,
                height,
                text,
                enabled
                        && inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        width,
                        height
                ),
                accent,
                enabled
        );
    }

    /*
     * 通用按钮
     */
    private void drawPanelButton(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            String text,
            boolean hovered,
            int accent,
            boolean enabled
    ) {

        int fill =
                !enabled
                        ? COLOR_PANEL
                        : hovered
                        ? COLOR_NODE_HOVER
                        : COLOR_PANEL_ALT;

        int border =
                !enabled
                        ? COLOR_DISABLED
                        : hovered
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
                x + (width - textWidth) / 2,
                y + (height - 8) / 2,
                !enabled
                        ? COLOR_DISABLED_TEXT
                        : hovered
                        ? accent
                        : COLOR_TEXT
        );
    }

    /*
     * 判断坐标是否位于区域内
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

    private boolean canManageWorkflows() {

        return ClientWorkflowPermission.canManage();
    }
}