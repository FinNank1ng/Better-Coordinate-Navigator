package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render;

import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.WorkflowScreenState;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.layout.WorkflowFrameLayout;
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
                WorkflowFrameLayout.sidebarWidth(
                        state.isSidebarCollapsed()
                );

        graphics.fill(
                0,
                WorkflowFrameLayout.HEADER_HEIGHT,
                sidebarWidth,
                screenHeight - WorkflowFrameLayout.FOOTER_HEIGHT,
                COLOR_SIDEBAR
        );

        graphics.fill(
                sidebarWidth - 1,
                WorkflowFrameLayout.HEADER_HEIGHT,
                sidebarWidth,
                screenHeight - WorkflowFrameLayout.FOOTER_HEIGHT,
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
                WorkflowFrameLayout.SIDEBAR_HEADER_X,
                WorkflowFrameLayout.sidebarHeaderY(),
                COLOR_TEXT
        );

        graphics.drawString(
                font,
                Component.literal(
                        state.getWorkflows().size()
                                + " 个"
                ),
                WorkflowFrameLayout.SIDEBAR_COUNT_X,
                WorkflowFrameLayout.sidebarHeaderY(),
                COLOR_TEXT_MUTED
        );

        drawSearchField(
                graphics
        );

        drawWorkflowList(
                graphics,
                WorkflowFrameLayout.workflowListTop(),
                WorkflowFrameLayout.workflowListBottom(
                        screenHeight
                ),
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
     * 搜索框背景
     *
     * 真正可输入的 EditBox 由 WorkflowScreen 管理
     * 这里仅负责统一绘制其外框
     */
    private void drawSearchField(
            GuiGraphics graphics
    ) {

        int x =
                WorkflowFrameLayout.SIDEBAR_SEARCH_X;

        int y =
                WorkflowFrameLayout.sidebarSearchY();

        int width =
                WorkflowFrameLayout.SIDEBAR_SEARCH_WIDTH;

        int height =
                WorkflowFrameLayout.SIDEBAR_SEARCH_HEIGHT;

        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                0xFF0C141D
        );

        graphics.fill(
                x,
                y,
                x + width,
                y + 1,
                COLOR_BORDER
        );

        graphics.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                COLOR_BORDER
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + height,
                COLOR_BORDER
        );

        graphics.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                COLOR_BORDER
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

        int x =
                WorkflowFrameLayout.SIDEBAR_COLLAPSE_X;

        int y =
                WorkflowFrameLayout.sidebarCollapseY();

        int buttonWidth = 26;

        boolean hovered =
                inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        buttonWidth,
                        WorkflowFrameLayout.SIDEBAR_COLLAPSE_HEIGHT
                );

        drawPanelButton(
                graphics,
                x,
                y,
                buttonWidth,
                WorkflowFrameLayout.SIDEBAR_COLLAPSE_HEIGHT,
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
                        * WorkflowFrameLayout.WORKFLOW_ITEM_HEIGHT;

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

        /*
         * 只允许工作流卡片绘制在列表区域内部
         */
        graphics.enableScissor(
                0,
                listTop,
                WorkflowFrameLayout.SIDEBAR_EXPANDED_WIDTH,
                listBottom
        );

        int y =
                listTop
                        - (int) sidebarScroll;

        for (Workflow workflow :
                visibleWorkflows) {

            int itemHeight =
                    WorkflowFrameLayout.WORKFLOW_ITEM_HEIGHT - 4;

            if (y + WorkflowFrameLayout.WORKFLOW_ITEM_HEIGHT >= listTop
                    && y <= listBottom) {

                boolean selected =
                        workflow == state.getCurrentWorkflow();

                boolean hovered =
                        inside(
                                mouseX,
                                mouseY,
                                WorkflowFrameLayout.SIDEBAR_ITEM_HORIZONTAL_PADDING,
                                y,
                                WorkflowFrameLayout.SIDEBAR_EXPANDED_WIDTH - 16,
                                itemHeight
                        );

                drawWorkflowItem(
                        graphics,
                        workflow,
                        y,
                        selected,
                        hovered
                );
            }

            y += WorkflowFrameLayout.WORKFLOW_ITEM_HEIGHT;
        }

        /*
         * 恢复正常绘制区域
         */
        graphics.disableScissor();
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
                WorkflowFrameLayout.HEADER_HEIGHT + 50;

        for (Workflow workflow :
                visibleWorkflows) {

            if (y + 30
                    >= screenHeight
                    - WorkflowFrameLayout.FOOTER_HEIGHT) {
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

        int x =
                WorkflowFrameLayout.SIDEBAR_ITEM_HORIZONTAL_PADDING;

        int itemWidth =
                WorkflowFrameLayout.SIDEBAR_EXPANDED_WIDTH - 16;

        int itemHeight =
                WorkflowFrameLayout.WORKFLOW_ITEM_HEIGHT - 4;

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
                y + 10,
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
                y + 7,
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
                y + 25,
                COLOR_TEXT_MUTED
        );

        if (hovered) {

            int pinX =
                    x + itemWidth - 44;

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
                    y + 14,
                    COLOR_WARNING
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

        int x =
                WorkflowFrameLayout.SIDEBAR_ITEM_HORIZONTAL_PADDING;

        int y =
                WorkflowFrameLayout.sidebarNewButtonY(
                        screenHeight
                );

        int buttonWidth =
                WorkflowFrameLayout.SIDEBAR_EXPANDED_WIDTH - 16;

        boolean hovered =
                inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        buttonWidth,
                        WorkflowFrameLayout.SIDEBAR_NEW_BUTTON_HEIGHT
                );

        drawPanelButton(
                graphics,
                x,
                y,
                buttonWidth,
                WorkflowFrameLayout.SIDEBAR_NEW_BUTTON_HEIGHT,
                "+ 新建工作流",
                hovered,
                COLOR_ACCENT
        );
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
