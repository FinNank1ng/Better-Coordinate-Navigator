package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen;

import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.WorkflowScreenState.NodePosition;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render.WorkflowCanvasRenderer;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render.WorkflowSidebarRenderer;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render.WorkflowFixedUIRenderer;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render.WorkflowPopupRenderer;

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

    private final WorkflowScreenState state =
            new WorkflowScreenState(
                    COLOR_SUCCESS
            );

    private WorkflowSidebarRenderer sidebarRenderer;
    private WorkflowFixedUIRenderer fixedUIRenderer;
    private WorkflowPopupRenderer popupRenderer;

    private final WorkflowScreenInputHandler inputHandler;
    private final WorkflowCanvasRenderer canvasRenderer;

    private int mouseX;
    private int mouseY;

    private EditBox workflowSearchBox;
    private EditBox markerSearchBox;
    private EditBox renameBox;

    public WorkflowScreen(Screen parent) {
        super(Component.literal("BCN / 工作流"));

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

                        this::getFilteredMarkers,
                        (step, marker) -> {

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
                        () ->
                                markDirty(
                                        "节点位置已修改"
                                ),
                        this::onClose,

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

            state.nodePositions.put(
                    step.getId(),
                    new NodePosition(
                            140,
                            120 + index * 190
                    )
            );

            index++;
        }

        state.workflows.add(workflow);

        state.currentWorkflow = workflow;

        state.selectedWorkflowIndex = 0;

        if (!workflow.isEmpty()) {

            state.selectedStepId =
                    workflow
                            .getStep(0)
                            .getId();
        }
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
                new WorkflowFixedUIRenderer(
                        state,
                        font
                );

        popupRenderer =
                new WorkflowPopupRenderer(
                        font
                );

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

        if (workflowSearchBox != null) {

            workflowSearchBox.setX(
                    state.sidebarCollapsed
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

            workflowSearchBox.visible = !state.sidebarCollapsed;
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

            markerSearchBox.visible = state.markerPickerOpen;
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

            renameBox.visible = state.renameDialogOpen;
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

            popupRenderer.renderWorkflowMenu(
                    graphics,
                    findWorkflow(
                            state.workflowMenuWorkflowId
                    ),
                    getFilteredWorkflows(),
                    state.getSidebarScroll()
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

            state.hoveredNodeId = newHoveredId;

            state.hoveredNodeStartTime = System.currentTimeMillis();
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
     * 打开目标标点选择器
     */
    private void openMarkerPicker() {

        state.replacingMarkerStepId = null;

        state.markerPickerOpen = true;

        state.actionPickerOpen = false;
        state.workflowMenuOpen = false;
        state.nodeMenuOpen = false;

        state.markerScroll = 0;

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
        markerSearchBox.setFocused(true);
    }

    /*
     * 关闭目标标点选择器
     */
    private void closeMarkerPicker() {

        state.markerPickerOpen = false;

        state.replacingMarkerStepId = null;

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

        state.actionPickerStepId =
                step.getId();

        state.selectedStepId =
                step.getId();

        state.actionPickerOpen = true;

        state.markerPickerOpen = false;
        state.workflowMenuOpen = false;
        state.nodeMenuOpen = false;

        updateEditorBounds();
    }

    /*
     * 关闭 Action 选择器
     */
    private void closeActionPicker() {

        state.actionPickerOpen = false;

        state.actionPickerStepId = null;

        updateEditorBounds();
    }

    /*
     * 创建流程节点
     */
    private void addStepFromMarker(
            QuestMarker marker
    ) {

        if (state.currentWorkflow == null
                || marker == null) {
            return;
        }

        WorkflowStep step =
                WorkflowStep.create(
                        marker.getId(),
                        3.0D
                );

        state.currentWorkflow.addStep(step);

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

        step.addAction(action);

        state.selectedStepId =
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

        state.currentWorkflow.addStep(copy);

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

        state.currentWorkflow.removeStep(index);

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
                                state.workflows.size()
                                        + 1
                        )
                );

        state.workflows.add(workflow);

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

        state.workflows.remove(
                workflow
        );

        state.pinnedWorkflows.remove(
                workflowId
        );

        if (state.workflows.isEmpty()) {

            Workflow fallback =
                    Workflow.create(
                            "新建工作流"
                    );

            state.workflows.add(fallback);
        }

        if (state.currentWorkflow == workflow
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

        state.sidebarScroll = 0.0D;

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

        renameBox.setFocused(true);

        updateEditorBounds();
    }

    /*
     * 确认重命名
     */
    private void confirmRename() {

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

        state.renameDialogOpen = false;

        state.workflowMenuWorkflowId = null;

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
                        - HEADER_HEIGHT
                        - FOOTER_HEIGHT;

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
     * 保存
     */
    private void saveWorkflow() {

        state.dirty = false;

        markStatus(
                "已保存",
                COLOR_SUCCESS
        );
    }

    /*
     * 测试
     */
    private void testWorkflow() {

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
                HEADER_HEIGHT + 8,
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
                        - (int) state.sidebarScroll;

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

                    state.workflowMenuOpen = true;

                    state.workflowMenuWorkflowId =
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

            result.add(workflow);
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
     * 修改状态
     */
    private void markDirty(
            String text
    ) {

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

        return state.sidebarCollapsed
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
                + state.panX
                + worldX * state.zoom;
    }

    private double worldToScreenY(
            double worldY
    ) {

        return HEADER_HEIGHT
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
                        - HEADER_HEIGHT
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
