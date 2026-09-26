package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.layout;

import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowAction;

/**
 * 工作流节点动作编辑界面的统一布局
 */
public final class WorkflowNodeActionEditorLayout {

    public static final int POPUP_WIDTH = 1100;
    public static final int POPUP_HEIGHT = 620;

    private static final int TITLE_HEIGHT = 38;
    private static final int INFO_HEIGHT = 50;
    private static final int BODY_GAP = 12;

    private static final int FOOTER_HEIGHT = 44;
    private static final int BODY_BOTTOM_GAP = 8;

    private static final int LEFT_WIDTH = 280;
    private static final int RIGHT_GAP = 12;
    private static final int OUTER_PADDING = 16;
    private static final int PANEL_PADDING = 14;

    private static final int CONFIG_HEIGHT = 128;
    private static final int EDITOR_GAP = 12;

    private static final int ACTION_LIST_TOP_OFFSET = 30;
    private static final int ACTION_LIST_ITEM_HEIGHT = 52;
    private static final int ACTION_LIST_ITEM_GAP = 6;
    private static final int ACTION_LIST_BOTTOM_GAP = 12;

    private static final int ACTION_TYPE_TOP_OFFSET = 58;
    private static final int ACTION_TYPE_BUTTON_WIDTH = 82;
    private static final int ACTION_TYPE_BUTTON_HEIGHT = 26;
    private static final int ACTION_TYPE_BUTTON_GAP = 6;

    private static final int EDITOR_TITLE_OFFSET = 34;
    private static final int FIELD_LABEL_OFFSET = 61;
    private static final int INPUT_OFFSET = 82;
    private static final int INPUT_FRAME_HEIGHT = 42;
    private static final int EDITBOX_HEIGHT = 20;

    private static final int EDITBOX_VERTICAL_INSET =
            (INPUT_FRAME_HEIGHT - EDITBOX_HEIGHT) / 2;

    private static final int EDITBOX_HORIZONTAL_INSET = 8;

    private static final int COUNT_WIDTH = 110;
    private static final int COUNT_GAP = 12;

    private static final int COMMAND_PREFIX_WIDTH = 28;

    private static final int DELETE_BUTTON_WIDTH = 90;
    private static final int DELETE_BUTTON_HEIGHT = 26;
    private static final int DELETE_BUTTON_RIGHT_GAP = 290;

    private static final int CANCEL_BUTTON_WIDTH = 78;
    private static final int CANCEL_BUTTON_HEIGHT = 26;
    private static final int CANCEL_BUTTON_RIGHT_GAP = 186;

    private static final int APPLY_BUTTON_WIDTH = 84;
    private static final int APPLY_BUTTON_HEIGHT = 26;
    private static final int APPLY_BUTTON_RIGHT_GAP = 100;

    private final double scale;
    private final int popupX;
    private final int popupY;

    private final Rect infoPanel;
    private final Rect leftPanel;
    private final Rect rightPanel;
    private final Rect actionConfigPanel;
    private final Rect actionEditorPanel;

    private final int bodyY;
    private final int footerY;
    private final int bodyHeight;

    private final int popupTitleX;
    private final int popupTitleY;

    private final int infoContentX;
    private final int infoContentWidth;
    private final int infoColumnWidth;
    private final int infoCenterY;

    private final int actionListHeaderX;
    private final int actionListHeaderY;
    private final int actionConfigTitleX;
    private final int actionConfigTitleY;
    private final int actionTypeLabelX;
    private final int actionTypeLabelY;
    private final int actionEditorHeaderX;
    private final int actionEditorHeaderY;
    private final int selectedActionLabelX;
    private final int selectedActionLabelY;
    private final int fieldLabelX;

    private final int actionEditorTitleY;
    private final int fieldLabelY;

    private final Rect dataFrame;
    private final Rect dataInput;
    private final Rect countFrame;
    private final Rect countInput;

    private final boolean commandAction;
    private final boolean giveItem;

