package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen;

import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.Workflow;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowAction;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowStep;

import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

public class WorkflowScreenInputHandler {

    private static final int NODE_MENU_WIDTH = 150;
    private static final int NODE_MENU_HEIGHT = 132;

    private static final int ACTION_POPUP_WIDTH = 520;
    private static final int ACTION_POPUP_HEIGHT = 360;

    private static final int MARKER_POPUP_WIDTH = 620;
    private static final int MARKER_POPUP_HEIGHT = 520;

    private static final int RENAME_POPUP_WIDTH = 380;
    private static final int RENAME_POPUP_HEIGHT = 150;

    private static final int HEADER_HEIGHT = 52;
    private static final int FOOTER_HEIGHT = 34;

    private static final int SIDEBAR_EXPANDED_WIDTH = 158;
    private static final int SIDEBAR_COLLAPSED_WIDTH = 42;

    private static final int WORKFLOW_ITEM_HEIGHT = 52;

    private static final int WORKFLOW_MENU_WIDTH = 128;
    private static final int WORKFLOW_MENU_HEIGHT = 72;

    private static final double MIN_ZOOM = 0.45D;
    private static final double MAX_ZOOM = 1.80D;

    private static final int COLOR_WARNING = 0xFFFFC66D;

    @FunctionalInterface
    public interface MouseClickHandler {

        boolean handle(
                double mouseX,
                double mouseY,
                int button
        );
    }

    @FunctionalInterface
    public interface MouseActionHandler {

        boolean handle(
                double mouseX,
                double mouseY
        );
    }

    @FunctionalInterface
    public interface StepFinder {

        WorkflowStep find(
                UUID stepId
        );
    }

    @FunctionalInterface
    public interface StepActionHandler {

        void handle(
                WorkflowStep step
        );
    }

    @FunctionalInterface
    public interface ActionHandler {

        void handle(
                WorkflowAction action
        );
    }

    @FunctionalInterface
    public interface MarkerProvider {

        List<QuestMarker> get();
    }

    @FunctionalInterface
    public interface MarkerReplaceHandler {

        void handle(
                WorkflowStep step,
                QuestMarker marker
        );
    }

    @FunctionalInterface
    public interface MarkerAddHandler {

        void handle(
                QuestMarker marker
        );
    }

    @FunctionalInterface
    public interface WorkflowFinder {

        Workflow find(
                UUID workflowId
        );
    }

    @FunctionalInterface
    public interface WorkflowListProvider {

        List<Workflow> get();
    }

    @FunctionalInterface
    public interface WorkflowActionHandler {

        void handle(
                Workflow workflow
        );
    }

    @FunctionalInterface
    public interface MouseDraggedHandler {

        boolean handle(
                double mouseX,
                double mouseY,
                int button,
                double dragX,
                double dragY
        );
    }

    @FunctionalInterface
    public interface MouseReleasedHandler {

        boolean handle(
                double mouseX,
                double mouseY,
                int button
        );
    }

    @FunctionalInterface
    public interface MouseScrolledHandler {

        boolean handle(
                double mouseX,
                double mouseY,
                double delta
        );
    }

    @FunctionalInterface
    public interface KeyPressedHandler {

        boolean handle(
                int keyCode,
                int scanCode,
                int modifiers
        );
    }

    private final WorkflowScreenState state;

    private final MouseClickHandler superClickHandler;

    private final MouseActionHandler headerClickHandler;
    private final MouseClickHandler sidebarClickHandler;
    private final MouseClickHandler canvasClickHandler;

    private final StepFinder stepFinder;

    private final StepActionHandler openMarkerPickerHandler;
    private final StepActionHandler openActionPickerHandler;
    private final StepActionHandler duplicateNodeHandler;
    private final StepActionHandler deleteNodeHandler;

    private final ActionHandler addActionHandler;
    private final Runnable closeActionPickerHandler;

    private final MarkerProvider markerProvider;
    private final MarkerReplaceHandler replaceMarkerHandler;
    private final MarkerAddHandler addMarkerHandler;
    private final Runnable closeMarkerPickerHandler;

    private final WorkflowFinder workflowFinder;
    private final WorkflowListProvider workflowListProvider;

    private final WorkflowActionHandler openRenameDialogHandler;
    private final WorkflowActionHandler deleteWorkflowHandler;

    private final Runnable closeRenameDialogHandler;
    private final Runnable confirmRenameHandler;
    private final Runnable saveWorkflowHandler;
    private final Runnable deleteSelectedNodeHandler;
    private final Runnable nodePositionChangedHandler;
    private final Runnable onCloseHandler;

    private final MouseDraggedHandler superMouseDraggedHandler;
    private final MouseReleasedHandler superMouseReleasedHandler;
    private final MouseScrolledHandler superMouseScrolledHandler;
    private final KeyPressedHandler superKeyPressedHandler;

