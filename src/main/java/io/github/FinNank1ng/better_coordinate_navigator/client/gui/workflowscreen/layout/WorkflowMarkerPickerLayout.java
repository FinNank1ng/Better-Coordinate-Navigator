package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.layout;

/**
 * 工作流目标标点选择器布局
 */
public final class WorkflowMarkerPickerLayout {

    public static final int POPUP_WIDTH = 620;
    public static final int POPUP_HEIGHT = 520;

    private static final int MIN_MARGIN = 24;

    private static final int TITLE_X = 20;
    private static final int TITLE_Y = 20;

    private static final int SEARCH_X = 20;
    private static final int SEARCH_Y = 54;
    private static final int SEARCH_HEIGHT = 26;

    private static final int LIST_TOP = 92;
    private static final int FOOTER_HEIGHT = 46;

    public static final int CARD_HEIGHT = 58;
    public static final int CARD_GAP = 8;

    private final double scale;
    private final int popupX;
    private final int popupY;

    private final int listTop;
    private final int listBottom;

    private WorkflowMarkerPickerLayout(
            double scale,
            int popupX,
            int popupY,
            int listTop,
            int listBottom
    ) {
        this.scale = scale;
        this.popupX = popupX;
        this.popupY = popupY;
        this.listTop = listTop;
        this.listBottom = listBottom;
    }

    public static WorkflowMarkerPickerLayout calculate(
            int screenWidth,
            int screenHeight
    ) {

        double scaleX =
                (screenWidth - MIN_MARGIN * 2.0D)
                        / POPUP_WIDTH;

        double scaleY =
                (screenHeight - MIN_MARGIN * 2.0D)
                        / POPUP_HEIGHT;

        double scale =
                Math.max(
                        0.55D,
                        Math.min(
                                1.0D,
                                Math.min(
                                        scaleX,
                                        scaleY
                                )
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

        return new WorkflowMarkerPickerLayout(
                scale,
                popupX,
                popupY,
                LIST_TOP,
                POPUP_HEIGHT - FOOTER_HEIGHT
        );
    }

    public int toScreenX(int designX) {
        return popupX
                + (int) Math.round(
                designX * scale
        );
    }

    public int toScreenY(int designY) {
        return popupY
                + (int) Math.round(
                designY * scale
        );
    }

    public int toScreenSize(int size) {
        return Math.max(
                1,
                (int) Math.round(
                        size * scale
                )
        );
    }

    public int getPopupX() {
        return popupX;
    }

    public int getPopupY() {
        return popupY;
    }

    public int getScaledWidth() {
        return toScreenSize(POPUP_WIDTH);
    }

    public int getScaledHeight() {
        return toScreenSize(POPUP_HEIGHT);
    }

    public double getScale() {
        return scale;
    }

    public int getListTop() {
        return listTop;
    }

    public int getListBottom() {
        return listBottom;
    }

    public int getCardScreenY(
            double scroll,
            int index
    ) {

        int designY =
                listTop
                        + index
                        * (CARD_HEIGHT + CARD_GAP)
                        - (int) scroll;

        return toScreenY(designY);
    }
}