    private WorkflowNodeActionEditorLayout(
            double scale,
            int popupX,
            int popupY,
            Rect infoPanel,
            Rect leftPanel,
            Rect rightPanel,
            Rect actionConfigPanel,
            Rect actionEditorPanel,
            int bodyY,
            int footerY,
            int bodyHeight,
            int popupTitleX,
            int popupTitleY,
            int infoContentX,
            int infoContentWidth,
            int infoColumnWidth,
            int infoCenterY,
            int actionListHeaderX,
            int actionListHeaderY,
            int actionConfigTitleX,
            int actionConfigTitleY,
            int actionTypeLabelX,
            int actionTypeLabelY,
            int actionEditorHeaderX,
            int actionEditorHeaderY,
            int selectedActionLabelX,
            int selectedActionLabelY,
            int fieldLabelX,
            int actionEditorTitleY,
            int fieldLabelY,
            Rect dataFrame,
            Rect dataInput,
            Rect countFrame,
            Rect countInput,
            boolean commandAction,
            boolean giveItem
    ) {
        this.scale = scale;
        this.popupX = popupX;
        this.popupY = popupY;
        this.infoPanel = infoPanel;
        this.leftPanel = leftPanel;
        this.rightPanel = rightPanel;
        this.actionConfigPanel = actionConfigPanel;
        this.actionEditorPanel = actionEditorPanel;
        this.bodyY = bodyY;
        this.footerY = footerY;
        this.bodyHeight = bodyHeight;
        this.popupTitleX = popupTitleX;
        this.popupTitleY = popupTitleY;
        this.infoContentX = infoContentX;
        this.infoContentWidth = infoContentWidth;
        this.infoColumnWidth = infoColumnWidth;
        this.infoCenterY = infoCenterY;
        this.actionListHeaderX = actionListHeaderX;
        this.actionListHeaderY = actionListHeaderY;
        this.actionConfigTitleX = actionConfigTitleX;
        this.actionConfigTitleY = actionConfigTitleY;
        this.actionTypeLabelX = actionTypeLabelX;
        this.actionTypeLabelY = actionTypeLabelY;
        this.actionEditorHeaderX = actionEditorHeaderX;
        this.actionEditorHeaderY = actionEditorHeaderY;
        this.selectedActionLabelX = selectedActionLabelX;
        this.selectedActionLabelY = selectedActionLabelY;
        this.fieldLabelX = fieldLabelX;
        this.actionEditorTitleY = actionEditorTitleY;
        this.fieldLabelY = fieldLabelY;
        this.dataFrame = dataFrame;
        this.dataInput = dataInput;
        this.countFrame = countFrame;
        this.countInput = countInput;
        this.commandAction = commandAction;
        this.giveItem = giveItem;
    }