    private int screenWidth;
    private int screenHeight;

    public WorkflowScreenInputHandler(
            WorkflowScreenState state,

            WorkflowFinder workflowFinder,
            WorkflowListProvider workflowListProvider,

            WorkflowActionHandler openRenameDialogHandler,
            WorkflowActionHandler deleteWorkflowHandler,

            ActionHandler addActionHandler,
            Runnable closeActionPickerHandler,

            MarkerProvider markerProvider,
            MarkerReplaceHandler replaceMarkerHandler,
            MarkerAddHandler addMarkerHandler,
            Runnable closeMarkerPickerHandler,

            StepFinder stepFinder,
            StepActionHandler openMarkerPickerHandler,
            StepActionHandler openActionPickerHandler,
            StepActionHandler duplicateNodeHandler,
            StepActionHandler deleteNodeHandler,

            Runnable closeRenameDialogHandler,
            Runnable confirmRenameHandler,
            Runnable saveWorkflowHandler,
            Runnable deleteSelectedNodeHandler,
            Runnable nodePositionChangedHandler,
            Runnable onCloseHandler,

            MouseClickHandler superClickHandler,
            MouseActionHandler headerClickHandler,
            MouseClickHandler sidebarClickHandler,
            MouseClickHandler canvasClickHandler,

            MouseDraggedHandler superMouseDraggedHandler,
            MouseReleasedHandler superMouseReleasedHandler,
            MouseScrolledHandler superMouseScrolledHandler,
            KeyPressedHandler superKeyPressedHandler
    ) {

        this.state = state;

        this.workflowFinder =
                workflowFinder;

        this.workflowListProvider =
                workflowListProvider;

        this.openRenameDialogHandler =
                openRenameDialogHandler;

        this.deleteWorkflowHandler =
                deleteWorkflowHandler;

        this.addActionHandler =
                addActionHandler;

        this.closeActionPickerHandler =
                closeActionPickerHandler;

        this.markerProvider =
                markerProvider;

        this.replaceMarkerHandler =
                replaceMarkerHandler;

        this.addMarkerHandler =
                addMarkerHandler;

        this.closeMarkerPickerHandler =
                closeMarkerPickerHandler;

        this.stepFinder =
                stepFinder;

        this.openMarkerPickerHandler =
                openMarkerPickerHandler;

        this.openActionPickerHandler =
                openActionPickerHandler;

        this.duplicateNodeHandler =
                duplicateNodeHandler;

        this.deleteNodeHandler =
                deleteNodeHandler;

        this.closeRenameDialogHandler =
                closeRenameDialogHandler;

        this.confirmRenameHandler =
                confirmRenameHandler;

        this.saveWorkflowHandler =
                saveWorkflowHandler;

        this.deleteSelectedNodeHandler =
                deleteSelectedNodeHandler;

        this.nodePositionChangedHandler =
                nodePositionChangedHandler;

        this.onCloseHandler =
                onCloseHandler;

        this.superClickHandler =
                superClickHandler;

        this.headerClickHandler =
                headerClickHandler;

        this.sidebarClickHandler =
                sidebarClickHandler;

        this.canvasClickHandler =
                canvasClickHandler;

        this.superMouseDraggedHandler =
                superMouseDraggedHandler;

        this.superMouseReleasedHandler =
                superMouseReleasedHandler;

        this.superMouseScrolledHandler =
                superMouseScrolledHandler;

        this.superKeyPressedHandler =
                superKeyPressedHandler;
    }

    /*
     * 更新输入上下文
     */
    public void updateContext(
            int screenWidth,
            int screenHeight
    ) {

        this.screenWidth =
                screenWidth;

        this.screenHeight =
                screenHeight;
    }

    /*
     * 处理鼠标点击总入口
     */
    public boolean handleMouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (state.renameDialogOpen) {

            if (handleRenameDialogClick(
                    mouseX,
                    mouseY,
                    button
            )) {

                return true;
            }

            return superClickHandler.handle(
                    mouseX,
                    mouseY,
                    button
            );
        }

        if (state.markerPickerOpen) {

            return handleMarkerPickerClick(
                    mouseX,
                    mouseY,
                    button
            );
        }

        if (state.actionPickerOpen) {

            return handleActionPickerClick(
                    mouseX,
                    mouseY,
                    button
            );
        }

        if (state.nodeMenuOpen) {

            if (handleNodeMenuClick(
                    mouseX,
                    mouseY
            )) {

                return true;
            }

            state.nodeMenuOpen = false;

            return true;
        }

        if (state.workflowMenuOpen) {

            if (handleWorkflowMenuClick(
                    mouseX,
                    mouseY,
                    button
            )) {

                return true;
            }

            state.workflowMenuOpen = false;
        }

        if (superClickHandler.handle(
                mouseX,
                mouseY,
                button
        )) {

            return true;
        }

