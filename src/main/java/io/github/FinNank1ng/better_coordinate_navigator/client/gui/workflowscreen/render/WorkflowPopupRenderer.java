package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render;

import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.WorkflowScreenState;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.layout.WorkflowFrameLayout;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.layout.WorkflowNodeActionEditorLayout;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.Workflow;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowAction;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowStep;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/*
 * 工作流弹窗渲染器
 */
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

    private static final int COLOR_CONTEXT_MENU = 0xFF0F1822;
    private static final int COLOR_CONTEXT_MENU_ITEM = 0xFF162330;
    private static final int COLOR_CONTEXT_MENU_HOVER = 0xFF20364A;
    private static final int COLOR_CONTEXT_MENU_BORDER = 0xFF3A5064;

    private static final int NODE_MENU_WIDTH = 150;
    private static final int NODE_MENU_HEIGHT = 132;

    private static final int MARKER_POPUP_WIDTH = 620;
    private static final int MARKER_POPUP_HEIGHT = 520;

    private static final int RENAME_POPUP_WIDTH = 380;
    private static final int RENAME_POPUP_HEIGHT = 150;

    private final WorkflowScreenState state;
    private final Font font;

    private double legacyMarkerScroll;
    private double legacySidebarScroll;

    private final boolean legacyMode;

    private int screenWidth;
    private int screenHeight;
    private int mouseX;
    private int mouseY;

    private WorkflowStep legacySelectedStep;
    private int legacySelectedActionIndex = -1;

    public WorkflowPopupRenderer(
            WorkflowScreenState state,
            Font font
    ) {
        this.state = state;
        this.font = font;
        this.legacyMode = false;
    }

    /*
     * 更新旧版弹窗渲染上下文
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
     * 更新旧版工作流界面当前选中的动作
     */
    public void setLegacySelectedActionIndex(
            int selectedActionIndex
    ) {
        this.legacySelectedActionIndex =
                selectedActionIndex;
    }

    /*
     * 绘制节点右键菜单
     */
    public void renderNodeMenu(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            WorkflowStep step
    ) {

        if (step == null) {
            return;
        }

        if (legacyMode) {
            legacySelectedStep = step;
        }

        int x = legacyMode
                ? mouseX + 6
                : state.getNodeMenuX();

        int y = legacyMode
                ? mouseY + 6
                : state.getNodeMenuY();

        if (x + NODE_MENU_WIDTH > screenWidth
                && screenWidth > 0) {
            x = screenWidth - NODE_MENU_WIDTH - 8;
        }

        if (y + NODE_MENU_HEIGHT > screenHeight
                && screenHeight > 0) {
            y = screenHeight - NODE_MENU_HEIGHT - 8;
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
                "编辑动作",
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
     * 兼容旧版 Action 选择器调用
     */
    public void renderActionPicker(
            GuiGraphics graphics
    ) {
        renderActionPicker(
                graphics,
                screenWidth,
                screenHeight,
                mouseX,
                mouseY
        );
    }

    /*
     * 兼容旧版任务点选择器调用
     */
    public void renderMarkerPicker(
            GuiGraphics graphics,
            List<QuestMarker> markers,
            double markerScroll
    ) {
        legacyMarkerScroll = markerScroll;

        renderMarkerPicker(
                graphics,
                screenWidth,
                screenHeight,
                mouseX,
                mouseY,
                markers
        );
    }

    /*
     * 兼容旧版工作流菜单调用
     */
    public void renderWorkflowMenu(
            GuiGraphics graphics,
            Workflow workflow,
            List<Workflow> visibleWorkflows,
            double sidebarScroll
    ) {
        legacySidebarScroll = sidebarScroll;

        renderWorkflowMenu(
                graphics,
                screenHeight,
                mouseX,
                mouseY,
                visibleWorkflows,
                workflow
        );
    }

    /*
     * 兼容旧版重命名对话框调用
     */
    public void renderRenameDialog(
            GuiGraphics graphics,
            net.minecraft.client.gui.components.EditBox renameBox
    ) {
        renderRenameDialog(
                graphics,
                screenWidth,
                screenHeight,
                mouseX,
                mouseY,
                renameBox != null && renameBox.isFocused()
        );
    }

    /*
     * 绘制节点编辑器
     */
    public void renderActionPicker(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight,
            int mouseX,
            int mouseY
    ) {

        drawOverlay(
                graphics,
                screenWidth,
                screenHeight
        );

        WorkflowStep step = findSelectedStep();

        int selectedActionIndex =
                legacyMode
                        ? legacySelectedActionIndex
                        : state.getSelectedActionIndex();

        WorkflowAction selectedAction =
                selectedActionIndex >= 0
                        && step != null
                        && selectedActionIndex < step.getActionCount()
                        ? getActionAt(
                        step,
                        selectedActionIndex
                )
                        : null;

        WorkflowNodeActionEditorLayout layout =
                WorkflowNodeActionEditorLayout.calculate(
                        screenWidth,
                        screenHeight,
                        selectedAction
                );

        double scale = layout.getScale();

        int popupX = layout.getPopupX();
        int popupY = layout.getPopupY();

        double designMouseX =
                (mouseX - popupX) / scale;

        double designMouseY =
                (mouseY - popupY) / scale;

        graphics.pose().pushPose();

        graphics.pose().translate(
                popupX,
                popupY,
                0
        );

        graphics.pose().scale(
                (float) scale,
                (float) scale,
                1.0F
        );

        int designMouseXInt =
                (int) designMouseX;

        int designMouseYInt =
                (int) designMouseY;

        drawPanel(
                graphics,
                0,
                0,
                WorkflowNodeActionEditorLayout.POPUP_WIDTH,
                WorkflowNodeActionEditorLayout.POPUP_HEIGHT
        );

        QuestMarker marker =
                step == null
                        ? null
                        : findMarker(
                        step.getMarkerId()
                );

        /*
         * 弹窗标题
         */
        drawScaledText(
                graphics,
                "编辑节点",
                layout.getPopupTitleX(),
                layout.getPopupTitleY(),
                1.85F,
                COLOR_TEXT
        );

        /*
         * 顶部节点信息
         */
        WorkflowNodeActionEditorLayout.Rect infoPanel =
                layout.getInfoPanel();

        drawPanelCard(
                graphics,
                infoPanel.x(),
                infoPanel.y(),
                infoPanel.width(),
                infoPanel.height(),
                COLOR_PANEL_ALT,
                COLOR_BORDER
        );

        String markerName =
                marker == null
                        ? "未设置任务点"
                        : marker.name;

        String positionText =
                marker == null
                        ? "坐标未知"
                        : String.format(
                        Locale.ROOT,
                        "X %.1f   Y %.1f   Z %.1f",
                        marker.x,
                        marker.y,
                        marker.z
                );

        String radiusText =
                step == null
                        ? "范围未知"
                        : String.format(
                        Locale.ROOT,
                        "触发范围 %.1f 格",
                        step.getTriggerRadius()
                );

        WorkflowNodeActionEditorLayout.Rect leftInfo =
                layout.infoColumn(0);

        WorkflowNodeActionEditorLayout.Rect centerInfo =
                layout.infoColumn(1);

        WorkflowNodeActionEditorLayout.Rect rightInfo =
                layout.infoColumn(2);

        drawCenteredScaledText(
                graphics,
                "◆ " + markerName,
                leftInfo.x(),
                layout.getInfoCenterY(),
                leftInfo.width(),
                1.65F,
                COLOR_TEXT
        );

        drawCenteredScaledText(
                graphics,
                positionText,
                centerInfo.x(),
                layout.getInfoCenterY(),
                centerInfo.width(),
                1.55F,
                COLOR_TEXT_SECONDARY
        );

        drawCenteredScaledText(
                graphics,
                radiusText,
                rightInfo.x(),
                layout.getInfoCenterY(),
                rightInfo.width(),
                1.55F,
                COLOR_TEXT_SECONDARY
        );

        /* 左侧：动作列表 */
        WorkflowNodeActionEditorLayout.Rect leftPanel =
                layout.getLeftPanel();

        drawPanelCard(
                graphics,
                leftPanel.x(),
                leftPanel.y(),
                leftPanel.width(),
                leftPanel.height(),
                COLOR_PANEL_ALT,
                COLOR_BORDER
        );

        drawScaledText(
                graphics,
                "动作",
                layout.getActionListHeaderX(),
                layout.getActionListHeaderY(),
                1.5F,
                COLOR_TEXT
        );

        if (step != null
                && !step.getActions().isEmpty()) {

            int index = 1;

            for (WorkflowAction action : step.getActions()) {

                int actionIndex = index - 1;

                if (!layout.actionItemFits(actionIndex)) {
                    break;
                }

                WorkflowNodeActionEditorLayout.Rect item =
                        layout.actionItem(actionIndex);

                boolean selected =
                        selectedActionIndex == actionIndex;

                boolean hovered =
                        item.contains(
                                designMouseXInt,
                                designMouseYInt
                        );

                drawPanelCard(
                        graphics,
                        item.x(),
                        item.y(),
                        item.width(),
                        item.height(),
                        selected || hovered
                                ? COLOR_NODE_HOVER
                                : COLOR_NODE,
                        selected || hovered
                                ? COLOR_ACCENT
                                : COLOR_BORDER
                );

                /*
                 * 动作列表标题
                 * 例如：1  发送消息
                 */
                drawScaledText(
                        graphics,
                        index
                                + "  "
                                + getActionTitle(
                                action.getType()
                        ),
                        item.x() + 10,
                        item.y() + 5,
                        1.4F,
                        COLOR_TEXT
                );

                String preview =
                        getActionPreview(action);

                if (preview.length() > 30) {
                    preview =
                            preview.substring(
                                    0,
                                    30
                            ) + "...";
                }

                /*
                 * 动作预览仍然保持小字号
                 */
                graphics.pose().pushPose();

                graphics.pose().translate(
                        item.x() + 10,
                        item.y() + 29,
                        0
                );

                graphics.pose().scale(
                        0.82F,
                        0.82F,
                        1.0F
                );

                graphics.drawString(
                        font,
                        Component.literal(preview),
                        0,
                        0,
                        COLOR_TEXT_MUTED
                );

                graphics.pose().popPose();

                index++;
            }

        } else {

            WorkflowNodeActionEditorLayout.Rect firstItem =
                    layout.actionItem(0);

            graphics.drawString(
                    font,
                    Component.literal("暂无动作"),
                    firstItem.x() + 10,
                    firstItem.y() + 8,
                    COLOR_TEXT_MUTED
            );
        }

        /* 右侧：动作配置 */
        WorkflowNodeActionEditorLayout.Rect configPanel =
                layout.getActionConfigPanel();

        drawPanelCard(
                graphics,
                configPanel.x(),
                configPanel.y(),
                configPanel.width(),
                configPanel.height(),
                COLOR_PANEL_ALT,
                COLOR_BORDER
        );

        drawScaledText(
                graphics,
                "动作配置",
                layout.getActionConfigTitleX(),
                layout.getActionConfigTitleY(),
                1.5F,
                COLOR_TEXT
        );

        drawScaledText(
                graphics,
                "选择动作类型",
                layout.getActionTypeLabelX(),
                layout.getActionTypeLabelY(),
                1.4F,
                COLOR_TEXT
        );

        drawActionTypeButtons(
                graphics,
                layout,
                designMouseXInt,
                designMouseYInt
        );

        /* 右侧：动作编辑 */
        WorkflowNodeActionEditorLayout.Rect editorPanel =
                layout.getActionEditorPanel();

        drawPanelCard(
                graphics,
                editorPanel.x(),
                editorPanel.y(),
                editorPanel.width(),
                editorPanel.height(),
                COLOR_PANEL_ALT,
                COLOR_BORDER
        );

        drawScaledText(
                graphics,
                "动作编辑",
                layout.getActionEditorHeaderX(),
                layout.getActionEditorHeaderY(),
                1.5F,
                COLOR_TEXT
        );

        String selectedActionLabel =
                selectedAction == null
                        ? "请从左侧选择一个动作进行编辑"
                        : (selectedActionIndex + 1)
                        + "  "
                        + getActionTitle(
                        selectedAction.getType()
                );

        /*
         * 当前编辑动作
         */
        drawScaledText(
                graphics,
                selectedActionLabel,
                layout.getSelectedActionLabelX(),
                layout.getSelectedActionLabelY() - 2,
                1.4F,
                selectedAction == null
                        ? COLOR_TEXT_MUTED
                        : COLOR_TEXT
        );

        WorkflowNodeActionEditorLayout.Rect dataFrame =
                layout.getDataFrame();

        String fieldLabel;

        if (selectedAction == null) {
            fieldLabel = "动作数据";

        } else if (layout.isCommandAction()) {
            fieldLabel = "执行命令";

        } else if (layout.isGiveItem()) {
            fieldLabel = "物品 ID";

        } else if (selectedAction.getType()
                == WorkflowAction.Type.MESSAGE) {
            fieldLabel = "消息内容";

        } else if (selectedAction.getType()
                == WorkflowAction.Type.SOUND) {
            fieldLabel = "声音 ID";

        } else {
            fieldLabel = "数据";
        }

        /*
         * 数据字段标题
         */
        drawScaledText(
                graphics,
                fieldLabel,
                layout.getFieldLabelX(),
                layout.getFieldLabelY() - 2,
                1.4F,
                selectedAction == null
                        ? COLOR_TEXT_MUTED
                        : COLOR_TEXT_SECONDARY
        );

        drawProminentInputFrame(
                graphics,
                dataFrame.x(),
                dataFrame.y(),
                dataFrame.width(),
                dataFrame.height(),
                selectedAction != null
        );

        WorkflowNodeActionEditorLayout.Rect commandPrefix =
                layout.commandPrefix();

        if (commandPrefix != null) {

            graphics.fill(
                    commandPrefix.x()
                            + commandPrefix.width()
                            - 1,
                    commandPrefix.y() + 3,
                    commandPrefix.x()
                            + commandPrefix.width(),
                    commandPrefix.y()
                            + commandPrefix.height()
                            - 3,
                    COLOR_BORDER
            );

            int slashWidth =
                    font.width("/");

            int slashX =
                    commandPrefix.x()
                            + (commandPrefix.width() - slashWidth) / 2;

            int slashY =
                    commandPrefix.y()
                            + (commandPrefix.height() - font.lineHeight) / 2;

            graphics.drawString(
                    font,
                    Component.literal("/"),
                    slashX,
                    slashY,
                    COLOR_TEXT
            );
        }

        if (layout.isGiveItem()) {

            WorkflowNodeActionEditorLayout.Rect countFrame =
                    layout.getCountFrame();

            if (countFrame != null) {

                /*
                 * 数量标题
                 */
                drawScaledText(
                        graphics,
                        "数量",
                        countFrame.x(),
                        layout.getFieldLabelY() - 2,
                        1.5F,
                        COLOR_TEXT_SECONDARY
                );

                drawProminentInputFrame(
                        graphics,
                        countFrame.x(),
                        countFrame.y(),
                        countFrame.width(),
                        countFrame.height(),
                        true
                );
            }
        }

        /* 底部操作 */
        WorkflowNodeActionEditorLayout.Rect deleteButton =
                layout.deleteButton();

        WorkflowNodeActionEditorLayout.Rect cancelButton =
                layout.cancelButton();

        WorkflowNodeActionEditorLayout.Rect applyButton =
                layout.applyButton();

        /*
         * 删除动作
         */
        drawScaledPanelButton(
                graphics,
                deleteButton.x(),
                deleteButton.y(),
                deleteButton.width(),
                deleteButton.height(),
                "删除动作",
                deleteButton.contains(
                        designMouseXInt,
                        designMouseYInt
                ),
                0xFFFF6B6B,
                1.5F
        );

        /*
         * 取消
         */
        drawScaledPanelButton(
                graphics,
                cancelButton.x(),
                cancelButton.y(),
                cancelButton.width(),
                cancelButton.height(),
                "取消",
                cancelButton.contains(
                        designMouseXInt,
                        designMouseYInt
                ),
                COLOR_TEXT,
                1.5F
        );

        /*
         * 应用
         */
        drawScaledPanelButton(
                graphics,
                applyButton.x(),
                applyButton.y(),
                applyButton.width(),
                applyButton.height(),
                "应用",
                applyButton.contains(
                        designMouseXInt,
                        designMouseYInt
                ),
                COLOR_ACCENT,
                1.5F
        );

        graphics.pose().popPose();
    }

    /*
     * 绘制动作类型按钮区域
     */
    private void drawActionTypeButtons(
            GuiGraphics graphics,
            WorkflowNodeActionEditorLayout layout,
            int mouseX,
            int mouseY
    ) {

        String[] labels = {
                "消息",
                "命令",
                "物品",
                "声音"
        };

        WorkflowAction.Type[] types = {
                WorkflowAction.Type.MESSAGE,
                WorkflowAction.Type.EXECUTE_COMMAND,
                WorkflowAction.Type.GIVE_ITEM,
                WorkflowAction.Type.SOUND
        };

        for (int i = 0; i < labels.length; i++) {

            WorkflowNodeActionEditorLayout.Rect button =
                    layout.actionTypeButton(i);

            drawCompactActionButton(
                    graphics,
                    button.x(),
                    button.y(),
                    button.width(),
                    labels[i],
                    types[i],
                    mouseX,
                    mouseY
            );
        }
    }

    /*
     * 绘制紧凑动作按钮
     */
    private void drawCompactActionButton(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            String text,
            WorkflowAction.Type type,
            int mouseX,
            int mouseY
    ) {

        boolean hovered =
                inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        width,
                        26
                );

        /*
         * 四个动作类型按钮文字统一 1.5 倍
         */
        drawScaledPanelButton(
                graphics,
                x,
                y,
                width,
                26,
                text,
                hovered,
                hovered
                        ? COLOR_ACCENT
                        : COLOR_BORDER,
                1.4F
        );
    }

    /*
     * 按索引获取 Action
     */
    private WorkflowAction getActionAt(
            WorkflowStep step,
            int index
    ) {

        if (step == null || index < 0) {
            return null;
        }

        int current = 0;

        for (WorkflowAction action : step.getActions()) {

            if (current == index) {
                return action;
            }

            current++;
        }

        return null;
    }

    /*
     * 获取当前编辑节点
     */
    private WorkflowStep findSelectedStep() {

        if (legacyMode) {
            return legacySelectedStep;
        }

        if (state.getSelectedStepId() == null
                || state.getCurrentWorkflow() == null) {
            return null;
        }

        for (WorkflowStep step :
                state.getCurrentWorkflow().getSteps()) {

            if (state.getSelectedStepId().equals(
                    step.getId()
            )) {
                return step;
            }
        }

        return null;
    }

    /*
     * 查找任务点
     */
    private QuestMarker findMarker(
            java.util.UUID markerId
    ) {

        if (markerId == null) {
            return null;
        }

        for (QuestMarker marker :
                ClientQuestCache.getMarkers()) {

            if (markerId.equals(marker.getId())) {
                return marker;
            }
        }

        return null;
    }

    /*
     * 获取动作名称
     */
    private String getActionTitle(
            WorkflowAction.Type type
    ) {

        return switch (type) {
            case MESSAGE -> "发送消息";
            case EXECUTE_COMMAND -> "执行命令";
            case GIVE_ITEM -> "发放物品";
            case ENABLE_MARKER -> "启用标点";
            case DISABLE_MARKER -> "禁用标点";
            case SOUND -> "播放声音";
        };
    }

    /*
     * 获取动作预览文本
     */
    private String getActionPreview(
            WorkflowAction action
    ) {

        if (action == null
                || action.getData() == null) {
            return "";
        }

        if (action.getType()
                == WorkflowAction.Type.GIVE_ITEM) {

            return action.getData()
                    + " ×"
                    + action.getCount();
        }

        return action.getData();
    }

    /*
     * 绘制任务点选择器
     */
    public void renderMarkerPicker(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight,
            int mouseX,
            int mouseY,
            List<QuestMarker> markers
    ) {

        drawOverlay(
                graphics,
                screenWidth,
                screenHeight
        );

        int x =
                (screenWidth - MARKER_POPUP_WIDTH)
                        / 2;

        int y =
                (screenHeight - MARKER_POPUP_HEIGHT)
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
                Component.literal("选择目标标点"),
                x + 20,
                y + 20,
                COLOR_TEXT
        );

        graphics.drawString(
                font,
                Component.literal("从已有任务点中选择流程目标"),
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
                        * (cardHeight + gap);

        int viewportHeight =
                listBottom - listTop;

        double maxScroll =
                Math.max(
                        0,
                        contentHeight - viewportHeight
                );

        double markerScroll =
                clamp(
                        legacyMode
                                ? legacyMarkerScroll
                                : state.getMarkerScroll(),
                        0,
                        maxScroll
                );

        if (legacyMode) {
            legacyMarkerScroll = markerScroll;
        } else {
            state.setMarkerScroll(markerScroll);
        }

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

                String name = marker.name;

                if (name.length() > 55) {
                    name =
                            name.substring(
                                    0,
                                    55
                            ) + "...";
                }

                graphics.drawString(
                        font,
                        Component.literal(name),
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

            cardY += cardHeight + gap;
        }

        if (markers.isEmpty()) {

            graphics.drawString(
                    font,
                    Component.literal("没有找到任务点"),
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
            int screenHeight,
            int mouseX,
            int mouseY,
            List<Workflow> visibleWorkflows,
            Workflow workflow
    ) {

        if (workflow == null) {
            return;
        }

        int itemIndex =
                visibleWorkflows.indexOf(workflow);

        if (itemIndex < 0) {
            return;
        }

        int listTop =
                WorkflowFrameLayout.workflowListTop();

        int itemY =
                listTop
                        + itemIndex
                        * WorkflowFrameLayout.WORKFLOW_ITEM_HEIGHT
                        - (int) (
                        legacyMode
                                ? legacySidebarScroll
                                : state.getSidebarScroll()
                );

        int menuWidth =
                WorkflowFrameLayout.WORKFLOW_MENU_WIDTH;

        int menuHeight =
                WorkflowFrameLayout.WORKFLOW_MENU_HEIGHT;

        int menuX =
                WorkflowFrameLayout.workflowMenuX();

        int menuY =
                WorkflowFrameLayout.workflowMenuY(
                        itemY,
                        screenHeight
                );

        drawWorkflowMenuPanel(
                graphics,
                menuX,
                menuY,
                menuWidth,
                menuHeight
        );

        drawWorkflowMenuItem(
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

        drawWorkflowMenuItem(
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
            int screenWidth,
            int screenHeight,
            int mouseX,
            int mouseY,
            boolean renameFocused
    ) {

        drawOverlay(
                graphics,
                screenWidth,
                screenHeight
        );

        int x =
                (screenWidth - RENAME_POPUP_WIDTH)
                        / 2;

        int y =
                (screenHeight - RENAME_POPUP_HEIGHT)
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
                Component.literal("重命名工作流"),
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
                renameFocused
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
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight
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
     * 通用面板
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
     * 工作流右键菜单浮层
     */
    private void drawWorkflowMenuPanel(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height
    ) {

        /*
         * 外部阴影
         */
        graphics.fill(
                x + 5,
                y + 5,
                x + width + 5,
                y + height + 5,
                0x88000000
        );

        /*
         * 主体
         */
        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                COLOR_CONTEXT_MENU
        );

        /*
         * 外边框
         */
        graphics.fill(
                x,
                y,
                x + width,
                y + 1,
                COLOR_CONTEXT_MENU_BORDER
        );

        graphics.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                COLOR_CONTEXT_MENU_BORDER
        );

        graphics.fill(
                x,
                y,
                x + 1,
                y + height,
                COLOR_CONTEXT_MENU_BORDER
        );

        graphics.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                COLOR_CONTEXT_MENU_BORDER
        );

        /*
         * 顶部内侧高光线
         */
        graphics.fill(
                x + 2,
                y + 2,
                x + width - 2,
                y + 3,
                0xFF25394B
        );

        /*
         * 两个菜单项之间的分隔线
         */
        graphics.fill(
                x + 8,
                y + 33,
                x + width - 8,
                y + 34,
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
                y + (height - 8) / 2,
                hovered
                        ? accent
                        : COLOR_TEXT
        );
    }

    /*
     * 缩放文字按钮
     *
     * 只放大按钮中的文字，
     * 不改变按钮本身的尺寸。
     */
    private void drawScaledPanelButton(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            String text,
            boolean hovered,
            int accent,
            float scale
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

        float scaledWidth =
                textWidth * scale;

        float scaledHeight =
                font.lineHeight * scale;

        float drawX =
                x + (width - scaledWidth) / 2.0F;

        float drawY =
                y + (height - scaledHeight) / 2.0F;

        graphics.pose().pushPose();

        graphics.pose().translate(
                drawX,
                drawY,
                0
        );

        graphics.pose().scale(
                scale,
                scale,
                1.0F
        );

        graphics.drawString(
                font,
                Component.literal(text),
                0,
                0,
                hovered
                        ? accent
                        : COLOR_TEXT
        );

        graphics.pose().popPose();
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
     * 工作流右键菜单项
     */
    private void drawWorkflowMenuItem(
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
                        ? COLOR_CONTEXT_MENU_HOVER
                        : COLOR_CONTEXT_MENU_ITEM;

        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                fill
        );

        /*
         * 左侧强调线
         */
        if (hovered) {

            graphics.fill(
                    x,
                    y,
                    x + 2,
                    y + height,
                    COLOR_ACCENT
            );
        }

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
     * 左 / 中 / 右信息栏使用的居中文字
     */
    private void drawCenteredScaledText(
            GuiGraphics graphics,
            String text,
            int x,
            int centerY,
            int width,
            float scale,
            int color
    ) {

        int textWidth =
                font.width(text);

        float scaledWidth =
                textWidth * scale;

        float drawX =
                x + (width - scaledWidth) / 2.0F;

        float drawY =
                centerY
                        - (font.lineHeight * scale) / 2.0F;

        graphics.pose().pushPose();

        graphics.pose().translate(
                drawX,
                drawY,
                0
        );

        graphics.pose().scale(
                scale,
                scale,
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
     * 普通缩放文字
     */
    private void drawScaledText(
            GuiGraphics graphics,
            String text,
            int x,
            int y,
            float scale,
            int color
    ) {

        graphics.pose().pushPose();

        graphics.pose().translate(
                x,
                y,
                0
        );

        graphics.pose().scale(
                scale,
                scale,
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
     * 强调型输入框
     */
    private void drawProminentInputFrame(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            boolean enabled
    ) {

        int outerBorder =
                enabled
                        ? 0xFF46627B
                        : 0xFF334250;

        int innerBorder =
                enabled
                        ? 0xFF22384B
                        : 0xFF202C36;

        int background =
                enabled
                        ? 0xFF0C141D
                        : 0xFF111821;

        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                background
        );

        graphics.fill(
                x,
                y,
                x + width,
                y + 2,
                outerBorder
        );

        graphics.fill(
                x,
                y + height - 2,
                x + width,
                y + height,
                outerBorder
        );

        graphics.fill(
                x,
                y,
                x + 2,
                y + height,
                outerBorder
        );

        graphics.fill(
                x + width - 2,
                y,
                x + width,
                y + height,
                outerBorder
        );

        graphics.fill(
                x + 2,
                y + 2,
                x + width - 2,
                y + 3,
                innerBorder
        );

        graphics.fill(
                x + 2,
                y + height - 3,
                x + width - 2,
                y + height - 2,
                innerBorder
        );

        graphics.fill(
                x + 2,
                y + 2,
                x + 3,
                y + height - 2,
                innerBorder
        );

        graphics.fill(
                x + width - 3,
                y + 2,
                x + width - 2,
                y + height - 2,
                innerBorder
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

        /*
         * 真正的输入框：使用比面板更深的底色
         */
        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                0xFF101821
        );

        graphics.fill(
                x,
                y,
                x + width,
                y + 2,
                border
        );

        graphics.fill(
                x,
                y + height - 2,
                x + width,
                y + height,
                border
        );

        graphics.fill(
                x,
                y,
                x + 2,
                y + height,
                border
        );

        graphics.fill(
                x + width - 2,
                y,
                x + width,
                y + height,
                border
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
}