    public static WorkflowNodeActionEditorLayout calculate(
            int screenWidth,
            int screenHeight,
            WorkflowAction action
    ) {
        double scaleX =
                (screenWidth - 24.0D)
                        / POPUP_WIDTH;

        double scaleY =
                (screenHeight - 24.0D)
                        / POPUP_HEIGHT;

        double scale = Math.max(
                0.55D,
                Math.min(
                        1.0D,
                        Math.min(scaleX, scaleY)
                )
        );

        int scaledWidth =
                (int) Math.round(
                        POPUP_WIDTH * scale
                );

        int scaledHeight =
                (int) Math.round(
                        POPUP_HEIGHT * scale
                );

        int popupX =
                (screenWidth - scaledWidth) / 2;

        int popupY =
                (screenHeight - scaledHeight) / 2;

        int infoY = TITLE_HEIGHT;

        Rect infoPanel = new Rect(
                OUTER_PADDING,
                infoY,
                POPUP_WIDTH - OUTER_PADDING * 2,
                INFO_HEIGHT
        );

        int bodyY =
                infoY
                        + INFO_HEIGHT
                        + BODY_GAP;

        int footerY =
                POPUP_HEIGHT
                        - FOOTER_HEIGHT;

        int bodyHeight =
                footerY
                        - bodyY
                        - BODY_BOTTOM_GAP;

        int rightX =
                OUTER_PADDING
                        + LEFT_WIDTH
                        + RIGHT_GAP;

        int rightWidth =
                POPUP_WIDTH
                        - LEFT_WIDTH
                        - 44;

        Rect leftPanel = new Rect(
                OUTER_PADDING,
                bodyY,
                LEFT_WIDTH,
                bodyHeight
        );

        Rect rightPanel = new Rect(
                rightX,
                bodyY,
                rightWidth,
                bodyHeight
        );

        Rect actionConfigPanel = new Rect(
                rightX,
                bodyY,
                rightWidth,
                CONFIG_HEIGHT
        );

        int editorY =
                bodyY
                        + CONFIG_HEIGHT
                        + EDITOR_GAP;

        int editorHeight =
                footerY
                        - editorY
                        - BODY_BOTTOM_GAP;

        Rect actionEditorPanel = new Rect(
                rightX,
                editorY,
                rightWidth,
                editorHeight
        );

        int actionEditorTitleY =
                editorY
                        + EDITOR_TITLE_OFFSET;

        int fieldLabelY =
                editorY
                        + FIELD_LABEL_OFFSET;

        int popupTitleX = 20;
        int popupTitleY = 16;

        int infoContentX = 24;
        int infoContentWidth = POPUP_WIDTH - 48;
        int infoColumnWidth = infoContentWidth / 3;
        int infoCenterY = infoY + INFO_HEIGHT / 2;

        int actionListHeaderX =
                leftPanel.x() + 12;
        int actionListHeaderY =
                leftPanel.y() + 10;

        int actionConfigTitleX =
                actionConfigPanel.x() + 12;
        int actionConfigTitleY =
                actionConfigPanel.y() + 10;

        int actionTypeLabelX =
                actionConfigPanel.x() + PANEL_PADDING;
        int actionTypeLabelY =
                actionConfigPanel.y() + 34;

        int actionEditorHeaderX =
                actionEditorPanel.x() + 12;
        int actionEditorHeaderY =
                actionEditorPanel.y() + 10;

        int selectedActionLabelX =
                actionEditorPanel.x() + PANEL_PADDING;
        int selectedActionLabelY =
                actionEditorPanel.y() + EDITOR_TITLE_OFFSET;

        int inputX =
                rightX
                        + PANEL_PADDING;

        int inputY =
                editorY
                        + INPUT_OFFSET;

        int inputWidth =
                rightWidth
                        - PANEL_PADDING * 2;

        int fieldLabelX =
                inputX;

        boolean commandAction =
                action != null
                        && action.getType()
                        == WorkflowAction.Type.EXECUTE_COMMAND;

        boolean giveItem =
                action != null
                        && action.getType()
                        == WorkflowAction.Type.GIVE_ITEM;

        int dataFrameWidth =
                giveItem
                        ? inputWidth
                        - COUNT_WIDTH
                        - COUNT_GAP
                        : inputWidth;

        dataFrameWidth = Math.max(
                40,
                dataFrameWidth
        );

        Rect dataFrame = new Rect(
                inputX,
                inputY,
                dataFrameWidth,
                INPUT_FRAME_HEIGHT
        );

        int editableX =
                inputX
                        + (commandAction
                        ? COMMAND_PREFIX_WIDTH
                        : 0);

        int editableWidth =
                dataFrameWidth
                        - (commandAction
                        ? COMMAND_PREFIX_WIDTH
                        : 0);

        editableWidth = Math.max(
                20,
                editableWidth
        );

        int editboxX =
                editableX
                        + EDITBOX_HORIZONTAL_INSET;

        int editboxWidth =
                editableWidth
                        - EDITBOX_HORIZONTAL_INSET;

        Rect dataInput = new Rect(
                editboxX,
                inputY + EDITBOX_VERTICAL_INSET,
                Math.max(
                        20,
                        editboxWidth
                ),
                EDITBOX_HEIGHT
        );

        Rect countFrame = null;
        Rect countInput = null;

        if (giveItem) {
            int countX =
                    inputX
                            + dataFrameWidth
                            + COUNT_GAP;

            countFrame = new Rect(
                    countX,
                    inputY,
                    COUNT_WIDTH,
                    INPUT_FRAME_HEIGHT
            );

            countInput = new Rect(
                    countX + EDITBOX_HORIZONTAL_INSET,
                    inputY + EDITBOX_VERTICAL_INSET,
                    Math.max(
                            20,
                            COUNT_WIDTH
                                    - EDITBOX_HORIZONTAL_INSET
                    ),
                    EDITBOX_HEIGHT
            );
        }

        return new WorkflowNodeActionEditorLayout(
                scale,
                popupX,
                popupY,
                infoPanel,
                leftPanel,
                rightPanel,
                actionConfigPanel,
                actionEditorPanel,
                bodyY,
                footerY,
                bodyHeight,
                popupTitleX,
                popupTitleY,
                infoContentX,
                infoContentWidth,
                infoColumnWidth,
                infoCenterY,
                actionListHeaderX,
                actionListHeaderY,
                actionConfigTitleX,
                actionConfigTitleY,
                actionTypeLabelX,
                actionTypeLabelY,
                actionEditorHeaderX,
                actionEditorHeaderY,
                selectedActionLabelX,
                selectedActionLabelY,
                fieldLabelX,
                actionEditorTitleY,
                fieldLabelY,
                dataFrame,
                dataInput,
                countFrame,
                countInput,
                commandAction,
                giveItem
        );
    }