        if (button
                == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {

            state.panning = true;

            state.panStartMouseX =
                    (int) mouseX;

            state.panStartMouseY =
                    (int) mouseY;

            state.panStartX =
                    state.panX;

            state.panStartY =
                    state.panY;

            return true;
        }

        if (headerClickHandler.handle(
                mouseX,
                mouseY
        )) {

            return true;
        }

        if (sidebarClickHandler.handle(
                mouseX,
                mouseY,
                button
        )) {

            return true;
        }

        if (canvasClickHandler.handle(
                mouseX,
                mouseY,
                button
        )) {

            return true;
        }

        return true;
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

        if (!inside(
                mouseX,
                mouseY,
                x,
                y,
                RENAME_POPUP_WIDTH,
                RENAME_POPUP_HEIGHT
        )) {

            closeRenameDialogHandler.run();

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

            closeRenameDialogHandler.run();

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

            confirmRenameHandler.run();

            return true;
        }

        return false;
    }

    /*
     * 节点右键菜单点击
     */
    private boolean handleNodeMenuClick(
            double mouseX,
            double mouseY
    ) {

        WorkflowStep step =
                stepFinder.find(
                        state.nodeMenuStepId
                );

        if (step == null) {

            state.nodeMenuOpen = false;

            return false;
        }

        int x =
                state.nodeMenuX;

        int y =
                state.nodeMenuY;

        if (inside(
                mouseX,
                mouseY,
                x + 6,
                y + 7,
                NODE_MENU_WIDTH - 12,
                24
        )) {

            state.nodeMenuOpen = false;

            openMarkerPickerHandler.handle(
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

            state.nodeMenuOpen = false;

            openActionPickerHandler.handle(
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

            state.nodeMenuOpen = false;

            duplicateNodeHandler.handle(
                    step
            );

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

            state.nodeMenuOpen = false;

            deleteNodeHandler.handle(
                    step
            );

            return true;
        }

        return false;
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

        if (inside(
                mouseX,
                mouseY,
                x + ACTION_POPUP_WIDTH - 104,
                y + ACTION_POPUP_HEIGHT - 38,
                84,
                26
        )) {

            closeActionPickerHandler.run();

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

            addActionHandler.handle(
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

            addActionHandler.handle(
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

            addActionHandler.handle(
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

            addActionHandler.handle(
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

            closeActionPickerHandler.run();

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

        if (!inside(
                mouseX,
                mouseY,
                x,
                y,
                MARKER_POPUP_WIDTH,
                MARKER_POPUP_HEIGHT
        )) {

            closeMarkerPickerHandler.run();

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

            closeMarkerPickerHandler.run();

            return true;
        }

        List<QuestMarker> markers =
                markerProvider.get();

        int listTop =
                y + 92;

        int cardHeight = 58;
        int gap = 8;

        int cardY =
                listTop
                        - (int) state.markerScroll;

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

                if (state.replacingMarkerStepId != null) {

                    WorkflowStep step =
                            stepFinder.find(
                                    state.replacingMarkerStepId
                            );

                    if (step != null) {

                        replaceMarkerHandler.handle(
                                step,
                                marker
                        );
                    }

                } else {

                    addMarkerHandler.handle(
                            marker
                    );
                }

                closeMarkerPickerHandler.run();

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
                workflowFinder.find(
                        state.workflowMenuWorkflowId
                );

        if (workflow == null) {

            state.workflowMenuOpen = false;

            return false;
        }

        List<Workflow> visible =
                workflowListProvider.get();

        int itemIndex =
                visible.indexOf(
                        workflow
                );

        if (itemIndex < 0) {

            return false;
        }

        int listTop =
                HEADER_HEIGHT + 52;

        int itemY =
                listTop
                        + itemIndex
                        * WORKFLOW_ITEM_HEIGHT
                        - (int) state.sidebarScroll;

        int menuX =
                SIDEBAR_EXPANDED_WIDTH
                        - WORKFLOW_MENU_WIDTH
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
                WORKFLOW_MENU_WIDTH - 12,
                24
        )) {

            openRenameDialogHandler.handle(
                    workflow
            );

            return true;
        }

        if (inside(
                mouseX,
                mouseY,
                menuX + 6,
                menuY + 34,
                WORKFLOW_MENU_WIDTH - 12,
                24
        )) {

            deleteWorkflowHandler.handle(
                    workflow
            );

            return true;
        }

        return false;
    }

    /*
     * 鼠标拖动
     */
    public boolean handleMouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {

        if (state.markerPickerOpen
                || state.actionPickerOpen
                || state.renameDialogOpen) {

            return true;
        }

        if (state.panning
                && button
                == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {

            state.panX =
                    state.panStartX
                            + (
                            mouseX
                                    - state.panStartMouseX
                    );

            state.panY =
                    state.panStartY
                            + (
                            mouseY
                                    - state.panStartMouseY
                    );

            return true;
        }

        if (state.draggingNodeId != null
                && button
                == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

            WorkflowScreenState.NodePosition position =
                    state.nodePositions.get(
                            state.draggingNodeId
                    );

            if (position != null) {

                position.x =
                        screenToWorldX(
                                mouseX
                        )
                                - state.dragOffsetX;

                position.y =
                        screenToWorldY(
                                mouseY
                        )
                                - state.dragOffsetY;

                return true;
            }
        }

        return superMouseDraggedHandler.handle(
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
    public boolean handleMouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (state.markerPickerOpen
                || state.actionPickerOpen
                || state.renameDialogOpen) {

            return true;
        }

        if (button
                == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {

            state.panning = false;

            return true;
        }

        if (button
                == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && state.draggingNodeId != null) {

            nodePositionChangedHandler.run();

            state.draggingNodeId = null;

            return true;
        }

        return superMouseReleasedHandler.handle(
                mouseX,
                mouseY,
                button
        );
    }

    /*
     * 鼠标滚轮
     */
    public boolean handleMouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {

        if (state.renameDialogOpen) {

            return true;
        }

        if (state.actionPickerOpen) {

            return true;
        }

        if (state.markerPickerOpen) {

            List<QuestMarker> markers =
                    markerProvider.get();

            int viewportHeight =
                    MARKER_POPUP_HEIGHT
                            - 138;

            int contentHeight =
                    markers.size() * 66;

            state.markerScroll =
                    clamp(
                            state.markerScroll
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

        if (!state.sidebarCollapsed
                && inside(
                mouseX,
                mouseY,
                0,
                HEADER_HEIGHT,
                getSidebarWidth(),
                screenHeight
                        - HEADER_HEIGHT
                        - FOOTER_HEIGHT
        )) {

            List<Workflow> visible =
                    workflowListProvider.get();

            int listHeight =
                    visible.size()
                            * WORKFLOW_ITEM_HEIGHT;

            int viewportHeight =
                    screenHeight
                            - HEADER_HEIGHT
                            - FOOTER_HEIGHT
                            - 100;

            state.sidebarScroll =
                    clamp(
                            state.sidebarScroll
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
                screenWidth
                        - getSidebarWidth(),
                screenHeight
                        - HEADER_HEIGHT
                        - FOOTER_HEIGHT
        )) {

            double oldZoom =
                    state.zoom;

            double zoomFactor =
                    delta > 0
                            ? 1.10D
                            : 0.90D;

            double newZoom =
                    clamp(
                            state.zoom
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

                state.zoom = newZoom;

                int canvasX =
                        getSidebarWidth();

                state.panX =
                        mouseX
                                - canvasX
                                - worldX * state.zoom;

                state.panY =
                        mouseY
                                - HEADER_HEIGHT
                                - worldY * state.zoom;
            }

            return true;
        }

        return superMouseScrolledHandler.handle(
                mouseX,
                mouseY,
                delta
        );
    }

    /*
     * 键盘快捷键
     */
    public boolean handleKeyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {

        if (keyCode
                == GLFW.GLFW_KEY_ESCAPE) {

            if (state.renameDialogOpen) {

                closeRenameDialogHandler.run();

                return true;
            }

            if (state.markerPickerOpen) {

                closeMarkerPickerHandler.run();

                return true;
            }

            if (state.actionPickerOpen) {

                closeActionPickerHandler.run();

                return true;
            }

            if (state.nodeMenuOpen) {

                state.nodeMenuOpen = false;
                state.nodeMenuStepId = null;

                return true;
            }

            if (state.workflowMenuOpen) {

                state.workflowMenuOpen = false;
                state.workflowMenuWorkflowId = null;

                return true;
            }

            onCloseHandler.run();

            return true;
        }

        if (state.renameDialogOpen
                && keyCode
                == GLFW.GLFW_KEY_ENTER) {

            confirmRenameHandler.run();

            return true;
        }

        if (!state.markerPickerOpen
                && !state.actionPickerOpen
                && !state.renameDialogOpen
                && keyCode
                == GLFW.GLFW_KEY_DELETE) {

            deleteSelectedNodeHandler.run();

            return true;
        }

        if (!state.markerPickerOpen
                && !state.actionPickerOpen
                && !state.renameDialogOpen
                && keyCode
                == GLFW.GLFW_KEY_S
                && net.minecraft.client.gui.screens.Screen
                .hasControlDown()) {

            saveWorkflowHandler.run();

            return true;
        }

        return superKeyPressedHandler.handle(
                keyCode,
                scanCode,
                modifiers
        );
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

    /*
     * 世界坐标转换到屏幕坐标
     */
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

    /*
     * 屏幕坐标转换到世界坐标
     */
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