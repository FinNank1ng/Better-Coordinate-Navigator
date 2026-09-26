package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen;

import io.github.FinNank1ng.better_coordinate_navigator.client.gui.BCNMainScreen;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.layout.WorkflowFrameLayout;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.WorkflowScreenState.NodePosition;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render.WorkflowCanvasRenderer;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render.WorkflowFrameRenderer;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render.WorkflowPopupRenderer;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render.WorkflowSidebarRenderer;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.layout.WorkflowNodeActionEditorLayout;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientWorkflowPermission;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import io.github.FinNank1ng.better_coordinate_navigator.network.ModPackets;
import io.github.FinNank1ng.better_coordinate_navigator.network.WorkflowSavePacket;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class WorkflowScreen extends Screen {

    private static final int COLOR_BACKGROUND = 0xFF0B1118;
    private static final int COLOR_ACCENT = 0xFF67B8FF;

    private static final int COLOR_TEXT = 0xFFE8EEF5;
    private static final int COLOR_TEXT_SECONDARY = 0xFFA8B3BF;
    private static final int COLOR_TEXT_MUTED = 0xFF6E7C8B;

    private static final int COLOR_SUCCESS = 0xFF72D69A;
    private static final int COLOR_WARNING = 0xFFFFC66D;
    private static final int COLOR_DANGER = 0xFFFF7474;

    private static final int NODE_WIDTH = 230;
    private static final int NODE_HEIGHT = 150;

    private static final double MIN_ZOOM = 0.45D;
    private static final double MAX_ZOOM = 1.80D;

    private static final long NODE_TOOLTIP_DELAY = 700L;

    private static final int NODE_MENU_WIDTH = 150;
    private static final int NODE_MENU_HEIGHT = 132;

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
    private static final float Z_WORKFLOW_MENU = 40.0F;

    private final Screen parent;

    private final WorkflowScreenState state =
            new WorkflowScreenState(
                    COLOR_SUCCESS
            );

    private WorkflowSidebarRenderer sidebarRenderer;
    private WorkflowFrameRenderer fixedUIRenderer;
    private WorkflowPopupRenderer popupRenderer;

    private final WorkflowScreenInputHandler inputHandler;
    private final WorkflowCanvasRenderer canvasRenderer;

    private int mouseX;
    private int mouseY;

    private EditBox workflowSearchBox;
    private EditBox markerSearchBox;
    private EditBox renameBox;

    private EditBox actionDataBox;
    private EditBox actionCountBox;

    public WorkflowScreen(
            Screen parent,
            List<Workflow> workflows
    ) {

        super(
                Component.literal(
                        "BCN 工作流"
                )
        );

        this.parent = parent;

        this.canvasRenderer =
                new WorkflowCanvasRenderer(
                        state
                );

        this.inputHandler =
                new WorkflowScreenInputHandler(
                        state,

                        this::findWorkflow,
                        this::getFilteredWorkflows,

                        this::openRenameDialog,
                        this::deleteWorkflow,

                        this::addActionToSelectedNode,
                        this::closeActionPicker,
                        this::applyActionEditorChanges,
                        this::syncActionEditors,
                        this::deleteSelectedAction,

                        this::getFilteredMarkers,
                        (step, marker) -> {

                            if (!canManageWorkflows()) {
                                return;
                            }

                            if (step == null
                                    || marker == null) {
                                return;
                            }

                            step.setMarkerId(
                                    marker.getId()
                            );

                            markDirty(
                                    "已修改目标标点"
                            );
                        },
                        this::addStepFromMarker,
                        this::closeMarkerPicker,

                        this::findStepById,
                        this::openMarkerPickerForStep,
                        this::openActionPicker,
                        this::duplicateNode,
                        this::deleteNode,

                        this::closeRenameDialog,
                        this::confirmRename,
                        this::saveWorkflow,
                        this::deleteSelectedNode,

                        // onCloseHandler
                        () -> {

                            minecraft.setScreen(
                                    new BCNMainScreen()
                            );
                        },

                        // nodePositionChangedHandler
                        () -> {
                            if (!canManageWorkflows()) {
                                return;
                            }

                            markDirty(
                                    "节点位置已修改"
                            );
                        },

                        (mouseX, mouseY, button) ->
                                super.mouseClicked(
                                        mouseX,
                                        mouseY,
                                        button
                                ),

                        this::handleHeaderClick,
                        this::handleSidebarClick,
                        this::handleCanvasClick,

                        (mouseX, mouseY, button, dragX, dragY) ->
                                super.mouseDragged(
                                        mouseX,
                                        mouseY,
                                        button,
                                        dragX,
                                        dragY
                                ),

                        (mouseX, mouseY, button) ->
                                super.mouseReleased(
                                        mouseX,
                                        mouseY,
                                        button
                                ),

                        (mouseX, mouseY, delta) ->
                                super.mouseScrolled(
                                        mouseX,
                                        mouseY,
                                        delta
                                ),

                        (keyCode, scanCode, modifiers) ->
                                super.keyPressed(
                                        keyCode,
                                        scanCode,
                                        modifiers
                                )
                );

        loadWorkflows(
                workflows
        );
    }

    /*
     * 判断当前玩家是否拥有工作流管理权限
     */
    private boolean canManageWorkflows() {

        return ClientWorkflowPermission.canManage();
    }

    /*
     * 显示没有管理权限的提示
     */
    private void markPermissionDenied() {

        markStatus(
                "当前玩家没有工作流管理权限",
                COLOR_DANGER
        );
    }

    /*
     * 创建初始工作流
     *
     * 当前工作流数据完全由服务器提供
     * 客户端不再自行创建持久化工作流
     */
    private void createInitialWorkflow() {

        state.currentWorkflow = null;

        state.selectedWorkflowIndex = -1;

        state.selectedStepId = null;
    }

    @Override
    protected void init() {

        super.init();

        sidebarRenderer =
                new WorkflowSidebarRenderer(
                        state,
                        font
                );

        fixedUIRenderer =
                new WorkflowFrameRenderer(
                        state,
                        font
                );

        popupRenderer =
                new WorkflowPopupRenderer(
                        state,
                        font
                );

        buildEditors();

        updateEditorBounds();

        ensureCurrentWorkflowNodePositions();
    }

    /*
     * 加载工作流到编辑器
     */
    private void loadWorkflows(
            List<Workflow> workflows
    ) {

        state.getWorkflows().clear();

        state.currentWorkflow = null;

        state.selectedWorkflowIndex = -1;

        state.selectedStepId = null;

        state.dirty = false;

        if (workflows == null
                || workflows.isEmpty()) {

            createInitialWorkflow();

            return;
        }

        state.getWorkflows().addAll(
                workflows
        );

        state.currentWorkflow =
                state.getWorkflows().get(0);

        state.selectedWorkflowIndex = 0;

        state.selectedStepId = null;

        state.dirty = false;

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

        workflowSearchBox.setMaxLength(
                80
        );

        workflowSearchBox.setBordered(
                false
        );

        workflowSearchBox.setTextColor(
                COLOR_TEXT
        );

        workflowSearchBox.setHint(
                Component.literal("搜索工作流")
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

        markerSearchBox.setMaxLength(
                128
        );

        markerSearchBox.setBordered(
                false
        );

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

        renameBox.setMaxLength(
                80
        );

        renameBox.setBordered(
                false
        );

        renameBox.setTextColor(
                COLOR_TEXT
        );

        renameBox.visible = false;

        /*
         * Action 数据输入框
         */
        actionDataBox =
                addRenderableWidget(
                        new EditBox(
                                font,
                                0,
                                0,
                                300,
                                26,
                                Component.literal(
                                        "动作数据"
                                )
                        )
                );

        actionDataBox.setMaxLength(
                512
        );

        actionDataBox.setBordered(
                false
        );

        actionDataBox.setTextColor(
                COLOR_TEXT
        );

        actionDataBox.visible = false;

        /*
         * Action 数量输入框
         */
        actionCountBox =
                addRenderableWidget(
                        new EditBox(
                                font,
                                0,
                                0,
                                90,
                                26,
                                Component.literal(
                                        "数量"
                                )
                        )
                );

        actionCountBox.setMaxLength(
                9
        );

        actionCountBox.setBordered(
                false
        );

        actionCountBox.setTextColor(
                COLOR_TEXT
        );

        actionCountBox.visible = false;
    }



    /*
     * 更新输入框位置
     */
    private void updateEditorBounds() {

        if (workflowSearchBox != null) {

            workflowSearchBox.setX(
                    state.sidebarCollapsed
                            ? -100
                            : WorkflowFrameLayout.SIDEBAR_SEARCH_X
                            + WorkflowFrameLayout.SIDEBAR_SEARCH_TEXT_OFFSET
            );


            workflowSearchBox.setY(
                    WorkflowFrameLayout.sidebarSearchY() + 6
            );

            workflowSearchBox.setWidth(
                    WorkflowFrameLayout.SIDEBAR_SEARCH_WIDTH
            );

            workflowSearchBox.setHeight(
                    WorkflowFrameLayout.SIDEBAR_SEARCH_HEIGHT
            );

            workflowSearchBox.visible =
                    !state.sidebarCollapsed
                            && !state.workflowMenuOpen
                            && !state.renameDialogOpen
                            && !state.markerPickerOpen
                            && !state.actionPickerOpen
                            && !state.nodeMenuOpen;

            if (!workflowSearchBox.visible) {
                workflowSearchBox.setFocused(false);
            }
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

            renameBox.visible =
                    state.renameDialogOpen;
        }

        if (actionDataBox != null
                && actionCountBox != null) {

            WorkflowAction selectedAction = null;

            if (state.actionPickerOpen) {
                WorkflowStep step =
                        findStepById(
                                state.actionPickerStepId != null
                                        ? state.actionPickerStepId
                                        : state.selectedStepId
                        );

                int selectedIndex =
                        state.getSelectedActionIndex();

                if (step != null
                        && selectedIndex >= 0
                        && selectedIndex < step.getActionCount()) {

                    selectedAction =
                            step.getAction(selectedIndex);
                }
            }

            WorkflowNodeActionEditorLayout layout =
                    WorkflowNodeActionEditorLayout.calculate(
                            width,
                            height,
                            selectedAction
                    );

            WorkflowNodeActionEditorLayout.Rect dataInput =
                    layout.toScreen(
                            layout.getDataInput()
                    );

            boolean showCount =
                    state.actionPickerOpen
                            && selectedAction != null
                            && layout.isGiveItem();

            actionDataBox.setX(
                    dataInput.x()
            );

            actionDataBox.setY(
                    dataInput.y()
            );

            actionDataBox.setWidth(
                    dataInput.width()
            );

            actionDataBox.setHeight(
                    dataInput.height()
            );

            actionDataBox.visible =
                    state.actionPickerOpen
                            && selectedAction != null;

            WorkflowNodeActionEditorLayout.Rect countInput =
                    layout.toScreen(
                            layout.getCountInput()
                    );

            if (showCount && countInput != null) {

                actionCountBox.setX(
                        countInput.x()
                );

                actionCountBox.setY(
                        countInput.y()
                );

                actionCountBox.setWidth(
                        countInput.width()
                );

                actionCountBox.setHeight(
                        countInput.height()
                );

                actionCountBox.visible = true;

            } else {

                actionCountBox.setFocused(false);
                actionCountBox.visible = false;
            }
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

        popupRenderer.updateContext(
                width,
                height,
                mouseX,
                mouseY
        );

        inputHandler.updateContext(
                width,
                height
        );

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

        canvasRenderer.renderCanvas(
                graphics,
                width,
                height,
                getSidebarWidth(),
                mouseX,
                mouseY
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

        if (state.nodeMenuOpen) {

            popupRenderer.renderNodeMenu(
                    graphics,
                    state.nodeMenuX,
                    state.nodeMenuY,
                    findStepById(
                            state.nodeMenuStepId
                    )
            );
        }

        if (!state.markerPickerOpen
                && !state.actionPickerOpen
                && !state.workflowMenuOpen
                && !state.nodeMenuOpen
                && !state.renameDialogOpen) {

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

        fixedUIRenderer.render(
                graphics,
                width,
                height,
                getSidebarWidth(),
                mouseX,
                mouseY
        );

        sidebarRenderer.render(
                graphics,
                width,
                height,
                mouseX,
                mouseY,
                getFilteredWorkflows()
        );

        canvasRenderer.renderZoomInfo(
                graphics,
                width,
                height
        );

        if (state.workflowMenuOpen) {

            graphics.pose().pushPose();

            graphics.pose().translate(
                    0.0F,
                    0.0F,
                    Z_WORKFLOW_MENU
            );

            popupRenderer.renderWorkflowMenu(
                    graphics,
                    findWorkflow(
                            state.workflowMenuWorkflowId
                    ),
                    getFilteredWorkflows(),
                    state.getSidebarScroll()
            );

            graphics.pose().popPose();
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

        if (state.markerPickerOpen) {

            popupRenderer.renderMarkerPicker(
                    graphics,
                    getFilteredMarkers(),
                    state.markerScroll
            );
        }

        if (state.actionPickerOpen) {

            popupRenderer.renderActionPicker(
                    graphics
            );
        }

        if (state.renameDialogOpen) {

            popupRenderer.renderRenameDialog(
                    graphics,
                    renameBox
            );
        }

        graphics.flush();

        graphics.pose().popPose();
    }

    /*
     * 更新悬停节点
     */
    private void updateHoveredNode() {

        if (state.markerPickerOpen
                || state.actionPickerOpen
                || state.workflowMenuOpen
                || state.nodeMenuOpen
                || state.renameDialogOpen) {

            state.hoveredNodeId = null;

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
                state.hoveredNodeId,
                newHoveredId
        )) {

            state.hoveredNodeId =
                    newHoveredId;

            state.hoveredNodeStartTime =
                    System.currentTimeMillis();
        }
    }

    /*
     * 绘制节点 UUID 提示
     */
    private void drawNodeTooltip(
            GuiGraphics graphics
    ) {

        if (state.hoveredNodeId == null) {
            return;
        }

        if (System.currentTimeMillis()
                - state.hoveredNodeStartTime
                < NODE_TOOLTIP_DELAY) {

            return;
        }

        WorkflowStep step =
                findStepById(
                        state.hoveredNodeId
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

        int canvasLeft =
                getSidebarWidth();

        int canvasTop =
                WorkflowFrameLayout.HEADER_HEIGHT;

        int canvasRight =
                width;

        int canvasBottom =
                height - WorkflowFrameLayout.FOOTER_HEIGHT;

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
                Component.literal(
                        "Step UUID"
                ),
                x + 10,
                y + 10,
                COLOR_TEXT_MUTED
        );

        graphics.drawString(
                font,
                Component.literal(
                        stepId
                ),
                x + 10,
                y + 24,
                COLOR_TEXT_SECONDARY
        );

        graphics.drawString(
                font,
                Component.literal(
                        "Marker UUID"
                ),
                x + 10,
                y + 48,
                COLOR_TEXT_MUTED
        );

        graphics.drawString(
                font,
                Component.literal(
                        markerId
                ),
                x + 10,
                y + 62,
                COLOR_TEXT_SECONDARY
        );
    }

    /*
     * 打开目标标点选择器
     */
    private void openMarkerPicker() {

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        state.replacingMarkerStepId = null;

        state.markerPickerOpen = true;

        state.actionPickerOpen = false;

        state.workflowMenuOpen = false;
        state.nodeMenuOpen = false;

        state.markerScroll = 0;

        updateEditorBounds();

        markerSearchBox.setValue("");

        markerSearchBox.setFocused(
                true
        );
    }

    /*
     * 打开已有节点的目标标点选择器
     */
    private void openMarkerPickerForStep(
            WorkflowStep step
    ) {

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        if (step == null) {
            return;
        }

        state.replacingMarkerStepId =
                step.getId();

        state.selectedStepId =
                step.getId();

        state.markerPickerOpen = true;

        state.actionPickerOpen = false;
        state.workflowMenuOpen = false;
        state.nodeMenuOpen = false;

        state.markerScroll = 0;

        updateEditorBounds();

        markerSearchBox.setValue("");

        markerSearchBox.setFocused(
                true
        );
    }

    /*
     * 关闭目标标点选择器
     */
    private void closeMarkerPicker() {

        state.markerPickerOpen = false;

        state.replacingMarkerStepId = null;

        if (markerSearchBox != null) {

            markerSearchBox.setFocused(
                    false
            );

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

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        if (step == null) {
            return;
        }

        state.actionPickerStepId =
                step.getId();

        state.selectedStepId =
                step.getId();

        state.selectedActionIndex =
                -1;

        state.actionPickerOpen = true;

        state.markerPickerOpen = false;
        state.workflowMenuOpen = false;
        state.nodeMenuOpen = false;

        updateEditorBounds();

        syncActionEditors();
    }

    /*
     * 关闭 Action 选择器
     */
    private void closeActionPicker() {

        state.actionPickerOpen = false;

        state.actionPickerStepId = null;

        state.selectedActionIndex = -1;

        if (actionDataBox != null) {

            actionDataBox.setFocused(
                    false
            );

            actionDataBox.visible = false;
        }

        if (actionCountBox != null) {

            actionCountBox.setFocused(
                    false
            );

            actionCountBox.visible = false;
        }

        updateEditorBounds();
    }

    /*
     * 创建流程节点
     */
    private void addStepFromMarker(
            QuestMarker marker
    ) {

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        if (state.currentWorkflow == null
                || marker == null) {

            return;
        }

        WorkflowStep step =
                WorkflowStep.create(
                        marker.getId(),
                        3.0D
                );

        state.currentWorkflow.addStep(
                step
        );

        int index =
                state.currentWorkflow
                        .getStepCount()
                        - 1;

        NodePosition position =
                new NodePosition(
                        140,
                        120 + index * 190
                );

        state.nodePositions.put(
                step.getId(),
                position
        );

        state.selectedStepId =
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

        if (!canManageWorkflows()) {

            markPermissionDenied();

            closeActionPicker();

            return;
        }

        if (action == null) {

            closeActionPicker();

            return;
        }

        WorkflowStep step =
                findStepById(
                        state.actionPickerStepId
                );

        if (step == null) {

            closeActionPicker();

            return;
        }

        step.addAction(
                action
        );

        state.selectedStepId =
                step.getId();

        state.selectedActionIndex =
                step.getActionCount() - 1;

        markDirty(
                "已添加动作"
        );

        syncActionEditors();
    }


    /*
     * 复制节点
     */
    private void duplicateNode(
            WorkflowStep source
    ) {

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        if (state.currentWorkflow == null
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

        state.currentWorkflow.addStep(
                copy
        );

        NodePosition sourcePosition =
                state.nodePositions.get(
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

        state.nodePositions.put(
                copy.getId(),
                new NodePosition(
                        newX,
                        newY
                )
        );

        state.selectedStepId =
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

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        if (state.currentWorkflow == null
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

        state.currentWorkflow.removeStep(
                index
        );

        state.nodePositions.remove(
                step.getId()
        );

        if (state.currentWorkflow.isEmpty()) {

            state.selectedStepId = null;

        } else {

            int nextIndex =
                    Math.min(
                            index,
                            state.currentWorkflow
                                    .getStepCount()
                                    - 1
                    );

            state.selectedStepId =
                    state.currentWorkflow
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

        return WorkflowAction.of(
                source.getType(),
                source.getData(),
                source.getCount()
        );
    }

    /*
     * 创建工作流
     */
    private void createWorkflow() {

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        Workflow workflow =
                Workflow.create(
                        "新建工作流 "
                                + (
                                state.workflows.size()
                                        + 1
                        )
                );

        state.workflows.add(
                workflow
        );

        selectWorkflow(
                workflow
        );

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

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        if (workflow == null) {
            return;
        }

        UUID workflowId =
                workflow.getId();

        state.workflows.remove(
                workflow
        );

        state.pinnedWorkflows.remove(
                workflowId
        );

        state.nodePositions.entrySet()
                .removeIf(
                        entry ->
                                workflow.getSteps()
                                        .stream()
                                        .noneMatch(
                                                step ->
                                                        step.getId()
                                                                .equals(
                                                                        entry.getKey()
                                                                )
                                        )
                );

        if (state.workflows.isEmpty()) {

            state.currentWorkflow = null;

            state.selectedWorkflowIndex = -1;

            state.selectedStepId = null;

        } else if (state.currentWorkflow == workflow
                || state.currentWorkflow == null) {

            state.currentWorkflow =
                    state.workflows.get(0);

            state.selectedWorkflowIndex =
                    0;

            if (state.currentWorkflow.isEmpty()) {

                state.selectedStepId = null;

            } else {

                state.selectedStepId =
                        state.currentWorkflow
                                .getStep(0)
                                .getId();
            }
        }

        markDirty(
                "已删除工作流"
        );

        state.workflowMenuOpen = false;
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

        state.currentWorkflow =
                workflow;

        state.selectedWorkflowIndex =
                state.workflows.indexOf(
                        workflow
                );

        if (workflow.isEmpty()) {

            state.selectedStepId = null;

        } else {

            state.selectedStepId =
                    workflow
                            .getStep(0)
                            .getId();
        }

        state.zoom = 1.0D;

        state.panX = 0.0D;
        state.panY = 0.0D;

        state.workflowMenuOpen = false;
        state.nodeMenuOpen = false;

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

        if (state.pinnedWorkflows.contains(
                workflow.getId()
        )) {

            state.pinnedWorkflows.remove(
                    workflow.getId()
            );

            markStatus(
                    "已取消固定",
                    COLOR_TEXT_SECONDARY
            );

        } else {

            state.pinnedWorkflows.add(
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

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        if (workflow == null) {
            return;
        }

        state.workflowMenuWorkflowId =
                workflow.getId();

        state.workflowMenuOpen = false;

        state.renameDialogOpen = true;

        renameBox.setValue(
                workflow.getName()
        );

        renameBox.setCursorPosition(
                renameBox.getValue().length()
        );

        renameBox.setFocused(
                true
        );

        updateEditorBounds();
    }

    /*
     * 确认重命名
     */
    private void confirmRename() {

        if (!canManageWorkflows()) {

            markPermissionDenied();

            closeRenameDialog();

            return;
        }

        if (!state.renameDialogOpen) {
            return;
        }

        Workflow workflow =
                findWorkflow(
                        state.workflowMenuWorkflowId
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

        workflow.setName(
                name
        );

        markDirty(
                "工作流名称已修改"
        );

        closeRenameDialog();
    }

    /*
     * 关闭重命名
     */
    private void closeRenameDialog() {

        state.renameDialogOpen = false;

        state.workflowMenuWorkflowId = null;

        if (renameBox != null) {

            renameBox.setFocused(
                    false
            );

            renameBox.visible = false;
        }

        updateEditorBounds();
    }

    /*
     * 自动适应画布
     */
    private void fitCanvas() {

        if (state.currentWorkflow == null
                || state.currentWorkflow.isEmpty()) {

            state.zoom = 1.0D;

            state.panX = 0.0D;
            state.panY = 0.0D;

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
                state.currentWorkflow.getSteps()) {

            NodePosition position =
                    state.nodePositions.get(
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
                        - WorkflowFrameLayout.HEADER_HEIGHT
                        - WorkflowFrameLayout.FOOTER_HEIGHT;

        double zoomX =
                (canvasWidth - 80)
                        / contentWidth;

        double zoomY =
                (canvasHeight - 80)
                        / contentHeight;

        state.zoom =
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

        state.panX =
                canvasWidth / 2.0
                        - contentCenterX
                        * state.zoom;

        state.panY =
                canvasHeight / 2.0
                        - contentCenterY
                        * state.zoom;
    }

    /*
     * 保存工作流
     */
    private void saveWorkflow() {

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        if (!state.dirty) {

            markStatus(
                    "没有需要保存的修改",
                    COLOR_TEXT_SECONDARY
            );

            return;
        }

        /*
         * 当前没有工作流时允许保存空列表
         *
         * 空列表代表删除了全部工作流
         */
        ModPackets.CHANNEL.sendToServer(
                new WorkflowSavePacket(
                        new ArrayList<>(
                                state.workflows
                        )
                )
        );

        /*
         * 这里暂时不能立即认为服务器已经保存成功
         *
         * 服务端会进行权限检查和数据校验
         */
        markStatus(
                "正在保存...",
                COLOR_WARNING
        );
    }

    /*
     * 服务端返回最新工作流数据
     */
    public void onWorkflowDataUpdated() {

        if (!state.dirty) {
            return;
        }

        state.dirty = false;

        markStatus(
                "已保存",
                COLOR_SUCCESS
        );
    }

    /*
     * 删除当前选中的 Action
     */
    private void deleteSelectedAction() {

        if (!canManageWorkflows()) {
            markPermissionDenied();
            return;
        }

        if (!state.actionPickerOpen) {
            return;
        }

        WorkflowStep step =
                findStepById(
                        state.actionPickerStepId
                );

        int index =
                state.getSelectedActionIndex();

        if (step == null
                || index < 0
                || index >= step.getActionCount()) {
            return;
        }

        step.removeAction(index);

        if (step.getActionCount() == 0) {
            state.selectedActionIndex = -1;
        } else {
            state.selectedActionIndex =
                    Math.min(
                            index,
                            step.getActionCount() - 1
                    );
        }

        markDirty(
                "已删除动作"
        );

        syncActionEditors();
    }

    /*
     * 同步当前 Action 编辑器
     */
    private void syncActionEditors() {

        if (actionDataBox == null
                || actionCountBox == null) {

            return;
        }

        if (!state.actionPickerOpen) {

            actionDataBox.setFocused(false);
            actionCountBox.setFocused(false);

            actionDataBox.visible = false;
            actionCountBox.visible = false;

            updateEditorBounds();

            return;
        }

        WorkflowStep step =
                findStepById(
                        state.actionPickerStepId != null
                                ? state.actionPickerStepId
                                : state.selectedStepId
                );

        int index =
                state.getSelectedActionIndex();

        if (step == null
                || index < 0
                || index >= step.getActionCount()) {

            actionDataBox.setFocused(false);
            actionCountBox.setFocused(false);

            actionDataBox.visible = false;
            actionCountBox.visible = false;

            updateEditorBounds();

            return;
        }

        WorkflowAction action =
                step.getAction(index);

        if (action == null) {

            actionDataBox.visible = false;
            actionCountBox.visible = false;

            updateEditorBounds();

            return;
        }

        String data =
                action.getData() == null
                        ? ""
                        : action.getData();

        if (action.getType()
                == WorkflowAction.Type.EXECUTE_COMMAND
                && data.startsWith("/")) {

            data = data.substring(1);
        }

        actionDataBox.setValue(
                data
        );

        actionDataBox.setCursorPosition(
                data.length()
        );

        actionDataBox.setFocused(
                false
        );

        if (action.getType()
                == WorkflowAction.Type.GIVE_ITEM) {

            actionCountBox.setValue(
                    String.valueOf(
                            action.getCount()
                    )
            );

            actionCountBox.setCursorPosition(
                    actionCountBox
                            .getValue()
                            .length()
            );

            actionCountBox.visible = true;

        } else {

            actionCountBox.setFocused(
                    false
            );

            actionCountBox.visible = false;
        }

        actionDataBox.visible = true;

        updateEditorBounds();
    }

    /*
     * 应用 Action 编辑修改
     */
    private void applyActionEditorChanges() {

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        if (!state.actionPickerOpen) {
            return;
        }

        WorkflowStep step =
                findStepById(
                        state.actionPickerStepId
                );

        int index =
                state.getSelectedActionIndex();

        if (step == null
                || index < 0
                || index >= step.getActionCount()) {

            markStatus(
                    "请先选择一个动作",
                    COLOR_WARNING
            );

            return;
        }

        WorkflowAction action =
                step.getAction(index);

        if (action == null) {
            return;
        }

        String data =
                actionDataBox.getValue();

        if (data == null) {
            data = "";
        }

        /*
         * 命令动作只在界面显示 /，实际数据不保存这个前缀。
         */
        if (action.getType()
                == WorkflowAction.Type.EXECUTE_COMMAND) {

            data = data.stripLeading();

            if (data.startsWith("/")) {
                data = data.substring(1);
            }
        }

        /*
         * GIVE_ITEM 额外保存数量
         */
        if (action.getType()
                == WorkflowAction.Type.GIVE_ITEM) {

            String countText =
                    actionCountBox.getValue()
                            .trim();

            int count;

            try {

                count =
                        Integer.parseInt(
                                countText
                        );

            } catch (NumberFormatException exception) {

                markStatus(
                        "数量必须是正整数",
                        COLOR_DANGER
                );

                return;
            }

            if (count <= 0) {

                markStatus(
                        "数量必须大于 0",
                        COLOR_DANGER
                );

                return;
            }

            action.setCount(
                    count
            );
        }

        action.setData(
                data
        );

        markDirty(
                "已应用动作修改"
        );

        closeActionPicker();
    }

    /*
     * 测试工作流
     */
    private void testWorkflow() {

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        if (state.currentWorkflow == null
                || state.currentWorkflow.isEmpty()) {

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

        return inputHandler.handleMouseClicked(
                mouseX,
                mouseY,
                button
        );
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
                WorkflowFrameLayout.HEADER_HEIGHT + 8,
                state.sidebarCollapsed
                        ? 26
                        : 30,
                28
        )) {

            state.sidebarCollapsed =
                    !state.sidebarCollapsed;

            updateEditorBounds();

            return true;
        }

        if (state.sidebarCollapsed) {

            List<Workflow> visible =
                    getFilteredWorkflows();

            int y =
                    WorkflowFrameLayout.HEADER_HEIGHT + 50;

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

            return false;
        }

        int newY =
                height
                        - WorkflowFrameLayout.FOOTER_HEIGHT
                        - 36;

        if (inside(
                mouseX,
                mouseY,
                8,
                newY,
                WorkflowFrameLayout.SIDEBAR_EXPANDED_WIDTH - 16,
                28
        )) {

            if (!canManageWorkflows()) {

                markPermissionDenied();

                return true;
            }

            createWorkflow();

            return true;
        }

        List<Workflow> visible = getFilteredWorkflows();

        int listTop =
                WorkflowFrameLayout.workflowListTop();

        int y =
                listTop - (int) state.sidebarScroll;

        int itemWidth =
                WorkflowFrameLayout.SIDEBAR_EXPANDED_WIDTH - 16;

        for (Workflow workflow :
                visible) {

            if (inside(
                    mouseX,
                    mouseY,
                    8,
                    y,
                    itemWidth,
                    WorkflowFrameLayout.WORKFLOW_ITEM_HEIGHT - 4
            )) {

                if (button
                        == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {

                    state.workflowMenuOpen = true;

                    state.workflowMenuWorkflowId =
                            workflow.getId();

                    return true;
                }

                int pinX = 8 + itemWidth - 44;

                if (mouseX >= pinX && mouseX < pinX + 18) {

                    togglePin(
                            workflow
                    );

                    return true;
                }


                selectWorkflow(
                        workflow
                );

                return true;
            }

            y += WorkflowFrameLayout.WORKFLOW_ITEM_HEIGHT;
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
                WorkflowFrameLayout.HEADER_HEIGHT;

        int canvasBottom =
                height - WorkflowFrameLayout.FOOTER_HEIGHT;

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

        int toolbarX = canvasX + 14;

        int toolbarY = WorkflowFrameLayout.HEADER_HEIGHT + 12;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

            if (inside(
                    mouseX,
                    mouseY,
                    toolbarX,
                    toolbarY,
                    64,
                    22
            )) {

                if (!canManageWorkflows()) {

                    markPermissionDenied();

                    return true;
                }

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

                state.selectedStepId =
                        clickedStep.getId();

                state.nodeMenuStepId =
                        clickedStep.getId();

                int canvasLeft =
                        getSidebarWidth();

                state.nodeMenuX =
                        (int) clamp(
                                mouseX + 8,
                                canvasLeft + 8,
                                width
                                        - NODE_MENU_WIDTH
                                        - 8
                        );

                state.nodeMenuY =
                        (int) clamp(
                                mouseY + 8,
                                canvasTop + 8,
                                canvasBottom
                                        - NODE_MENU_HEIGHT
                                        - 8
                        );

                state.nodeMenuOpen = true;

                return true;
            }

            state.nodeMenuOpen = false;

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

            state.selectedStepId = null;

            return true;
        }

        state.selectedStepId =
                clickedStep.getId();

        /*
         * 普通玩家只能查看节点
         * 不能开始拖动节点
         */
        if (!canManageWorkflows()) {
            return true;
        }

        NodePosition position =
                state.nodePositions.get(
                        state.selectedStepId
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

            state.draggingNodeId =
                    state.selectedStepId;

            state.dragOffsetX =
                    worldMouseX
                            - position.x;

            state.dragOffsetY =
                    worldMouseY
                            - position.y;
        }

        return true;
    }

    /*
     * 鼠标拖动
     */
    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {

        this.mouseX =
                (int) mouseX;

        this.mouseY =
                (int) mouseY;

        /*
         * 普通玩家不能修改节点位置
         */
        if (!canManageWorkflows()
                && button
                == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

            return true;
        }

        return inputHandler.handleMouseDragged(
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

        return inputHandler.handleMouseReleased(
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

        return inputHandler.handleMouseScrolled(
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

        if (state.actionPickerOpen
                && keyCode == GLFW.GLFW_KEY_DELETE
                && ((actionDataBox != null && actionDataBox.isFocused())
                || (actionCountBox != null && actionCountBox.isFocused()))) {

            return super.keyPressed(
                    keyCode,
                    scanCode,
                    modifiers
            );
        }

        return inputHandler.handleKeyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }

    /*
     * 删除当前选中的节点
     */
    private void deleteSelectedNode() {

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        if (state.selectedStepId == null) {
            return;
        }

        WorkflowStep step =
                findStepById(
                        state.selectedStepId
                );

        if (step == null) {

            state.selectedStepId = null;

            return;
        }

        deleteNode(
                step
        );
    }

    @Override
    public void onClose() {

        minecraft.setScreen(new BCNMainScreen()
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

        if (state.currentWorkflow == null
                || stepId == null) {

            return null;
        }

        for (WorkflowStep step :
                state.currentWorkflow.getSteps()) {

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
                state.workflows) {

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

        if (state.currentWorkflow == null
                || stepId == null) {

            return -1;
        }

        for (int i = 0;
             i < state.currentWorkflow.getStepCount();
             i++) {

            if (stepId.equals(
                    state.currentWorkflow
                            .getStep(i)
                            .getId()
            )) {

                return i;
            }
        }

        return -1;
    }

    /*
     * 查找当前鼠标位置的节点
     */
    private WorkflowStep findStepAtScreen(
            double mouseX,
            double mouseY
    ) {

        if (state.currentWorkflow == null) {
            return null;
        }

        /*
         * Canvas 屏幕区域
         */
        int canvasLeft =
                getSidebarWidth();

        int canvasTop =
                WorkflowFrameLayout.HEADER_HEIGHT;

        int canvasBottom =
                height - WorkflowFrameLayout.FOOTER_HEIGHT;

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
                state.currentWorkflow.getSteps();

        /*
         * 后绘制的节点优先获得鼠标命中
         */
        for (int i = steps.size() - 1;
             i >= 0;
             i--) {

            WorkflowStep step =
                    steps.get(i);

            NodePosition position =
                    state.nodePositions.get(
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
                                            * state.zoom
                            )
                    );

            int nodeHeight =
                    Math.max(
                            82,
                            (int) (
                                    NODE_HEIGHT
                                            * state.zoom
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

        if (state.currentWorkflow == null) {
            return;
        }

        int index = 0;

        for (WorkflowStep step :
                state.currentWorkflow.getSteps()) {

            int nodeIndex =
                    index;

            state.nodePositions.computeIfAbsent(
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
                state.workflows) {

            if (!keyword.isEmpty()
                    && !workflow.getName()
                    .toLowerCase(
                            Locale.ROOT
                    )
                    .contains(keyword)) {

                continue;
            }

            result.add(
                    workflow
            );
        }

        result.sort(
                Comparator
                        .comparing(
                                (Workflow workflow) ->
                                        !state.pinnedWorkflows
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

                result.add(
                        marker
                );
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
     * 修改状态
     */
    private void markDirty(
            String text
    ) {

        if (!canManageWorkflows()) {

            markPermissionDenied();

            return;
        }

        state.dirty = true;

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

        state.statusText = text;

        state.statusColor = color;
    }

    /*
     * 获取当前侧边栏宽度
     */
    private int getSidebarWidth() {

        return WorkflowFrameLayout.sidebarWidth(
                state.sidebarCollapsed
        );
    }

    /*
     * 世界坐标转换到屏幕坐标
     */
    private double worldToScreenX(
            double worldX
    ) {

        return getSidebarWidth()
                + state.panX
                + worldX * state.zoom;
    }

    private double worldToScreenY(
            double worldY
    ) {

        return WorkflowFrameLayout.HEADER_HEIGHT
                + state.panY
                + worldY * state.zoom;
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
                        - state.panX
        ) / state.zoom;
    }

    private double screenToWorldY(
            double screenY
    ) {

        return (
                screenY
                        - WorkflowFrameLayout.HEADER_HEIGHT
                        - state.panY
        ) / state.zoom;
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
}