    public Rect actionItem(
            int index
    ) {
        return new Rect(
                leftPanel.x() + 8,
                leftPanel.y()
                        + ACTION_LIST_TOP_OFFSET
                        + index
                        * (
                        ACTION_LIST_ITEM_HEIGHT
                                + ACTION_LIST_ITEM_GAP
                ),
                leftPanel.width() - 16,
                ACTION_LIST_ITEM_HEIGHT
        );
    }

    public boolean actionItemFits(
            int index
    ) {
        Rect rect = actionItem(index);

        return rect.y() + rect.height()
                <= leftPanel.y()
                + leftPanel.height()
                - ACTION_LIST_BOTTOM_GAP;
    }

    public int getActionTypeButtonCount(
            int availableWidth
    ) {
        return Math.max(
                1,
                (
                        availableWidth
                                + ACTION_TYPE_BUTTON_GAP
                ) / (
                        ACTION_TYPE_BUTTON_WIDTH
                                + ACTION_TYPE_BUTTON_GAP
                )
        );
    }

    public Rect actionTypeButton(
            int index
    ) {
        int availableWidth =
                actionConfigPanel.width()
                        - PANEL_PADDING * 2;

        int columns =
                getActionTypeButtonCount(
                        availableWidth
                );

        int row =
                index / columns;

        int column =
                index % columns;

        return new Rect(
                actionConfigPanel.x()
                        + PANEL_PADDING
                        + column
                        * (
                        ACTION_TYPE_BUTTON_WIDTH
                                + ACTION_TYPE_BUTTON_GAP
                ),
                actionConfigPanel.y()
                        + ACTION_TYPE_TOP_OFFSET
                        + row
                        * (
                        ACTION_TYPE_BUTTON_HEIGHT
                                + ACTION_TYPE_BUTTON_GAP
                ),
                ACTION_TYPE_BUTTON_WIDTH,
                ACTION_TYPE_BUTTON_HEIGHT
        );
    }

    public Rect deleteButton() {
        return new Rect(
                POPUP_WIDTH
                        - DELETE_BUTTON_RIGHT_GAP,
                footerY,
                DELETE_BUTTON_WIDTH,
                DELETE_BUTTON_HEIGHT
        );
    }

    public Rect cancelButton() {
        return new Rect(
                POPUP_WIDTH
                        - CANCEL_BUTTON_RIGHT_GAP,
                footerY,
                CANCEL_BUTTON_WIDTH,
                CANCEL_BUTTON_HEIGHT
        );
    }

    public Rect applyButton() {
        return new Rect(
                POPUP_WIDTH
                        - APPLY_BUTTON_RIGHT_GAP,
                footerY,
                APPLY_BUTTON_WIDTH,
                APPLY_BUTTON_HEIGHT
        );
    }

    public Rect commandPrefix() {
        if (!commandAction) {
            return null;
        }

        return new Rect(
                dataFrame.x(),
                dataFrame.y(),
                COMMAND_PREFIX_WIDTH,
                dataFrame.height()
        );
    }

