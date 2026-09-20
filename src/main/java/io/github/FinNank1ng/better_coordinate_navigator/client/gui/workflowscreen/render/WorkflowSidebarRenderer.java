package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render;

import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.WorkflowScreenState;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.Workflow;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

/*
 * 工作流侧边栏渲染器
 */
public class WorkflowSidebarRenderer {

    private static final int COLOR_SIDEBAR = 0xFF101720;
    private static final int COLOR_PANEL = 0xFF141C26;
    private static final int COLOR_PANEL_ALT = 0xFF17212D;

    private static final int COLOR_NODE_HOVER = 0xFF1D3043;
    private static final int COLOR_NODE_SELECTED = 0xFF203B55;

    private static final int COLOR_BORDER = 0xFF283747;
    private static final int COLOR_ACCENT = 0xFF67B8FF;

    private static final int COLOR_TEXT = 0xFFE8EEF5;
    private static final int COLOR_TEXT_SECONDARY = 0xFFA8B3BF;
    private static final int COLOR_TEXT_MUTED = 0xFF6E7C8B;
    private static final int COLOR_WARNING = 0xFFFFC66D;

    private static final int HEADER_HEIGHT = 52;
    private static final int FOOTER_HEIGHT = 34;

    private static final int SIDEBAR_EXPANDED_WIDTH = 158;
    private static final int SIDEBAR_COLLAPSED_WIDTH = 42;

    private static final int WORKFLOW_ITEM_HEIGHT = 52;

    private final WorkflowScreenState state;
    private final Font font;

    public WorkflowSidebarRenderer(
            WorkflowScreenState state,
            Font font
    ) {

        this.state = state;
        this.font = font;
    }

    /*
     * 绘制整个侧边栏
     */
    public void render(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight,
            int mouseX,
            int mouseY,
            List<Workflow> visibleWorkflows
    ) {

        int sidebarWidth =
                getSidebarWidth();

        graphics.fill(
                0,
                HEADER_HEIGHT,
                sidebarWidth,
                screenHeight - FOOTER_HEIGHT,
                COLOR_SIDEBAR
        );

        graphics.fill(
                sidebarWidth - 1,
                HEADER_HEIGHT,
                sidebarWidth,
                screenHeight - FOOTER_HEIGHT,
                COLOR_BORDER
        );

        drawCollapseButton(
                graphics,
                mouseX,
                mouseY
        );

        if (state.isSidebarCollapsed()) {

            drawCollapsedWorkflowList(
                    graphics,
                    screenHeight,
                    mouseX,
                    mouseY,
                    visibleWorkflows
            );

            return;
        }

        graphics.drawString(
                font,
                Component.literal("工作流"),
                14,
                HEADER_HEIGHT + 13,
                COLOR_TEXT
        );

        graphics.drawString(
                font,
                Component.literal(
                        state.getWorkflows().size()
                                + " 个"
                ),
                76,
                HEADER_HEIGHT + 13,
                COLOR_TEXT_MUTED
        );

        drawWorkflowList(
                graphics,
                HEADER_HEIGHT + 52,
                screenHeight - FOOTER_HEIGHT - 48,
                mouseX,
                mouseY,
                visibleWorkflows
        );

        drawSidebarNewButton(
                graphics,
                screenHeight,
                mouseX,
                mouseY
        );
    }

    /*
     * 折叠按钮
     */
    private void drawCollapseButton(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {

        int x = 8;

        int y =
                HEADER_HEIGHT + 8;

        int buttonWidth =
                state.isSidebarCollapsed()
                        ? 26
                        : 30;

        boolean hovered =
                inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        buttonWidth,
                        28
                );

        drawPanelButton(
                graphics,
                x,
                y,
                buttonWidth,
                28,
                state.isSidebarCollapsed()
                        ? ">"
                        : "<",
                hovered,
                COLOR_TEXT
        );
    }

    /*
     * 工作流列表
     */
    private void drawWorkflowList(
            GuiGraphics graphics,
            int listTop,
            int listBottom,
            int mouseX,
            int mouseY,
            List<Workflow> visibleWorkflows
    ) {

        int contentHeight =
                visibleWorkflows.size()
                        * WORKFLOW_ITEM_HEIGHT;

        int viewportHeight =
                listBottom - listTop;

        double maxScroll =
                Math.max(
                        0,
                        contentHeight - viewportHeight
                );

        double sidebarScroll =
                clamp(
                        state.getSidebarScroll(),
                        0,
                        maxScroll
                );

        state.setSidebarScroll(
                sidebarScroll
        );

        int y =
                listTop
                        - (int) sidebarScroll;

        for (Workflow workflow :
                visibleWorkflows) {

            if (y + WORKFLOW_ITEM_HEIGHT >= listTop
                    && y <= listBottom) {

                boolean selected =
                        workflow == state.getCurrentWorkflow();

                boolean hovered =
                        inside(
                                mouseX,
                                mouseY,
                                8,
                                y,
                                SIDEBAR_EXPANDED_WIDTH - 16,
                                WORKFLOW_ITEM_HEIGHT - 4
                        );

                drawWorkflowItem(
                        graphics,
                        workflow,
                        y,
                        selected,
                        hovered
                );
            }

            y += WORKFLOW_ITEM_HEIGHT;
        }
    }

