package io.github.FinNank1ng.better_coordinate_navigator.client.gui;

import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.Workflow;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowAction;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowStep;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class WorkflowScreen extends Screen {

    private static final int COLOR_BACKGROUND = 0xFF0B1118;
    private static final int COLOR_HEADER = 0xFF101720;
    private static final int COLOR_SIDEBAR = 0xFF101720;
    private static final int COLOR_PANEL = 0xFF141C26;
    private static final int COLOR_PANEL_ALT = 0xFF17212D;

    private static final int COLOR_NODE = 0xFF182432;
    private static final int COLOR_NODE_HOVER = 0xFF1D3043;
    private static final int COLOR_NODE_SELECTED = 0xFF203B55;

    private static final int COLOR_BORDER = 0xFF283747;
    private static final int COLOR_BORDER_HOVER = 0xFF3E5F7D;
    private static final int COLOR_ACCENT = 0xFF67B8FF;

    private static final int COLOR_TEXT = 0xFFE8EEF5;
    private static final int COLOR_TEXT_SECONDARY = 0xFFA8B3BF;
    private static final int COLOR_TEXT_MUTED = 0xFF6E7C8B;

    private static final int COLOR_SUCCESS = 0xFF72D69A;
    private static final int COLOR_WARNING = 0xFFFFC66D;
    private static final int COLOR_DANGER = 0xFFFF7474;

    private static final int HEADER_HEIGHT = 52;
    private static final int FOOTER_HEIGHT = 34;

    private static final int SIDEBAR_EXPANDED_WIDTH = 158;
    private static final int SIDEBAR_COLLAPSED_WIDTH = 42;

    private static final int WORKFLOW_ITEM_HEIGHT = 52;

    private static final int NODE_WIDTH = 230;
    private static final int NODE_HEIGHT = 150;

    private static final double MIN_ZOOM = 0.45D;
    private static final double MAX_ZOOM = 1.80D;

    private static final long NODE_TOOLTIP_DELAY = 700L;

    private static final int NODE_MENU_WIDTH = 150;
    private static final int NODE_MENU_HEIGHT = 132;

    private static final int ACTION_POPUP_WIDTH = 520;
    private static final int ACTION_POPUP_HEIGHT = 360;

    private static final int MARKER_POPUP_WIDTH = 620;
    private static final int MARKER_POPUP_HEIGHT = 520;

    private static final int RENAME_POPUP_WIDTH = 380;
    private static final int RENAME_POPUP_HEIGHT = 150;

    /*
     * GUI 渲染层级
     */
    private static final float Z_CANVAS = 0.0F;
    private static final float Z_CANVAS_FLOATING = 100.0F;
    private static final float Z_FIXED_UI = 200.0F;
    private static final float Z_MODAL = 300.0F;
    private static final float Z_WIDGET = 400.0F;

    private final Screen parent;

    private final List<Workflow> workflows = new ArrayList<>();
    private final Set<UUID> pinnedWorkflows = new HashSet<>();
    private final Map<UUID, NodePosition> nodePositions = new HashMap<>();

    private Workflow currentWorkflow;

    private int selectedWorkflowIndex = -1;
    private UUID selectedStepId;

    private boolean sidebarCollapsed = false;

    private boolean markerPickerOpen = false;
    private boolean actionPickerOpen = false;
    private boolean workflowMenuOpen = false;
    private boolean nodeMenuOpen = false;
    private boolean renameDialogOpen = false;

    private UUID replacingMarkerStepId;
    private UUID actionPickerStepId;
    private UUID workflowMenuWorkflowId;
    private UUID nodeMenuStepId;

    private int nodeMenuX;
    private int nodeMenuY;

    private UUID hoveredNodeId;
    private long hoveredNodeStartTime;

    private double sidebarScroll = 0.0D;
    private double markerScroll = 0.0D;

    private double zoom = 1.0D;

    private double panX = 0.0D;
    private double panY = 0.0D;

    private boolean panning = false;

    private int panStartMouseX;
    private int panStartMouseY;

    private double panStartX;
    private double panStartY;

    private UUID draggingNodeId;

    private double dragOffsetX;
    private double dragOffsetY;

    private boolean dirty = false;

    private String statusText = "已保存";
    private int statusColor = COLOR_SUCCESS;

    private int mouseX;
    private int mouseY;

    private EditBox workflowSearchBox;
    private EditBox markerSearchBox;
    private EditBox renameBox;

    public WorkflowScreen(Screen parent) {
        super(Component.literal("BCN / 工作流"));

        this.parent = parent;

        createInitialWorkflow();
    }

    /*
     * 创建初始工作流
     */
    private void createInitialWorkflow() {

        Workflow workflow =
                Workflow.create(
                        "新手引导流程"
                );

        List<QuestMarker> markers =
                ClientQuestCache.getMarkers();

        int index = 0;

        for (QuestMarker marker : markers) {

            if (index >= 3) {
                break;
            }

            WorkflowStep step =
                    WorkflowStep.create(
                            marker.getId(),
                            3.0D
                    );

            if (index == 0) {

                step.addAction(
                        WorkflowAction.message(
                                "欢迎来到目标区域"
                        )
                );

            } else {

                step.addAction(
                        WorkflowAction.executeCommand(
                                "say 玩家已抵达目标点"
                        )
                );
            }

            workflow.addStep(step);

            nodePositions.put(
                    step.getId(),
                    new NodePosition(
                            140,
                            120 + index * 190
                    )
            );

            index++;
        }

        workflows.add(workflow);

        currentWorkflow = workflow;

        selectedWorkflowIndex = 0;

        if (!workflow.isEmpty()) {

            selectedStepId =
                    workflow
                            .getStep(0)
                            .getId();
        }
    }

    @Override
    protected void init() {

        super.init();

        buildEditors();

        updateEditorBounds();

        ensureCurrentWorkflowNodePositions();
    }

    /*
     * 创建编辑器输入框
     */
    private void buildEditors() {

        clearWidgets();

        workflowSearchBox =
                addRenderableWidget(
                        new EditBox(
                                font,
                                0,
                                0,
                                180,
                                24,
                                Component.literal(
                                        "搜索工作流"
                                )
                        )
                );

        workflowSearchBox.setMaxLength(80);
        workflowSearchBox.setBordered(false);
        workflowSearchBox.setTextColor(
                COLOR_TEXT
        );

        markerSearchBox =
                addRenderableWidget(
                        new EditBox(
                                font,
                                0,
                                0,
                                300,
                                24,
                                Component.literal(
                                        "搜索任务点"
                                )
                        )
                );

        markerSearchBox.setMaxLength(128);
        markerSearchBox.setBordered(false);
        markerSearchBox.setTextColor(
                COLOR_TEXT
        );

        markerSearchBox.visible = false;

        renameBox =
                addRenderableWidget(
                        new EditBox(
                                font,
                                0,
                                0,
                                260,
                                26,
                                Component.literal(
                                        "工作流名称"
                                )
                        )
                );

        renameBox.setMaxLength(80);
        renameBox.setBordered(false);
        renameBox.setTextColor(
                COLOR_TEXT
        );

        renameBox.visible = false;
    }

    /*
     * 更新输入框位置
     */
    private void updateEditorBounds() {

        int sidebarWidth = getSidebarWidth();

        if (workflowSearchBox != null) {

            workflowSearchBox.setX(
                    sidebarCollapsed
                            ? -100
                            : 48
            );

            workflowSearchBox.setY(
                    HEADER_HEIGHT + 12
            );

            workflowSearchBox.setWidth(
                    Math.max(
                            120,
                            SIDEBAR_EXPANDED_WIDTH - 60
                    )
            );

            workflowSearchBox.setHeight(
                    26
            );

            workflowSearchBox.visible = !sidebarCollapsed;
        }

        if (markerSearchBox != null) {

            int popupX =
                    (width - MARKER_POPUP_WIDTH)
                            / 2;

            int popupY =
                    (height - MARKER_POPUP_HEIGHT)
                            / 2;

            markerSearchBox.setX(
                    popupX + 20
            );

            markerSearchBox.setY(
                    popupY + 54
            );

            markerSearchBox.setWidth(
                    MARKER_POPUP_WIDTH - 40
            );

            markerSearchBox.setHeight(
                    26
            );

            markerSearchBox.visible = markerPickerOpen;
        }

        if (renameBox != null) {

            int popupX =
                    (width - RENAME_POPUP_WIDTH)
                            / 2;

            int popupY =
                    (height - RENAME_POPUP_HEIGHT)
                            / 2;

            renameBox.setX(
                    popupX + 20
            );

            renameBox.setY(
                    popupY + 56
            );

            renameBox.setWidth(
                    RENAME_POPUP_WIDTH - 40
            );

            renameBox.setHeight(
                    28
            );

            renameBox.visible = renameDialogOpen;
        }
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        this.mouseX = mouseX;

        this.mouseY = mouseY;

        ensureCurrentWorkflowNodePositions();

        updateHoveredNode();

        /*
         * 绘制工作流界面
         */
        renderBackground(
                graphics
        );

        /*
         * 原生 Widget 层
         */
        graphics.pose().pushPose();

        graphics.pose().translate(
                0.0F,
                0.0F,
                Z_WIDGET
        );

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        graphics.pose().popPose();
    }

    @Override
    public void renderBackground(
            GuiGraphics graphics
    ) {

        /*
         * 最底层背景
         */
        graphics.fill(
                0,
                0,
                width,
                height,
                COLOR_BACKGROUND
        );

        /*
         * Canvas 世界层
         */
        graphics.pose().pushPose();

        graphics.pose().translate(
                0.0F,
                0.0F,
                Z_CANVAS
        );

        drawCanvas(
                graphics
        );

        graphics.flush();

        graphics.pose().popPose();

        /*
         * Canvas 浮动层
         */
        graphics.pose().pushPose();

        graphics.pose().translate(
                0.0F,
                0.0F,
                Z_CANVAS_FLOATING
        );

        if (nodeMenuOpen) {

            drawNodeMenu(
                    graphics
            );
        }

        if (!markerPickerOpen
                && !actionPickerOpen
                && !workflowMenuOpen
                && !nodeMenuOpen
                && !renameDialogOpen) {

            drawNodeTooltip(
                    graphics
            );
        }

        graphics.flush();

        graphics.pose().popPose();

        /*
         * 固定 UI 层
         */
        graphics.pose().pushPose();

        graphics.pose().translate(
                0.0F,
                0.0F,
                Z_FIXED_UI
        );

        drawHeader(
                graphics
        );

        drawSidebar(
                graphics
        );

        drawFooter(
                graphics
        );

        drawCanvasToolbar(
                graphics
        );

        drawCanvasZoomInfo(
                graphics
        );

        /*
         * 工作流菜单
         */
        if (workflowMenuOpen) {

            drawWorkflowMenu(
                    graphics
            );
        }

        graphics.flush();

        graphics.pose().popPose();

        /*
         * Modal 层
         */
        graphics.pose().pushPose();

        graphics.pose().translate(
                0.0F,
                0.0F,
                Z_MODAL
        );

        if (markerPickerOpen) {

            drawMarkerPicker(
                    graphics
            );
        }

        if (actionPickerOpen) {

            drawActionPicker(
                    graphics
            );
        }

        if (renameDialogOpen) {

            drawRenameDialog(
                    graphics
            );
        }

        graphics.flush();

        graphics.pose().popPose();
    }

    /*
     * 更新悬停节点
     */
    private void updateHoveredNode() {

        if (markerPickerOpen
                || actionPickerOpen
                || workflowMenuOpen
                || nodeMenuOpen
                || renameDialogOpen) {

            hoveredNodeId = null;

            return;
        }

        WorkflowStep hoveredStep =
                findStepAtScreen(
                        mouseX,
                        mouseY
                );

        UUID newHoveredId =
                hoveredStep == null
                        ? null
                        : hoveredStep.getId();

        if (!Objects.equals(
                hoveredNodeId,
                newHoveredId
        )) {

            hoveredNodeId = newHoveredId;

            hoveredNodeStartTime = System.currentTimeMillis();
        }
    }

    /*
     * 绘制节点 UUID 提示
     */
    private void drawNodeTooltip(
            GuiGraphics graphics
    ) {

        if (hoveredNodeId == null) {
            return;
        }

        if (System.currentTimeMillis()
                - hoveredNodeStartTime
                < NODE_TOOLTIP_DELAY) {
            return;
        }

        WorkflowStep step =
                findStepById(
                        hoveredNodeId
                );

        if (step == null) {
            return;
        }

        QuestMarker marker =
                findMarker(
                        step.getMarkerId()
                );

        String stepId =
                step.getId()
                        .toString();

        String markerId =
                marker == null
                        ? "不存在"
                        : marker.getId()
                        .toString();

        int tooltipWidth = 304;
        int tooltipHeight = 92;

        int canvasLeft = getSidebarWidth();

        int canvasTop = HEADER_HEIGHT;

        int canvasRight = width;

        int canvasBottom = height - FOOTER_HEIGHT;

        int x =
                (int) clamp(
                        mouseX + 14,
                        canvasLeft + 8,
                        canvasRight
                                - tooltipWidth
                                - 8
                );

        int y =
                (int) clamp(
                        mouseY + 14,
                        canvasTop + 8,
                        canvasBottom
                                - tooltipHeight
                                - 8
                );

        if (x + tooltipWidth > width) {
            x =
                    width
                            - tooltipWidth
                            - 8;
        }

        if (y + tooltipHeight > height) {
            y =
                    height
                            - tooltipHeight
                            - 8;
        }

        graphics.fill(
                x,
                y,
                x + tooltipWidth,
                y + tooltipHeight,
                0xF0141B24
        );

        graphics.fill(
                x,
                y,
                x + tooltipWidth,
                y + 2,
                COLOR_ACCENT
        );

        graphics.drawString(
                font,
                Component.literal("Step UUID"),
                x + 10,
                y + 10,
                COLOR_TEXT_MUTED
        );

        graphics.drawString(
                font,
                Component.literal(stepId),
                x + 10,
                y + 24,
                COLOR_TEXT_SECONDARY
        );

        graphics.drawString(
                font,
                Component.literal("Marker UUID"),
                x + 10,
                y + 48,
                COLOR_TEXT_MUTED
        );

        graphics.drawString(
                font,
                Component.literal(markerId),
                x + 10,
                y + 62,
                COLOR_TEXT_SECONDARY
        );
    }

    /*
     * 顶部标题栏
     */
    private void drawHeader(
            GuiGraphics graphics
    ) {

        graphics.fill(
                0,
                0,
                width,
                HEADER_HEIGHT,
                COLOR_HEADER
        );

        graphics.fill(
                0,
                HEADER_HEIGHT - 2,
                width,
                HEADER_HEIGHT,
                COLOR_BORDER
        );

        graphics.drawString(
                font,
                Component.literal("BCN"),
                18,
                16,
                COLOR_TEXT
        );

        graphics.drawString(
                font,
                Component.literal("/"),
                48,
                16,
                COLOR_TEXT_MUTED
        );

        graphics.drawString(
                font,
                Component.literal("工作流"),
                64,
                16,
                COLOR_ACCENT
        );

        graphics.drawString(
                font,
                Component.literal("·"),
                116,
                17,
                COLOR_TEXT_MUTED
        );

        String workflowName =
                currentWorkflow == null
                        ? "未选择工作流"
                        : currentWorkflow.getName();

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
                16,
                COLOR_TEXT_SECONDARY
        );

        int testX =
                width - 155;

        int saveX =
                width - 82;

        drawHeaderControl(
                graphics,
                testX,
                14,
                54,
                20,
                "测试",
                COLOR_ACCENT
        );

        drawHeaderControl(
                graphics,
                saveX,
                14,
                54,
                20,
                "保存",
                COLOR_SUCCESS
        );
    }

    /*
     * 左侧工作流浏览器
     */
    private void drawSidebar(
            GuiGraphics graphics
    ) {

        int sidebarWidth =
                getSidebarWidth();

        graphics.fill(
                0,
                HEADER_HEIGHT,
                sidebarWidth,
                height - FOOTER_HEIGHT,
                COLOR_SIDEBAR
        );

        graphics.fill(
                sidebarWidth - 1,
                HEADER_HEIGHT,
                sidebarWidth,
                height - FOOTER_HEIGHT,
                COLOR_BORDER
        );

        drawCollapseButton(
                graphics
        );

        if (sidebarCollapsed) {

            drawCollapsedWorkflowList(
                    graphics
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
                        workflows.size()
                                + " 个"
                ),
                76,
                HEADER_HEIGHT + 13,
                COLOR_TEXT_MUTED
        );

        drawWorkflowList(
                graphics,
                HEADER_HEIGHT + 52,
                height
                        - FOOTER_HEIGHT
                        - 48
        );

        drawSidebarNewButton(
                graphics
        );
    }

    /*
     * 折叠按钮
     */
    private void drawCollapseButton(
            GuiGraphics graphics
    ) {

        int x = 8;

        int y =
                HEADER_HEIGHT + 8;

        int buttonWidth =
                sidebarCollapsed
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
                sidebarCollapsed
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
            int listBottom
    ) {

        List<Workflow> visible =
                getFilteredWorkflows();

        int contentHeight =
                visible.size()
                        * WORKFLOW_ITEM_HEIGHT;

        int viewportHeight =
                listBottom - listTop;

        double maxScroll =
                Math.max(
                        0,
                        contentHeight
                                - viewportHeight
                );

        sidebarScroll =
                clamp(
                        sidebarScroll,
                        0,
                        maxScroll
                );

        int y =
                listTop
                        - (int) sidebarScroll;

        for (Workflow workflow :
                visible) {

            if (y + WORKFLOW_ITEM_HEIGHT >= listTop
                    && y <= listBottom) {

                boolean selected =
                        workflow == currentWorkflow;

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
            GuiGraphics graphics
    ) {

        List<Workflow> visible =
                getFilteredWorkflows();

        int y =
                HEADER_HEIGHT + 50;

        for (Workflow workflow :
                visible) {

            if (y + 30
                    >= height - FOOTER_HEIGHT) {
                break;
            }

            boolean selected =
                    workflow == currentWorkflow;

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
                            pinnedWorkflows.contains(
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
            GuiGraphics graphics
    ) {

        int x = 8;

        int y =
                height
                        - FOOTER_HEIGHT
                        - 36;

        int width =
                SIDEBAR_EXPANDED_WIDTH - 16;

        boolean hovered =
                inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        width,
                        22
                );

        drawPanelButton(
                graphics,
                x,
                y,
                width,
                22,
                "+ 新建工作流",
                hovered,
                COLOR_ACCENT
        );
    }

    /*
     * 画布
     */
    private void drawCanvas(
            GuiGraphics graphics
    ) {

        int canvasX =
                getSidebarWidth();

        int canvasTop =
                HEADER_HEIGHT;

        int canvasBottom =
                height - FOOTER_HEIGHT;

        graphics.fill(
                canvasX,
                canvasTop,
                width,
                canvasBottom,
                COLOR_BACKGROUND
        );

        /*
         * Canvas 裁剪区域
         */
        graphics.enableScissor(
                canvasX,
                canvasTop,
                width,
                canvasBottom
        );

        /*
         * 网格
         */
        drawGrid(
                graphics,
                canvasX,
                canvasTop,
                width,
                canvasBottom
        );

        if (currentWorkflow == null) {

            drawCanvasEmpty(
                    graphics,
                    canvasX,
                    canvasTop,
                    width - canvasX,
                    canvasBottom - canvasTop
            );

            graphics.flush();

            graphics.disableScissor();

            return;
        }

        /*
         * 确保节点位置
         */
        ensureCurrentWorkflowNodePositions();

        /*
         * 连接线
         */
        drawConnections(
                graphics
        );

        /*
         * 节点
         */
        for (WorkflowStep step :
                currentWorkflow.getSteps()) {

            drawNode(
                    graphics,
                    step
            );
        }

        /*
         * 提交 Canvas
         */
        graphics.flush();

        graphics.disableScissor();
    }


    /*
     * 绘制画布缩放信息
     */
    private void drawCanvasZoomInfo(
            GuiGraphics graphics
    ) {
        String text =
                String.format(
                        Locale.ROOT,
                        "%.0f%%",
                        zoom * 100.0D
                );

        int textWidth =
                font.width(text);

        int x =
                width
                        - textWidth
                        - 14;

        int y =
                height
                        - FOOTER_HEIGHT
                        - 16;

        graphics.drawString(
                font,
                Component.literal(text),
                x,
                y,
                COLOR_TEXT_SECONDARY
        );
    }


    /*
     * 画布网格
     */
    private void drawGrid(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom
    ) {

        int gridSize =
                Math.max(
                        12,
                        (int) (
                                32 * zoom
                        )
                );

        int offsetX =
                mod(
                        (int) panX,
                        gridSize
                );

        int offsetY =
                mod(
                        (int) panY,
                        gridSize
                );

        int gridColor =
                0xFF111923;

        for (
                int x = left + offsetX;
                x < right;
                x += gridSize
        ) {

            graphics.fill(
                    x,
                    top,
                    x + 1,
                    bottom,
                    gridColor
            );
        }

        for (
                int y = top + offsetY;
                y < bottom;
                y += gridSize
        ) {

            graphics.fill(
                    left,
                    y,
                    right,
                    y + 1,
                    gridColor
            );
        }
    }

    /*
     * 画布工具栏
     */
    private void drawCanvasToolbar(
            GuiGraphics graphics
    ) {

        int x =
                getSidebarWidth()
                        + 12;

        int y =
                HEADER_HEIGHT
                        + 12;

        drawPanelButton(
                graphics,
                x,
                y,
                44,
                20,
                "+",
                inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        44,
                        20
                ),
                COLOR_ACCENT
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
                COLOR_TEXT
        );
    }

    /*
     * 画布空状态
     */
    private void drawCanvasEmpty(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height
    ) {

        String title =
                "还没有流程节点";

        String description =
                "点击左上角“+”开始建立工作流";

        int centerX =
                x + width / 2;

        int centerY =
                y + height / 2;

        graphics.drawString(
                font,
                Component.literal(title),
                centerX
                        - font.width(title) / 2,
                centerY - 12,
                COLOR_TEXT
        );

        graphics.drawString(
                font,
                Component.literal(description),
                centerX
                        - font.width(description) / 2,
                centerY + 12,
                COLOR_TEXT_MUTED
        );
    }

    /*
     * 绘制节点连接线
     */
    private void drawConnections(
            GuiGraphics graphics
    ) {

        List<WorkflowStep> steps =
                currentWorkflow.getSteps();

        for (int i = 0;
             i < steps.size() - 1;
             i++) {

            WorkflowStep from =
                    steps.get(i);

            WorkflowStep to =
                    steps.get(i + 1);

            NodePosition fromPosition =
                    nodePositions.get(
                            from.getId()
                    );

            NodePosition toPosition =
                    nodePositions.get(
                            to.getId()
                    );

            if (fromPosition == null
                    || toPosition == null) {
                continue;
            }

            double fromX =
                    worldToScreenX(
                            fromPosition.x
                                    + NODE_WIDTH / 2.0
                    );

            double fromY =
                    worldToScreenY(
                            fromPosition.y
                                    + NODE_HEIGHT
                    );

            double toX =
                    worldToScreenX(
                            toPosition.x
                                    + NODE_WIDTH / 2.0
                    );

            double toY =
                    worldToScreenY(
                            toPosition.y
                    );

            drawWorkflowConnection(
                    graphics,
                    fromX,
                    fromY,
                    toX,
                    toY
            );
        }
    }

    /*
     * 绘制单向工作流连接
     */
    private void drawWorkflowConnection(
            GuiGraphics graphics,
            double startX,
            double startY,
            double endX,
            double endY
    ) {

        int thickness =
                (int) Math.round(
                        clamp(
                                2.0D
                                        + (
                                        zoom
                                                - 0.45D
                                ) * 0.8D,
                                2.0D,
                                3.0D
                        )
                );

        int x1 =
                (int) startX;

        int y1 =
                (int) startY;

        int x2 =
                (int) endX;

        int y2 =
                (int) endY;

        int arrowSize =
                Math.max(
                        5,
                        Math.min(
                                9,
                                (int) (
                                        8 * zoom
                                )
                        )
                );

        int arrowY =
                y2
                        - arrowSize
                        - 2;

        int middleY;

        if (y2 > y1) {

            middleY =
                    y1
                            + Math.max(
                            14,
                            (y2 - y1) / 2
                    );

        } else {

            middleY =
                    y1
                            - Math.max(
                            14,
                            (y1 - y2) / 2
                    );
        }

        graphics.fill(
                x1 - thickness / 2,
                Math.min(
                        y1,
                        middleY
                ),
                x1 + thickness / 2 + 1,
                Math.max(
                        y1,
                        middleY
                ),
                COLOR_BORDER_HOVER
        );

        int minX =
                Math.min(
                        x1,
                        x2
                );

        int maxX =
                Math.max(
                        x1,
                        x2
                );

        graphics.fill(
                minX,
                middleY - thickness / 2,
                maxX + 1,
                middleY + thickness / 2 + 1,
                COLOR_BORDER_HOVER
        );

        graphics.fill(
                x2 - thickness / 2,
                Math.min(
                        middleY,
                        arrowY
                ),
                x2 + thickness / 2 + 1,
                Math.max(
                        middleY,
                        arrowY
                ),
                COLOR_BORDER_HOVER
        );

        graphics.fill(
                x2 - arrowSize,
                arrowY,
                x2 + arrowSize + 1,
                arrowY + thickness,
                COLOR_ACCENT
        );

        graphics.fill(
                x2 - arrowSize + 2,
                arrowY + thickness,
                x2 + arrowSize - 1,
                arrowY + thickness + 2,
                COLOR_ACCENT
        );

        graphics.fill(
                x2 - arrowSize + 4,
                arrowY + thickness + 2,
                x2 + arrowSize - 3,
                arrowY + thickness + 4,
                COLOR_ACCENT
        );
    }

    /*
     * 绘制流程节点
     */
    private void drawNode(
            GuiGraphics graphics,
            WorkflowStep step
    ) {

        NodePosition position =
                nodePositions.get(
                        step.getId()
                );

        if (position == null) {
            return;
        }

        int x =
                (int) worldToScreenX(
                        position.x
                );

        int y =
                (int) worldToScreenY(
                        position.y
                );

        int nodeWidth =
                Math.max(
                        150,
                        (int) (
                                NODE_WIDTH * zoom
                        )
                );

        int nodeHeight =
                Math.max(
                        82,
                        (int) (
                                NODE_HEIGHT * zoom
                        )
                );

        boolean selected =
                step.getId().equals(
                        selectedStepId
                );

        boolean hovered =
                inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        nodeWidth,
                        nodeHeight
                );

        int fillColor =
                selected
                        ? COLOR_NODE_SELECTED
                        : hovered
                        ? COLOR_NODE_HOVER
                        : COLOR_NODE;

        int borderColor =
                selected
                        ? COLOR_ACCENT
                        : hovered
                        ? COLOR_BORDER_HOVER
                        : COLOR_BORDER;

        graphics.fill(
                x,
                y,
                x + nodeWidth,
                y + nodeHeight,
                fillColor
        );

        graphics.fill(
                x,
                y,
                x + nodeWidth,
                y + 2,
                borderColor
        );

        graphics.fill(
                x,
                y + nodeHeight - 2,
                x + nodeWidth,
                y + nodeHeight,
                borderColor
        );

        graphics.fill(
                x,
                y,
                x + 2,
                y + nodeHeight,
                borderColor
        );

        graphics.fill(
                x + nodeWidth - 2,
                y,
                x + nodeWidth,
                y + nodeHeight,
                borderColor
        );

        QuestMarker marker =
                findMarker(
                        step.getMarkerId()
                );

        String markerName =
                marker == null
                        ? "目标标点不存在"
                        : marker.name;

        String stepText =
                String.format(
                        Locale.ROOT,
                        "%02d",
                        getStepIndex(step) + 1
                );

        String actionCountText =
                step.getActionCount()
                        + " Action";

        boolean compact =
                zoom < 0.80D;

        double textScale =
                compact
                        ? 0.82D
                        : clamp(
                        0.86D
                                + (
                                zoom
                                        - 0.8D
                        ) * 0.14D,
                        0.86D,
                        1.0D
                );

        int padding =
                compact
                        ? 10
                        : Math.max(
                        12,
                        (int) (
                                14 * zoom
                        )
                );

        int titleY =
                y + padding;

        drawNodeText(
                graphics,
                stepText,
                x + padding,
                titleY,
                selected
                        ? COLOR_ACCENT
                        : COLOR_TEXT_MUTED,
                textScale
        );

        int actionWidth =
                scaledFontWidth(
                        actionCountText,
                        textScale
                );

        int actionX =
                x
                        + nodeWidth
                        - padding
                        - actionWidth;

        int stepWidth =
                scaledFontWidth(
                        stepText,
                        textScale
                );

        int markerX =
                x
                        + padding
                        + stepWidth
                        + (
                        compact
                                ? 14
                                : 18
                );

        int markerAvailableWidth =
                actionX
                        - markerX
                        - (
                        compact
                                ? 8
                                : 12
                );

        markerName =
                trimTextToWidth(
                        markerName,
                        Math.max(
                                30,
                                markerAvailableWidth
                        ),
                        textScale
                );

        drawNodeText(
                graphics,
                markerName,
                markerX,
                titleY,
                COLOR_TEXT,
                textScale
        );

        drawNodeText(
                graphics,
                actionCountText,
                actionX,
                titleY,
                COLOR_SUCCESS,
                textScale
        );

        int dividerY =
                y
                        + (
                        compact
                                ? 30
                                : Math.max(
                                34,
                                (int) (
                                        42 * zoom
                                )
                        )
                );

        graphics.fill(
                x + padding,
                dividerY,
                x + nodeWidth - padding,
                dividerY + 1,
                COLOR_BORDER
        );

        if (compact) {

            drawNodeText(
                    graphics,
                    "目标",
                    x + padding,
                    y + 40,
                    COLOR_TEXT_MUTED,
                    textScale
            );

            String compactTarget =
                    trimTextToWidth(
                            markerName,
                            nodeWidth
                                    - padding * 2,
                            textScale
                    );

            drawNodeText(
                    graphics,
                    compactTarget,
                    x + padding + 38,
                    y + 40,
                    COLOR_TEXT_SECONDARY,
                    textScale
            );

            String triggerText =
                    String.format(
                            Locale.ROOT,
                            "触发 %.1fm",
                            step.getTriggerRadius()
                    );

            drawNodeText(
                    graphics,
                    triggerText,
                    x + padding,
                    y + 60,
                    COLOR_TEXT_SECONDARY,
                    textScale
            );

            if (step.getActionCount() > 0) {

                WorkflowAction action =
                        step.getAction(0);

                drawNodeText(
                        graphics,
                        "• "
                                + getActionTitle(
                                action.getType()
                        ),
                        x + padding + 75,
                        y + 60,
                        COLOR_TEXT_MUTED,
                        textScale
                );
            }

        } else {

            int contentPadding =
                    Math.max(
                            12,
                            (int) (
                                    16 * zoom
                            )
                    );

            int targetLabelY =
                    y
                            + (
                            int) (
                            53 * zoom
                    );

            int targetNameY =
                    y
                            + (
                            int) (
                            73 * zoom
                    );

            drawNodeText(
                    graphics,
                    "目标",
                    x + contentPadding,
                    targetLabelY,
                    COLOR_TEXT_MUTED,
                    textScale
            );

            String targetText =
                    marker == null
                            ? "目标标点不存在"
                            : marker.name;

            targetText =
                    trimTextToWidth(
                            targetText,
                            nodeWidth
                                    - contentPadding * 2,
                            textScale
                    );

            drawNodeText(
                    graphics,
                    targetText,
                    x + contentPadding,
                    targetNameY,
                    COLOR_TEXT_SECONDARY,
                    textScale
            );

            int triggerY =
                    y
                            + (
                            int) (
                            98 * zoom
                    );

            drawNodeText(
                    graphics,
                    "触发范围",
                    x + contentPadding,
                    triggerY,
                    COLOR_TEXT_MUTED,
                    textScale
            );

            String triggerText =
                    String.format(
                            Locale.ROOT,
                            "%.1f m",
                            step.getTriggerRadius()
                    );

            int triggerWidth =
                    scaledFontWidth(
                            triggerText,
                            textScale
                    );

            drawNodeText(
                    graphics,
                    triggerText,
                    x + nodeWidth
                            - contentPadding
                            - triggerWidth,
                    triggerY,
                    COLOR_TEXT_SECONDARY,
                    textScale
            );

            int actionLabelY =
                    y
                            + (
                            int) (
                            121 * zoom
                    );

            drawNodeText(
                    graphics,
                    "ACTION",
                    x + contentPadding,
                    actionLabelY,
                    COLOR_TEXT_MUTED,
                    textScale
            );

            if (step.getActionCount() > 0) {

                WorkflowAction action =
                        step.getAction(0);

                int actionBoxX =
                        x + contentPadding;

                int actionBoxY =
                        y
                                + (
                                int) (
                                136 * zoom
                        );

                int actionBoxWidth =
                        nodeWidth
                                - contentPadding * 2;

                int actionBoxHeight =
                        Math.max(
                                20,
                                (int) (
                                        32 * zoom
                                )
                        );

                if (actionBoxY
                        + actionBoxHeight
                        <= y + nodeHeight - 8) {

                    graphics.fill(
                            actionBoxX,
                            actionBoxY,
                            actionBoxX
                                    + actionBoxWidth,
                            actionBoxY
                                    + actionBoxHeight,
                            COLOR_PANEL_ALT
                    );

                    graphics.fill(
                            actionBoxX,
                            actionBoxY,
                            actionBoxX + 2,
                            actionBoxY
                                    + actionBoxHeight,
                            COLOR_ACCENT
                    );

                    String actionText =
                            "• "
                                    + getActionTitle(
                                    action.getType()
                            );

                    drawNodeText(
                            graphics,
                            actionText,
                            actionBoxX + 10,
                            actionBoxY + 7,
                            COLOR_TEXT_SECONDARY,
                            textScale
                    );
                }
            }
        }

        drawNodePort(
                graphics,
                x + nodeWidth / 2,
                y,
                zoom
        );

        drawNodePort(
                graphics,
                x + nodeWidth / 2,
                y + nodeHeight,
                zoom
        );
    }

    /*
     * 绘制节点文字
     */
    private void drawNodeText(
            GuiGraphics graphics,
            String text,
            int x,
            int y,
            int color,
            double scale
    ) {

        graphics.pose().pushPose();

        graphics.pose().translate(
                x,
                y,
                0
        );

        graphics.pose().scale(
                (float) scale,
                (float) scale,
                1.0F
        );

        graphics.drawString(
                font,
                Component.literal(text),
                0,
                0,
                color
        );

        graphics.pose().popPose();
    }

    /*
     * 获取缩放后的文字宽度
     */
    private int scaledFontWidth(
            String text,
            double scale
    ) {
        return (int) Math.ceil(
                font.width(text)
                        * scale
        );
    }

    /*
     * 截断过长文字
     */
    private String trimTextToWidth(
            String text,
            int maxWidth,
            double scale
    ) {

        if (text == null
                || text.isEmpty()) {

            return "";
        }

        if (scaledFontWidth(
                text,
                scale
        ) <= maxWidth) {

            return text;
        }

        String suffix =
                "...";

        int suffixWidth =
                scaledFontWidth(
                        suffix,
                        scale
                );

        if (suffixWidth >= maxWidth) {
            return "";
        }

        StringBuilder builder =
                new StringBuilder();

        for (int i = 0;
             i < text.length();
             i++) {

            builder.append(
                    text.charAt(i)
            );

            String candidate =
                    builder.toString();

            if (
                    scaledFontWidth(
                            candidate,
                            scale
                    ) + suffixWidth
                            > maxWidth
            ) {

                builder.deleteCharAt(
                        builder.length() - 1
                );

                break;
            }
        }

        return builder
                + suffix;
    }

    /*
     * 绘制节点连接端口
     */
    private void drawNodePort(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            double zoom
    ) {

        int size =
                Math.max(
                        6,
                        Math.min(
                                10,
                                (int) (
                                        8 * zoom
                                )
                        )
                );

        int half =
                size / 2;

        graphics.fill(
                centerX - half,
                centerY - half,
                centerX + half + 1,
                centerY + half + 1,
                COLOR_ACCENT
        );
    }

    /*
     * 节点右键菜单
     */
    private void drawNodeMenu(
            GuiGraphics graphics
    ) {

        WorkflowStep step =
                findStepById(
                        nodeMenuStepId
                );

        if (step == null) {
            nodeMenuOpen = false;
            return;
        }

        int x = nodeMenuX;
        int y = nodeMenuY;

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
     * Action 选择器
     */
    private void drawActionPicker(
            GuiGraphics graphics
    ) {

        drawOverlay(graphics);

        int x =
                (width - ACTION_POPUP_WIDTH)
                        / 2;

        int y =
                (height - ACTION_POPUP_HEIGHT)
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
     * Action 类型卡片
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
     * 任务点选择器
     */
    private void drawMarkerPicker(
            GuiGraphics graphics
    ) {

        drawOverlay(graphics);

        int x =
                (width - MARKER_POPUP_WIDTH)
                        / 2;

        int y =
                (height - MARKER_POPUP_HEIGHT)
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

        List<QuestMarker> markers =
                getFilteredMarkers();

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
                        * (cardHeight + gap);

        int viewportHeight =
                listBottom - listTop;

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

        for (QuestMarker marker :
                markers) {

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
                            ) + "...";
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
                    cardHeight + gap;
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
     * 工作流菜单
     */
    private void drawWorkflowMenu(
            GuiGraphics graphics
    ) {

        Workflow workflow =
                findWorkflow(
                        workflowMenuWorkflowId
                );

        if (workflow == null) {
            workflowMenuOpen = false;
            return;
        }

        List<Workflow> visible =
                getFilteredWorkflows();

        int itemIndex =
                visible.indexOf(workflow);

        if (itemIndex < 0) {
            workflowMenuOpen = false;
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
                        itemY + WORKFLOW_ITEM_HEIGHT
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
     * 重命名对话框
     */
    private void drawRenameDialog(
            GuiGraphics graphics
    ) {

        drawOverlay(graphics);

        int x =
                (width - RENAME_POPUP_WIDTH)
                        / 2;

        int y =
                (height - RENAME_POPUP_HEIGHT)
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
     * 遮罩
     */
    private void drawOverlay(
            GuiGraphics graphics
    ) {

        graphics.fill(
                0,
                0,
                width,
                height,
                0x99000000
        );
    }

    /*
     * 底部状态栏
     */
    private void drawFooter(
            GuiGraphics graphics
    ) {

        int y =
                height
                        - FOOTER_HEIGHT;

        graphics.fill(
                0,
                y,
                width,
                height,
                COLOR_HEADER
        );

        graphics.fill(
                0,
                y,
                width,
                y + 1,
                COLOR_BORDER
        );

        graphics.drawString(
                font,
                Component.literal(
                        "● " + statusText
                ),
                16,
                y + 10,
                statusColor
        );

        String info =
                currentWorkflow == null
                        ? "未选择工作流"
                        : "当前工作流："
                        + currentWorkflow.getName()
                        + "    "
                        + currentWorkflow.getStepCount()
                        + " 个节点";

        int infoWidth =
                font.width(info);

        graphics.drawString(
                font,
                Component.literal(info),
                Math.max(
                        250,
                        width / 2
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
                width
                        - helpWidth
                        - 14,
                y + 10,
                COLOR_TEXT_MUTED
        );
    }

    /*
     * 通用面板
     */
    private void drawPanel(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height
    ) {

        /*
         * 面板阴影
         */
        graphics.fill(
                x + 5,
                y + 5,
                x + width + 5,
                y + height + 5,
                0x66000000
        );

        /*
         * 主面板
         */
        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                COLOR_PANEL
        );

        /*
         * 顶部边框
         */
        graphics.fill(
                x,
                y,
                x + width,
                y + 2,
                COLOR_BORDER
        );

        /*
         * 底部边框
         */
        graphics.fill(
                x,
                y + height - 2,
                x + width,
                y + height,
                COLOR_BORDER
        );

        /*
         * 左侧边框
         */
        graphics.fill(
                x,
                y,
                x + 2,
                y + height,
                COLOR_BORDER
        );

        /*
         * 右侧边框
         */
        graphics.fill(
                x + width - 2,
                y,
                x + width,
                y + height,
                COLOR_BORDER
        );
    }

    /*
     * 通用卡片
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
                y + (
                        height - 8
                ) / 2,
                hovered
                        ? accent
                        : COLOR_TEXT
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
            int accent
    ) {

        drawPanelButton(
                graphics,
                x,
                y,
                width,
                height,
                text,
                inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        width,
                        height
                ),
                accent
        );
    }

    /*
     * 菜单项
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
     * 输入框背景
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
     * 打开目标标点选择器
     */
    private void openMarkerPicker() {

        replacingMarkerStepId = null;

        markerPickerOpen = true;

        actionPickerOpen = false;
        workflowMenuOpen = false;
        nodeMenuOpen = false;

        markerScroll = 0;

        updateEditorBounds();

        markerSearchBox.setValue("");
        markerSearchBox.setFocused(true);
    }

    /*
     * 打开已有节点的目标标点选择器
     */
    private void openMarkerPickerForStep(
            WorkflowStep step
    ) {

        if (step == null) {
            return;
        }

        replacingMarkerStepId =
                step.getId();

        selectedStepId =
                step.getId();

        markerPickerOpen = true;

        actionPickerOpen = false;
        workflowMenuOpen = false;
        nodeMenuOpen = false;

        markerScroll = 0;

        updateEditorBounds();

        markerSearchBox.setValue("");
        markerSearchBox.setFocused(true);
    }

    /*
     * 关闭目标标点选择器
     */
    private void closeMarkerPicker() {

        markerPickerOpen = false;

        replacingMarkerStepId = null;

        if (markerSearchBox != null) {

            markerSearchBox.setFocused(false);
            markerSearchBox.visible = false;
        }

        updateEditorBounds();
    }

    /*
     * 打开 Action 选择器
     */
    private void openActionPicker(
            WorkflowStep step
    ) {

        if (step == null) {
            return;
        }

        actionPickerStepId =
                step.getId();

        selectedStepId =
                step.getId();

        actionPickerOpen = true;

        markerPickerOpen = false;
        workflowMenuOpen = false;
        nodeMenuOpen = false;

        updateEditorBounds();
    }

    /*
     * 关闭 Action 选择器
     */
    private void closeActionPicker() {

        actionPickerOpen = false;

        actionPickerStepId = null;

        updateEditorBounds();
    }

    /*
     * 创建流程节点
     */
    private void addStepFromMarker(
            QuestMarker marker
    ) {

        if (currentWorkflow == null
                || marker == null) {
            return;
        }

        WorkflowStep step =
                WorkflowStep.create(
                        marker.getId(),
                        3.0D
                );

        currentWorkflow.addStep(step);

        int index =
                currentWorkflow
                        .getStepCount()
                        - 1;

        NodePosition position =
                new NodePosition(
                        140,
                        120 + index * 190
                );

        nodePositions.put(
                step.getId(),
                position
        );

        selectedStepId =
                step.getId();

        markDirty(
                "已添加流程节点"
        );
    }

    /*
     * 给流程节点添加 Action
     */
    private void addActionToSelectedNode(
            WorkflowAction action
    ) {

        if (action == null) {
            closeActionPicker();
            return;
        }

        WorkflowStep step =
                findStepById(
                        actionPickerStepId
                );

        if (step == null) {
            closeActionPicker();
            return;
        }

        step.addAction(action);

        selectedStepId =
                step.getId();

        markDirty(
                "已添加动作"
        );

        closeActionPicker();
    }

    /*
     * 复制节点
     */
    private void duplicateNode(
            WorkflowStep source
    ) {

        if (currentWorkflow == null
                || source == null) {
            return;
        }

        WorkflowStep copy =
                WorkflowStep.create(
                        source.getMarkerId(),
                        source.getTriggerRadius()
                );

        for (WorkflowAction action :
                source.getActions()) {

            copy.addAction(
                    copyAction(action)
            );
        }

        currentWorkflow.addStep(copy);

        NodePosition sourcePosition =
                nodePositions.get(
                        source.getId()
                );

        double newX =
                sourcePosition == null
                        ? 140
                        : sourcePosition.x
                        + 280;

        double newY =
                sourcePosition == null
                        ? 140
                        : sourcePosition.y
                        + 80;

        nodePositions.put(
                copy.getId(),
                new NodePosition(
                        newX,
                        newY
                )
        );

        selectedStepId =
                copy.getId();

        markDirty(
                "已复制流程节点"
        );
    }

    /*
     * 删除节点
     */
    private void deleteNode(
            WorkflowStep step
    ) {

        if (currentWorkflow == null
                || step == null) {
            return;
        }

        int index =
                findStepIndex(
                        step.getId()
                );

        if (index < 0) {
            return;
        }

        currentWorkflow.removeStep(index);

        nodePositions.remove(
                step.getId()
        );

        if (currentWorkflow.isEmpty()) {

            selectedStepId = null;

        } else {

            int nextIndex =
                    Math.min(
                            index,
                            currentWorkflow
                                    .getStepCount()
                                    - 1
                    );

            selectedStepId =
                    currentWorkflow
                            .getStep(
                                    nextIndex
                            )
                            .getId();
        }

        markDirty(
                "已删除流程节点"
        );
    }

    /*
     * 复制 Action
     */
    private WorkflowAction copyAction(
            WorkflowAction source
    ) {

        if (source == null) {
            return null;
        }

        return switch (source.getType()) {

            case EXECUTE_COMMAND -> WorkflowAction.executeCommand(
                    source.getData()
            );

            case GIVE_ITEM -> WorkflowAction.giveItem(
                    source.getData(),
                    source.getCount()
            );

            case ENABLE_MARKER -> WorkflowAction.enableMarker(
                    source.getData()
            );

            case DISABLE_MARKER -> WorkflowAction.disableMarker(
                    source.getData()
            );

            case MESSAGE -> WorkflowAction.message(
                    source.getData()
            );

            case SOUND -> WorkflowAction.sound(
                    source.getData()
            );
        };
    }

    /*
     * 创建工作流
     */
    private void createWorkflow() {

        Workflow workflow =
                Workflow.create(
                        "新建工作流 "
                                + (
                                workflows.size()
                                        + 1
                        )
                );

        workflows.add(workflow);

        selectWorkflow(workflow);

        markDirty(
                "已创建工作流"
        );
    }

    /*
     * 删除工作流
     */
    private void deleteWorkflow(
            Workflow workflow
    ) {

        if (workflow == null) {
            return;
        }

        UUID workflowId =
                workflow.getId();

        workflows.remove(
                workflow
        );

        pinnedWorkflows.remove(
                workflowId
        );

        if (workflows.isEmpty()) {

            Workflow fallback =
                    Workflow.create(
                            "新建工作流"
                    );

            workflows.add(fallback);
        }

        if (currentWorkflow == workflow
                || currentWorkflow == null) {

            currentWorkflow =
                    workflows.get(0);

            selectedWorkflowIndex =
                    0;

            if (currentWorkflow.isEmpty()) {

                selectedStepId = null;

            } else {

                selectedStepId =
                        currentWorkflow
                                .getStep(0)
                                .getId();
            }
        }

        markDirty(
                "已删除工作流"
        );

        workflowMenuOpen = false;
    }

    /*
     * 选择工作流
     */
    private void selectWorkflow(
            Workflow workflow
    ) {

        if (workflow == null) {
            return;
        }

        currentWorkflow =
                workflow;

        selectedWorkflowIndex =
                workflows.indexOf(
                        workflow
                );

        if (workflow.isEmpty()) {

            selectedStepId = null;

        } else {

            selectedStepId =
                    workflow
                            .getStep(0)
                            .getId();
        }

        zoom = 1.0D;

        panX = 0.0D;
        panY = 0.0D;

        sidebarScroll = 0.0D;

        workflowMenuOpen = false;
        nodeMenuOpen = false;

        ensureCurrentWorkflowNodePositions();

        markStatus(
                "已切换工作流",
                COLOR_TEXT_SECONDARY
        );
    }

    /*
     * 切换 Pin
     */
    private void togglePin(
            Workflow workflow
    ) {

        if (workflow == null) {
            return;
        }

        if (pinnedWorkflows.contains(
                workflow.getId()
        )) {

            pinnedWorkflows.remove(
                    workflow.getId()
            );

            markStatus(
                    "已取消固定",
                    COLOR_TEXT_SECONDARY
            );

        } else {

            pinnedWorkflows.add(
                    workflow.getId()
            );

            markStatus(
                    "已固定工作流",
                    COLOR_WARNING
            );
        }
    }

    /*
     * 打开重命名
     */
    private void openRenameDialog(
            Workflow workflow
    ) {

        if (workflow == null) {
            return;
        }

        workflowMenuWorkflowId =
                workflow.getId();

        workflowMenuOpen = false;

        renameDialogOpen = true;

        renameBox.setValue(
                workflow.getName()
        );

        renameBox.setCursorPosition(
                renameBox.getValue().length()
        );

        renameBox.setFocused(true);

        updateEditorBounds();
    }

    /*
     * 确认重命名
     */
    private void confirmRename() {

        if (!renameDialogOpen) {
            return;
        }

        Workflow workflow =
                findWorkflow(
                        workflowMenuWorkflowId
                );

        if (workflow == null) {

            closeRenameDialog();

            return;
        }

        String name =
                renameBox.getValue()
                        .trim();

        if (name.isEmpty()) {

            markStatus(
                    "名称不能为空",
                    COLOR_DANGER
            );

            return;
        }

        workflow.setName(name);

        markDirty(
                "工作流名称已修改"
        );

        closeRenameDialog();
    }

    /*
     * 关闭重命名
     */
    private void closeRenameDialog() {

        renameDialogOpen = false;

        workflowMenuWorkflowId = null;

        if (renameBox != null) {

            renameBox.setFocused(false);
            renameBox.visible = false;
        }

        updateEditorBounds();
    }

    /*
     * 自动适应画布
     */
    private void fitCanvas() {

        if (currentWorkflow == null
                || currentWorkflow.isEmpty()) {

            zoom = 1.0D;
            panX = 0.0D;
            panY = 0.0D;

            return;
        }

        ensureCurrentWorkflowNodePositions();

        double minX =
                Double.MAX_VALUE;

        double minY =
                Double.MAX_VALUE;

        double maxX =
                -Double.MAX_VALUE;

        double maxY =
                -Double.MAX_VALUE;

        for (WorkflowStep step :
                currentWorkflow.getSteps()) {

            NodePosition position =
                    nodePositions.get(
                            step.getId()
                    );

            if (position == null) {
                continue;
            }

            minX =
                    Math.min(
                            minX,
                            position.x
                    );

            minY =
                    Math.min(
                            minY,
                            position.y
                    );

            maxX =
                    Math.max(
                            maxX,
                            position.x
                                    + NODE_WIDTH
                    );

            maxY =
                    Math.max(
                            maxY,
                            position.y
                                    + NODE_HEIGHT
                    );
        }

        double contentWidth =
                Math.max(
                        1,
                        maxX - minX
                );

        double contentHeight =
                Math.max(
                        1,
                        maxY - minY
                );

        int canvasWidth =
                width
                        - getSidebarWidth();

        int canvasHeight =
                height
                        - HEADER_HEIGHT
                        - FOOTER_HEIGHT;

        double zoomX =
                (canvasWidth - 80)
                        / contentWidth;

        double zoomY =
                (canvasHeight - 80)
                        / contentHeight;

        zoom =
                clamp(
                        Math.min(
                                zoomX,
                                zoomY
                        ),
                        MIN_ZOOM,
                        MAX_ZOOM
                );

        double contentCenterX =
                minX
                        + contentWidth / 2.0;

        double contentCenterY =
                minY
                        + contentHeight / 2.0;

        panX =
                canvasWidth / 2.0
                        - contentCenterX
                        * zoom;

        panY =
                canvasHeight / 2.0
                        - contentCenterY
                        * zoom;
    }

    /*
     * 保存
     */
    private void saveWorkflow() {

        dirty = false;

        markStatus(
                "已保存",
                COLOR_SUCCESS
        );
    }

    /*
     * 测试
     */
    private void testWorkflow() {

        if (currentWorkflow == null
                || currentWorkflow.isEmpty()) {

            markStatus(
                    "没有可测试的流程",
                    COLOR_WARNING
            );

            return;
        }

        markStatus(
                "测试入口已准备",
                COLOR_ACCENT
        );
    }

    /*
     * 鼠标点击
     */
    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        this.mouseX =
                (int) mouseX;

        this.mouseY =
                (int) mouseY;

        if (renameDialogOpen) {

            return handleRenameDialogClick(
                    mouseX,
                    mouseY,
                    button
            );
        }

        if (markerPickerOpen) {

            return handleMarkerPickerClick(
                    mouseX,
                    mouseY,
                    button
            );
        }

        if (actionPickerOpen) {

            return handleActionPickerClick(
                    mouseX,
                    mouseY,
                    button
            );
        }

        if (nodeMenuOpen) {

            if (handleNodeMenuClick(
                    mouseX,
                    mouseY
            )) {

                return true;
            }

            nodeMenuOpen = false;

            return true;
        }

        if (workflowMenuOpen) {

            if (handleWorkflowMenuClick(
                    mouseX,
                    mouseY,
                    button
            )) {

                return true;
            }

            workflowMenuOpen = false;
        }

        if (super.mouseClicked(
                mouseX,
                mouseY,
                button
        )) {

            return true;
        }

        if (button
                == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {

            panning = true;

            panStartMouseX =
                    (int) mouseX;

            panStartMouseY =
                    (int) mouseY;

            panStartX = panX;
            panStartY = panY;

            return true;
        }

        if (handleHeaderClick(
                mouseX,
                mouseY
        )) {

            return true;
        }

        if (handleSidebarClick(
                mouseX,
                mouseY,
                button
        )) {

            return true;
        }

        if (handleCanvasClick(
                mouseX,
                mouseY,
                button
        )) {

            return true;
        }

        return true;
    }

    /*
     * 节点右键菜单点击
     */
    private boolean handleNodeMenuClick(
            double mouseX,
            double mouseY
    ) {

        WorkflowStep step =
                findStepById(
                        nodeMenuStepId
                );

        if (step == null) {
            nodeMenuOpen = false;
            return false;
        }

        int x =
                nodeMenuX;

        int y =
                nodeMenuY;

        if (inside(
                mouseX,
                mouseY,
                x + 6,
                y + 7,
                NODE_MENU_WIDTH - 12,
                24
        )) {

            nodeMenuOpen = false;

            openMarkerPickerForStep(
                    step
            );

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                x + 6,
                y + 34,
                NODE_MENU_WIDTH - 12,
                24
        )) {

            nodeMenuOpen = false;

            openActionPicker(
                    step
            );

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                x + 6,
                y + 61,
                NODE_MENU_WIDTH - 12,
                24
        )) {

            nodeMenuOpen = false;

            duplicateNode(step);

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                x + 6,
                y + 88,
                NODE_MENU_WIDTH - 12,
                24
        )) {

            nodeMenuOpen = false;

            deleteNode(step);

            return true;
        }

        return false;
    }

    /*
     * 顶部点击
     */
    private boolean handleHeaderClick(
            double mouseX,
            double mouseY
    ) {

        int testX =
                width - 155;

        int saveX =
                width - 82;

        if (inside(
                mouseX,
                mouseY,
                saveX,
                14,
                64,
                24
        )) {

            saveWorkflow();

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                testX,
                14,
                64,
                24
        )) {

            testWorkflow();

            return true;
        }

        return false;
    }

    /*
     * 左侧点击
     */
    private boolean handleSidebarClick(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (inside(
                mouseX,
                mouseY,
                8,
                HEADER_HEIGHT + 8,
                sidebarCollapsed
                        ? 26
                        : 30,
                28
        )) {

            sidebarCollapsed =
                    !sidebarCollapsed;

            updateEditorBounds();

            return true;
        }

        if (sidebarCollapsed) {

            List<Workflow> visible =
                    getFilteredWorkflows();

            int y =
                    HEADER_HEIGHT + 50;

            for (Workflow workflow :
                    visible) {

                if (inside(
                        mouseX,
                        mouseY,
                        6,
                        y,
                        30,
                        30
                )) {

                    selectWorkflow(
                            workflow
                    );

                    return true;
                }

                y += 38;
            }

            return true;
        }

        int newY =
                height
                        - FOOTER_HEIGHT
                        - 36;

        if (inside(
                mouseX,
                mouseY,
                8,
                newY,
                SIDEBAR_EXPANDED_WIDTH - 16,
                28
        )) {

            createWorkflow();

            return true;
        }

        List<Workflow> visible =
                getFilteredWorkflows();

        int listTop =
                HEADER_HEIGHT + 52;

        int y =
                listTop
                        - (int) sidebarScroll;

        int itemWidth =
                SIDEBAR_EXPANDED_WIDTH - 16;

        for (Workflow workflow :
                visible) {

            if (inside(
                    mouseX,
                    mouseY,
                    8,
                    y,
                    itemWidth,
                    WORKFLOW_ITEM_HEIGHT - 4
            )) {

                int pinX =
                        8
                                + itemWidth
                                - 44;

                int moreX =
                        8
                                + itemWidth
                                - 22;

                if (mouseX >= pinX
                        && mouseX
                        < pinX + 18) {

                    togglePin(
                            workflow
                    );

                    return true;
                }

                if (mouseX >= moreX
                        && mouseX
                        < moreX + 18) {

                    workflowMenuOpen = true;

                    workflowMenuWorkflowId =
                            workflow.getId();

                    return true;
                }

                selectWorkflow(
                        workflow
                );

                return true;
            }

            y +=
                    WORKFLOW_ITEM_HEIGHT;
        }

        return false;
    }

    /*
     * 画布点击
     */
    private boolean handleCanvasClick(
            double mouseX,
            double mouseY,
            int button
    ) {

        int canvasX =
                getSidebarWidth();

        int canvasTop =
                HEADER_HEIGHT;

        int canvasBottom =
                height - FOOTER_HEIGHT;

        if (!inside(
                mouseX,
                mouseY,
                canvasX,
                canvasTop,
                width - canvasX,
                canvasBottom
                        - canvasTop
        )) {

            return false;
        }

        int toolbarX =
                canvasX + 14;

        int toolbarY =
                HEADER_HEIGHT + 12;

        if (button
                == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

            if (inside(
                    mouseX,
                    mouseY,
                    toolbarX,
                    toolbarY,
                    64,
                    22
            )) {

                openMarkerPicker();

                return true;
            }

            if (inside(
                    mouseX,
                    mouseY,
                    toolbarX + 70,
                    toolbarY,
                    52,
                    22
            )) {

                fitCanvas();

                return true;
            }
        }

        if (button
                == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {

            WorkflowStep clickedStep =
                    findStepAtScreen(
                            mouseX,
                            mouseY
                    );

            if (clickedStep != null) {

                selectedStepId =
                        clickedStep.getId();

                nodeMenuStepId =
                        clickedStep.getId();

                int canvasLeft =
                        getSidebarWidth();

                nodeMenuX =
                        (int) clamp(
                                mouseX + 8,
                                canvasLeft + 8,
                                width
                                        - NODE_MENU_WIDTH
                                        - 8
                        );

                nodeMenuY =
                        (int) clamp(
                                mouseY + 8,
                                canvasTop + 8,
                                canvasBottom
                                        - NODE_MENU_HEIGHT
                                        - 8
                        );

                nodeMenuOpen = true;

                return true;
            }

            nodeMenuOpen = false;

            return true;
        }

        if (button
                != GLFW.GLFW_MOUSE_BUTTON_LEFT) {

            return false;
        }

        WorkflowStep clickedStep =
                findStepAtScreen(
                        mouseX,
                        mouseY
                );

        if (clickedStep == null) {

            selectedStepId = null;

            return true;
        }

        selectedStepId =
                clickedStep.getId();

        NodePosition position =
                nodePositions.get(
                        selectedStepId
                );

        if (position != null) {

            double worldMouseX =
                    screenToWorldX(
                            mouseX
                    );

            double worldMouseY =
                    screenToWorldY(
                            mouseY
                    );

            draggingNodeId =
                    selectedStepId;

            dragOffsetX =
                    worldMouseX
                            - position.x;

            dragOffsetY =
                    worldMouseY
                            - position.y;
        }

        return true;
    }

    /*
     * Action 选择器点击
     */
    private boolean handleActionPickerClick(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (button
                != GLFW.GLFW_MOUSE_BUTTON_LEFT) {

            return true;
        }

        int x =
                (width - ACTION_POPUP_WIDTH)
                        / 2;

        int y =
                (height - ACTION_POPUP_HEIGHT)
                        / 2;

        if (inside(
                mouseX,
                mouseY,
                x + ACTION_POPUP_WIDTH - 104,
                y + ACTION_POPUP_HEIGHT - 38,
                84,
                26
        )) {

            closeActionPicker();

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                x + 20,
                y + 74,
                ACTION_POPUP_WIDTH - 40,
                48
        )) {

            addActionToSelectedNode(
                    WorkflowAction.message(
                            "请输入消息"
                    )
            );

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                x + 20,
                y + 132,
                ACTION_POPUP_WIDTH - 40,
                48
        )) {

            addActionToSelectedNode(
                    WorkflowAction.executeCommand(
                            "say Hello"
                    )
            );

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                x + 20,
                y + 190,
                ACTION_POPUP_WIDTH - 40,
                48
        )) {

            addActionToSelectedNode(
                    WorkflowAction.giveItem(
                            "minecraft:stone",
                            1
                    )
            );

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                x + 20,
                y + 248,
                ACTION_POPUP_WIDTH - 40,
                48
        )) {

            addActionToSelectedNode(
                    WorkflowAction.sound(
                            "minecraft:block.note_block.pling"
                    )
            );

            return true;
        }

        if (!inside(
                mouseX,
                mouseY,
                x,
                y,
                ACTION_POPUP_WIDTH,
                ACTION_POPUP_HEIGHT
        )) {

            closeActionPicker();

            return true;
        }

        return true;
    }

    /*
     * 任务点选择器点击
     */
    private boolean handleMarkerPickerClick(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (button
                != GLFW.GLFW_MOUSE_BUTTON_LEFT) {

            return true;
        }

        int x =
                (width - MARKER_POPUP_WIDTH)
                        / 2;

        int y =
                (height - MARKER_POPUP_HEIGHT)
                        / 2;

        if (!inside(
                mouseX,
                mouseY,
                x,
                y,
                MARKER_POPUP_WIDTH,
                MARKER_POPUP_HEIGHT
        )) {

            closeMarkerPicker();

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                x + MARKER_POPUP_WIDTH - 110,
                y + MARKER_POPUP_HEIGHT - 38,
                90,
                26
        )) {

            closeMarkerPicker();

            return true;
        }

        List<QuestMarker> markers =
                getFilteredMarkers();

        int listTop =
                y + 92;

        int cardHeight = 58;
        int gap = 8;

        int cardY =
                listTop
                        - (int) markerScroll;

        for (QuestMarker marker :
                markers) {

            if (inside(
                    mouseX,
                    mouseY,
                    x + 20,
                    cardY,
                    MARKER_POPUP_WIDTH - 40,
                    cardHeight
            )) {

                if (replacingMarkerStepId != null) {

                    WorkflowStep step =
                            findStepById(
                                    replacingMarkerStepId
                            );

                    if (step != null) {

                        step.setMarkerId(
                                marker.getId()
                        );

                        markDirty(
                                "已修改目标标点"
                        );
                    }

                } else {

                    addStepFromMarker(
                            marker
                    );
                }

                closeMarkerPicker();

                return true;
            }

            cardY +=
                    cardHeight + gap;
        }

        return true;
    }

    /*
     * 工作流菜单点击
     */
    private boolean handleWorkflowMenuClick(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (button
                != GLFW.GLFW_MOUSE_BUTTON_LEFT) {

            return true;
        }

        Workflow workflow =
                findWorkflow(
                        workflowMenuWorkflowId
                );

        if (workflow == null) {
            workflowMenuOpen = false;
            return false;
        }

        List<Workflow> visible =
                getFilteredWorkflows();

        int itemIndex =
                visible.indexOf(workflow);

        if (itemIndex < 0) {
            return false;
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

        if (inside(
                mouseX,
                mouseY,
                menuX + 6,
                menuY + 7,
                menuWidth - 12,
                24
        )) {

            openRenameDialog(
                    workflow
            );

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                menuX + 6,
                menuY + 34,
                menuWidth - 12,
                24
        )) {

            deleteWorkflow(
                    workflow
            );

            return true;
        }

        return false;
    }

    /*
     * 重命名对话框点击
     */
    private boolean handleRenameDialogClick(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (button
                != GLFW.GLFW_MOUSE_BUTTON_LEFT) {

            return true;
        }

        int x =
                (width - RENAME_POPUP_WIDTH)
                        / 2;

        int y =
                (height - RENAME_POPUP_HEIGHT)
                        / 2;

        if (!inside(
                mouseX,
                mouseY,
                x,
                y,
                RENAME_POPUP_WIDTH,
                RENAME_POPUP_HEIGHT
        )) {

            closeRenameDialog();

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                x + RENAME_POPUP_WIDTH - 190,
                y + 106,
                76,
                26
        )) {

            closeRenameDialog();

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                x + RENAME_POPUP_WIDTH - 104,
                y + 106,
                76,
                26
        )) {

            confirmRename();

            return true;
        }

        return false;
    }

    /*
     * 拖动节点
     */
    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {

        if (markerPickerOpen
                || actionPickerOpen
                || renameDialogOpen) {

            return true;
        }

        this.mouseX =
                (int) mouseX;

        this.mouseY =
                (int) mouseY;

        if (panning
                && button
                == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {

            panX =
                    panStartX
                            + (
                            mouseX
                                    - panStartMouseX
                    );

            panY =
                    panStartY
                            + (
                            mouseY
                                    - panStartMouseY
                    );

            return true;
        }

        if (draggingNodeId != null
                && button
                == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

            NodePosition position =
                    nodePositions.get(
                            draggingNodeId
                    );

            if (position != null) {

                position.x =
                        screenToWorldX(
                                mouseX
                        )
                                - dragOffsetX;

                position.y =
                        screenToWorldY(
                                mouseY
                        )
                                - dragOffsetY;

                return true;
            }
        }

        return super.mouseDragged(
                mouseX,
                mouseY,
                button,
                dragX,
                dragY
        );
    }

    /*
     * 鼠标释放
     */
    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (markerPickerOpen
                || actionPickerOpen
                || renameDialogOpen) {

            return true;
        }

        if (button
                == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {

            panning = false;

            return true;
        }

        if (button
                == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && draggingNodeId != null) {

            markDirty(
                    "节点位置已修改"
            );

            draggingNodeId = null;

            return true;
        }

        return super.mouseReleased(
                mouseX,
                mouseY,
                button
        );
    }


    /*
     * 鼠标滚轮
     */
    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {

        if (renameDialogOpen) {

            return true;
        }

        if (actionPickerOpen) {

            return true;
        }

        if (markerPickerOpen) {

            List<QuestMarker> markers =
                    getFilteredMarkers();

            int viewportHeight =
                    MARKER_POPUP_HEIGHT
                            - 138;

            int contentHeight =
                    markers.size() * 66;

            markerScroll =
                    clamp(
                            markerScroll
                                    - delta * 58,
                            0,
                            Math.max(
                                    0,
                                    contentHeight
                                            - viewportHeight
                            )
                    );

            return true;
        }

        if (!sidebarCollapsed
                && inside(
                mouseX,
                mouseY,
                0,
                HEADER_HEIGHT,
                getSidebarWidth(),
                height
                        - HEADER_HEIGHT
                        - FOOTER_HEIGHT
        )) {

            List<Workflow> visible =
                    getFilteredWorkflows();

            int listHeight =
                    visible.size()
                            * WORKFLOW_ITEM_HEIGHT;

            int viewportHeight =
                    height
                            - HEADER_HEIGHT
                            - FOOTER_HEIGHT
                            - 100;

            sidebarScroll =
                    clamp(
                            sidebarScroll
                                    - delta * 50,
                            0,
                            Math.max(
                                    0,
                                    listHeight
                                            - viewportHeight
                            )
                    );

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                getSidebarWidth(),
                HEADER_HEIGHT,
                width
                        - getSidebarWidth(),
                height
                        - HEADER_HEIGHT
                        - FOOTER_HEIGHT
        )) {

            double oldZoom =
                    zoom;

            double zoomFactor =
                    delta > 0
                            ? 1.10D
                            : 0.90D;

            double newZoom =
                    clamp(
                            zoom
                                    * zoomFactor,
                            MIN_ZOOM,
                            MAX_ZOOM
                    );

            if (Math.abs(
                    newZoom - oldZoom
            ) > 0.0001D) {

                double worldX =
                        screenToWorldX(
                                mouseX
                        );

                double worldY =
                        screenToWorldY(
                                mouseY
                        );

                zoom = newZoom;

                int canvasX =
                        getSidebarWidth();

                panX =
                        mouseX
                                - canvasX
                                - worldX * zoom;

                panY =
                        mouseY
                                - HEADER_HEIGHT
                                - worldY * zoom;
            }

            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                delta
        );
    }


    /*
     * 键盘快捷键
     */
    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        /*
         * ESC 按层级关闭界面
         */
        if (keyCode
                == GLFW.GLFW_KEY_ESCAPE) {

            if (renameDialogOpen) {

                closeRenameDialog();

                return true;
            }

            if (markerPickerOpen) {

                closeMarkerPicker();

                return true;
            }

            if (actionPickerOpen) {

                closeActionPicker();

                return true;
            }

            if (nodeMenuOpen) {

                nodeMenuOpen = false;
                nodeMenuStepId = null;

                return true;
            }

            if (workflowMenuOpen) {

                workflowMenuOpen = false;
                workflowMenuWorkflowId = null;

                return true;
            }

            onClose();

            return true;
        }

        if (renameDialogOpen
                && keyCode
                == GLFW.GLFW_KEY_ENTER) {

            confirmRename();

            return true;
        }

        if (!markerPickerOpen
                && !actionPickerOpen
                && !renameDialogOpen
                && keyCode
                == GLFW.GLFW_KEY_DELETE) {

            deleteSelectedNode();

            return true;
        }

        if (!markerPickerOpen
                && !actionPickerOpen
                && !renameDialogOpen
                && keyCode
                == GLFW.GLFW_KEY_S
                && Screen.hasControlDown()) {

            saveWorkflow();

            return true;
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }


    /*
     * 删除当前选中的节点
     */
    private void deleteSelectedNode() {

        if (selectedStepId == null) {
            return;
        }

        WorkflowStep step =
                findStepById(
                        selectedStepId
                );

        if (step == null) {
            selectedStepId = null;
            return;
        }

        deleteNode(step);
    }

    @Override
    public void onClose() {

        minecraft.setScreen(
                parent
        );
    }

    /*
     * 根据 UUID 查找任务点
     */
    private QuestMarker findMarker(
            UUID markerId
    ) {

        if (markerId == null) {
            return null;
        }

        for (QuestMarker marker :
                ClientQuestCache.getMarkers()) {

            if (markerId.equals(
                    marker.getId()
            )) {

                return marker;
            }
        }

        return null;
    }

    /*
     * 根据 UUID 查找步骤
     */
    private WorkflowStep findStepById(
            UUID stepId
    ) {

        if (currentWorkflow == null
                || stepId == null) {

            return null;
        }

        for (WorkflowStep step :
                currentWorkflow.getSteps()) {

            if (stepId.equals(
                    step.getId()
            )) {

                return step;
            }
        }

        return null;
    }

    /*
     * 查找工作流
     */
    private Workflow findWorkflow(
            UUID workflowId
    ) {

        if (workflowId == null) {
            return null;
        }

        for (Workflow workflow :
                workflows) {

            if (workflowId.equals(
                    workflow.getId()
            )) {

                return workflow;
            }
        }

        return null;
    }

    /*
     * 查找步骤位置
     */
    private int findStepIndex(
            UUID stepId
    ) {

        if (currentWorkflow == null
                || stepId == null) {

            return -1;
        }

        for (int i = 0;
             i < currentWorkflow.getStepCount();
             i++) {

            if (stepId.equals(
                    currentWorkflow
                            .getStep(i)
                            .getId()
            )) {

                return i;
            }
        }

        return -1;
    }

    /*
     * 获取步骤下标
     */
    private int getStepIndex(
            WorkflowStep step
    ) {

        if (step == null) {
            return -1;
        }

        return findStepIndex(
                step.getId()
        );
    }

    /*
     * 查找当前鼠标位置的节点
     */
    private WorkflowStep findStepAtScreen(
            double mouseX,
            double mouseY
    ) {

        if (currentWorkflow == null) {
            return null;
        }

        /*
         * Canvas 屏幕区域
         */
        int canvasLeft =
                getSidebarWidth();

        int canvasTop =
                HEADER_HEIGHT;

        int canvasBottom =
                height - FOOTER_HEIGHT;

        /*
         * 鼠标不在 Canvas 中
         */
        if (!inside(
                mouseX,
                mouseY,
                canvasLeft,
                canvasTop,
                width - canvasLeft,
                canvasBottom - canvasTop
        )) {

            return null;
        }

        List<WorkflowStep> steps =
                currentWorkflow.getSteps();

        /*
         * 后绘制的节点优先获得鼠标命中
         */
        for (int i = steps.size() - 1;
             i >= 0;
             i--) {

            WorkflowStep step =
                    steps.get(i);

            NodePosition position =
                    nodePositions.get(
                            step.getId()
                    );

            if (position == null) {
                continue;
            }

            int x =
                    (int) worldToScreenX(
                            position.x
                    );

            int y =
                    (int) worldToScreenY(
                            position.y
                    );

            int nodeWidth =
                    Math.max(
                            150,
                            (int) (
                                    NODE_WIDTH
                                            * zoom
                            )
                    );

            int nodeHeight =
                    Math.max(
                            82,
                            (int) (
                                    NODE_HEIGHT
                                            * zoom
                            )
                    );

            /*
             * 判断鼠标是否位于节点范围内
             */
            if (inside(
                    mouseX,
                    mouseY,
                    x,
                    y,
                    nodeWidth,
                    nodeHeight
            )) {

                return step;
            }
        }

        return null;
    }


    /*
     * 确保所有节点都有编辑器位置
     */
    private void ensureCurrentWorkflowNodePositions() {

        if (currentWorkflow == null) {
            return;
        }

        int index = 0;

        for (WorkflowStep step :
                currentWorkflow.getSteps()) {

            int nodeIndex =
                    index;

            nodePositions.computeIfAbsent(
                    step.getId(),
                    ignored ->
                            new NodePosition(
                                    140,
                                    120
                                            + nodeIndex
                                            * 190
                            )
            );

            index++;
        }
    }

    /*
     * 获取工作流搜索结果
     */
    private List<Workflow> getFilteredWorkflows() {

        String keyword =
                workflowSearchBox == null
                        ? ""
                        : workflowSearchBox
                        .getValue()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        List<Workflow> result =
                new ArrayList<>();

        for (Workflow workflow :
                workflows) {

            if (!keyword.isEmpty()
                    && !workflow.getName()
                    .toLowerCase(
                            Locale.ROOT
                    )
                    .contains(keyword)) {

                continue;
            }

            result.add(workflow);
        }

        result.sort(
                Comparator
                        .comparing(
                                (Workflow workflow) ->
                                        !pinnedWorkflows
                                                .contains(
                                                        workflow
                                                                .getId()
                                                )
                        )
                        .thenComparing(
                                Workflow::getName,
                                String.CASE_INSENSITIVE_ORDER
                        )
        );

        return result;
    }

    /*
     * 获取任务点搜索结果
     */
    private List<QuestMarker> getFilteredMarkers() {

        String keyword =
                markerSearchBox == null
                        ? ""
                        : markerSearchBox
                        .getValue()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        List<QuestMarker> result =
                new ArrayList<>();

        for (QuestMarker marker :
                ClientQuestCache.getMarkers()) {

            if (keyword.isEmpty()
                    || marker.name
                    .toLowerCase(
                            Locale.ROOT
                    )
                    .contains(keyword)) {

                result.add(marker);
            }
        }

        result.sort(
                Comparator.comparing(
                        (QuestMarker marker) ->
                                marker.name
                                        .toLowerCase(
                                                Locale.ROOT
                                        )
                )
        );

        return result;
    }

    /*
     * 获取 Action 显示名称
     */
    private String getActionTitle(
            WorkflowAction.Type type
    ) {

        return switch (type) {

            case EXECUTE_COMMAND ->
                    "执行命令";

            case GIVE_ITEM ->
                    "发放物品";

            case ENABLE_MARKER ->
                    "启用标点";

            case DISABLE_MARKER ->
                    "禁用标点";

            case MESSAGE ->
                    "发送消息";

            case SOUND ->
                    "播放声音";
        };
    }

    /*
     * 修改状态
     */
    private void markDirty(
            String text
    ) {

        dirty = true;

        markStatus(
                text,
                COLOR_WARNING
        );
    }

    /*
     * 设置状态
     */
    private void markStatus(
            String text,
            int color
    ) {

        statusText = text;

        statusColor = color;
    }

    /*
     * 获取当前侧边栏宽度
     */
    private int getSidebarWidth() {

        return sidebarCollapsed
                ? SIDEBAR_COLLAPSED_WIDTH
                : SIDEBAR_EXPANDED_WIDTH;
    }

    /*
     * 世界坐标转换到屏幕坐标
     */
    private double worldToScreenX(
            double worldX
    ) {

        return getSidebarWidth()
                + panX
                + worldX * zoom;
    }

    private double worldToScreenY(
            double worldY
    ) {

        return HEADER_HEIGHT
                + panY
                + worldY * zoom;
    }

    /*
     * 屏幕坐标转换到世界坐标
     */
    private double screenToWorldX(
            double screenX
    ) {

        return (
                screenX
                        - getSidebarWidth()
                        - panX
        ) / zoom;
    }

    private double screenToWorldY(
            double screenY
    ) {

        return (
                screenY
                        - HEADER_HEIGHT
                        - panY
        ) / zoom;
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

    /*
     * 处理负数取模
     */
    private int mod(
            int value,
            int divisor
    ) {

        int result =
                value % divisor;

        return result < 0
                ? result + divisor
                : result;
    }

    /*
     * 节点位置
     */
    private static class NodePosition {

        private double x;
        private double y;

        private NodePosition(
                double x,
                double y
        ) {

            this.x = x;
            this.y = y;
        }
    }

    /*
     * 判断是否存在模态窗口
     */
    private boolean isModalOpen() {

        return markerPickerOpen
                || actionPickerOpen
                || renameDialogOpen;
    }

}