    public Rect toScreen(
            Rect designRect
    ) {
        if (designRect == null) {
            return null;
        }

        return new Rect(
                popupX
                        + (int) Math.round(
                        designRect.x() * scale
                ),
                popupY
                        + (int) Math.round(
                        designRect.y() * scale
                ),
                Math.max(
                        1,
                        (int) Math.round(
                                designRect.width() * scale
                        )
                ),
                Math.max(
                        1,
                        (int) Math.round(
                                designRect.height() * scale
                        )
                )
        );
    }

    public int toScreenX(
            int designX
    ) {
        return popupX
                + (int) Math.round(
                designX * scale
        );
    }

    public int toScreenY(
            int designY
    ) {
        return popupY
                + (int) Math.round(
                designY * scale
        );
    }

    public int toScreenSize(
            int designSize
    ) {
        return Math.max(
                1,
                (int) Math.round(
                        designSize * scale
                )
        );
    }

    public double getScale() {
        return scale;
    }

    public int getPopupX() {
        return popupX;
    }

    public int getPopupY() {
        return popupY;
    }

    public Rect getInfoPanel() {
        return infoPanel;
    }

    public Rect getLeftPanel() {
        return leftPanel;
    }

    public Rect getRightPanel() {
        return rightPanel;
    }

    public Rect getActionConfigPanel() {
        return actionConfigPanel;
    }

    public Rect getActionEditorPanel() {
        return actionEditorPanel;
    }

    public int getBodyY() {
        return bodyY;
    }

    public int getFooterY() {
        return footerY;
    }

    public int getBodyHeight() {
        return bodyHeight;
    }

    public int getActionEditorTitleY() {
        return actionEditorTitleY;
    }

    public int getPopupTitleX() {
        return popupTitleX;
    }

    public int getPopupTitleY() {
        return popupTitleY;
    }

    public int getInfoContentX() {
        return infoContentX;
    }

    public int getInfoContentWidth() {
        return infoContentWidth;
    }

    public int getInfoColumnWidth() {
        return infoColumnWidth;
    }

    public int getInfoCenterY() {
        return infoCenterY;
    }

    public Rect infoColumn(
            int index
    ) {
        int columnWidth =
                index == 2
                        ? infoContentWidth
                        - infoColumnWidth * 2
                        : infoColumnWidth;

        return new Rect(
                infoContentX
                        + infoColumnWidth * index,
                infoPanel.y(),
                columnWidth,
                infoPanel.height()
        );
    }

    public int getActionListHeaderX() {
        return actionListHeaderX;
    }

    public int getActionListHeaderY() {
        return actionListHeaderY;
    }

    public int getActionConfigTitleX() {
        return actionConfigTitleX;
    }

    public int getActionConfigTitleY() {
        return actionConfigTitleY;
    }

    public int getActionTypeLabelX() {
        return actionTypeLabelX;
    }

    public int getActionTypeLabelY() {
        return actionTypeLabelY;
    }

    public int getActionEditorHeaderX() {
        return actionEditorHeaderX;
    }

    public int getActionEditorHeaderY() {
        return actionEditorHeaderY;
    }

    public int getSelectedActionLabelX() {
        return selectedActionLabelX;
    }

    public int getSelectedActionLabelY() {
        return selectedActionLabelY;
    }

    public int getFieldLabelX() {
        return fieldLabelX;
    }

    public int getFieldLabelY() {
        return fieldLabelY;
    }

    public Rect getDataFrame() {
        return dataFrame;
    }

    public Rect getDataInput() {
        return dataInput;
    }

    public Rect getCountFrame() {
        return countFrame;
    }

    public Rect getCountInput() {
        return countInput;
    }

    public boolean isCommandAction() {
        return commandAction;
    }

    public boolean isGiveItem() {
        return giveItem;
    }

    public record Rect(
            int x,
            int y,
            int width,
            int height
    ) {

        public boolean contains(
                double mouseX,
                double mouseY
        ) {
            return mouseX >= x
                    && mouseX <= x + width
                    && mouseY >= y
                    && mouseY <= y + height;
        }
    }
}