    /*
     * 折叠后的工作流列表
     */
    private void drawCollapsedWorkflowList(
            GuiGraphics graphics,
            int screenHeight,
            int mouseX,
            int mouseY,
            List<Workflow> visibleWorkflows
    ) {

        int y =
                HEADER_HEIGHT + 50;

        for (Workflow workflow :
                visibleWorkflows) {

            if (y + 30
                    >= screenHeight - FOOTER_HEIGHT) {
                break;
            }

            boolean selected =
                    workflow == state.getCurrentWorkflow();

            boolean hovered =
                    inside(
                            mouseX,
                            mouseY,
                            6,
                            y,
                            30,
                            30
                    );

            int fill =
                    selected
                            ? COLOR_NODE_SELECTED
                            : hovered
                            ? COLOR_NODE_HOVER
                            : COLOR_PANEL_ALT;

            graphics.fill(
                    6,
                    y,
                    36,
                    y + 30,
                    fill
            );

            graphics.drawString(
                    font,
                    Component.literal(
                            selected
                                    ? "●"
                                    : "○"
                    ),
                    16,
                    y + 10,
                    selected
                            ? COLOR_ACCENT
                            : COLOR_TEXT_MUTED
            );

            y += 38;
        }
    }

    /*
     * 工作流条目
     */
    private void drawWorkflowItem(
            GuiGraphics graphics,
            Workflow workflow,
            int y,
            boolean selected,
            boolean hovered
    ) {

        int x = 8;

        int itemWidth =
                SIDEBAR_EXPANDED_WIDTH - 16;

        int itemHeight =
                WORKFLOW_ITEM_HEIGHT - 4;

        int fill =
                selected
                        ? COLOR_NODE_SELECTED
                        : hovered
                        ? COLOR_NODE_HOVER
                        : COLOR_PANEL;

        int border =
                selected
                        ? COLOR_ACCENT
                        : COLOR_BORDER;

        graphics.fill(
                x,
                y,
                x + itemWidth,
                y + itemHeight,
                fill
        );

        graphics.fill(
                x,
                y,
                x + 3,
                y + itemHeight,
                border
        );

        graphics.drawString(
                font,
                Component.literal(
                        selected
                                ? "●"
                                : "○"
                ),
                x + 10,
                y + 12,
                selected
                        ? COLOR_ACCENT
                        : COLOR_TEXT_MUTED
        );

        String name =
                workflow.getName();

        if (name.length() > 19) {

            name =
                    name.substring(
                            0,
                            19
                    ) + "...";
        }

        graphics.drawString(
                font,
                Component.literal(name),
                x + 28,
                y + 9,
                selected
                        ? COLOR_TEXT
                        : COLOR_TEXT_SECONDARY
        );

        graphics.drawString(
                font,
                Component.literal(
                        workflow.getStepCount()
                                + " 个步骤"
                ),
                x + 28,
                y + 28,
                COLOR_TEXT_MUTED
        );

        if (hovered) {

            int pinX =
                    x + itemWidth - 44;

            int moreX =
                    x + itemWidth - 22;

            graphics.drawString(
                    font,
                    Component.literal(
                            state.getPinnedWorkflows().contains(
                                    workflow.getId()
                            )
                                    ? "●"
                                    : "○"
                    ),
                    pinX,
                    y + 17,
                    COLOR_WARNING
            );

            graphics.drawString(
                    font,
                    Component.literal("⋯"),
                    moreX,
                    y + 14,
                    COLOR_TEXT
            );
        }
    }

    /*
     * 新建工作流按钮
     */
    private void drawSidebarNewButton(
            GuiGraphics graphics,
            int screenHeight,
            int mouseX,
            int mouseY
    ) {

        int x = 8;

        int y =
                screenHeight
                        - FOOTER_HEIGHT
                        - 36;

        int buttonWidth =
                SIDEBAR_EXPANDED_WIDTH - 16;

        boolean hovered =
                inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        buttonWidth,
                        22
                );

        drawPanelButton(
                graphics,
                x,
                y,
                buttonWidth,
                22,
                "+ 新建工作流",
                hovered,
                COLOR_ACCENT
        );
    }

    private int getSidebarWidth() {

        return state.isSidebarCollapsed()
                ? SIDEBAR_COLLAPSED_WIDTH
                : SIDEBAR_EXPANDED_WIDTH;
    }

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

    /*
     * 绘制侧边栏按钮
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
                x + (width - textWidth) / 2,
                y + (height - 8) / 2,
                hovered
                        ? accent
                        : COLOR_TEXT
        );
    }
}