package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.layout;

import net.minecraft.client.gui.Font;

/*
 * 工作流界面框架布局
 */
public final class WorkflowFrameLayout {

    public static final int HEADER_HEIGHT = 42;
    public static final int FOOTER_HEIGHT = 34;

    public static final int SIDEBAR_EXPANDED_WIDTH = 158;
    public static final int SIDEBAR_COLLAPSED_WIDTH = 42;

    /*
     * Workflow 列表单项槽位高度
     */
    public static final int WORKFLOW_ITEM_HEIGHT = 46;

    public static final int HEADER_BUTTON_WIDTH = 54;
    public static final int HEADER_BUTTON_HEIGHT = 20;

    public static final int HEADER_BCN_X = 18;
    public static final int HEADER_SLASH_X = 48;
    public static final int HEADER_SECTION_X = 64;
    public static final int HEADER_SEPARATOR_X = 116;
    public static final int HEADER_WORKFLOW_X = 134;

    public static final int HEADER_TEST_RIGHT_GAP = 101;
    public static final int HEADER_SAVE_RIGHT_GAP = 28;

    public static final int TOOLBAR_LEFT_OFFSET = 12;
    public static final int TOOLBAR_TOP_OFFSET = 12;
    public static final int TOOLBAR_ADD_WIDTH = 44;
    public static final int TOOLBAR_BUTTON_HEIGHT = 20;
    public static final int TOOLBAR_SECOND_X_OFFSET = 70;
    public static final int TOOLBAR_HITBOX_WIDTH = 52;
    public static final int TOOLBAR_HITBOX_HEIGHT = 22;

    /*
     * Sidebar 标题区
     */
    public static final int SIDEBAR_HEADER_X = 48;
    public static final int SIDEBAR_HEADER_Y_OFFSET = 10;
    public static final int SIDEBAR_COUNT_X = 100;

    /*
     * Sidebar 搜索框
     */
    public static final int SIDEBAR_SEARCH_X = 8;
    public static final int SIDEBAR_SEARCH_Y_OFFSET = 34;
    public static final int SIDEBAR_SEARCH_WIDTH = 142;
    public static final int SIDEBAR_SEARCH_HEIGHT = 22;
    public static final int SIDEBAR_SEARCH_TEXT_OFFSET = 6;

    public static final int SIDEBAR_COLLAPSE_X = 8;
    public static final int SIDEBAR_COLLAPSE_Y_OFFSET = 6;
    public static final int SIDEBAR_COLLAPSE_HEIGHT = 24;

    public static final int SIDEBAR_LIST_TOP_OFFSET = 64;
    public static final int SIDEBAR_LIST_BOTTOM_OFFSET = 48;

    public static final int SIDEBAR_ITEM_HORIZONTAL_PADDING = 8;

    public static final int SIDEBAR_NEW_BUTTON_BOTTOM_OFFSET = 36;
    public static final int SIDEBAR_NEW_BUTTON_HEIGHT = 22;

    /*
     * Workflow 上下文菜单
     */
    public static final int WORKFLOW_MENU_WIDTH = 128;
    public static final int WORKFLOW_MENU_HEIGHT = 72;
    public static final int WORKFLOW_MENU_HORIZONTAL_OFFSET = 8;
    public static final int WORKFLOW_MENU_MIN_TOP_OFFSET = 8;
    public static final int WORKFLOW_MENU_BOTTOM_OFFSET = 8;

    private WorkflowFrameLayout() {
    }

    public static int headerTextY(
            Font font
    ) {

        return (
                HEADER_HEIGHT
                        - font.lineHeight
        ) / 2;
    }

    public static int headerButtonY() {

        return (
                HEADER_HEIGHT
                        - HEADER_BUTTON_HEIGHT
        ) / 2;
    }

    public static int sidebarWidth(
            boolean collapsed
    ) {

        return collapsed
                ? SIDEBAR_COLLAPSED_WIDTH
                : SIDEBAR_EXPANDED_WIDTH;
    }

    public static int sidebarHeaderY() {

        return HEADER_HEIGHT
                + SIDEBAR_HEADER_Y_OFFSET;
    }

    public static int sidebarSearchY() {

        return HEADER_HEIGHT
                + SIDEBAR_SEARCH_Y_OFFSET;
    }

    public static int sidebarCollapseY() {

        return HEADER_HEIGHT
                + SIDEBAR_COLLAPSE_Y_OFFSET;
    }

    public static int workflowListTop() {

        return HEADER_HEIGHT
                + SIDEBAR_LIST_TOP_OFFSET;
    }

    public static int workflowListBottom(
            int screenHeight
    ) {

        return screenHeight
                - FOOTER_HEIGHT
                - SIDEBAR_LIST_BOTTOM_OFFSET;
    }

    public static int sidebarNewButtonY(
            int screenHeight
    ) {

        return screenHeight
                - FOOTER_HEIGHT
                - SIDEBAR_NEW_BUTTON_BOTTOM_OFFSET;
    }

    public static int toolbarY() {

        return HEADER_HEIGHT
                + TOOLBAR_TOP_OFFSET;
    }

    public static int toolbarX(
            int sidebarWidth
    ) {

        return sidebarWidth
                + TOOLBAR_LEFT_OFFSET;
    }

    public static int headerTestX(
            int screenWidth
    ) {

        return screenWidth
                - HEADER_TEST_RIGHT_GAP
                - HEADER_BUTTON_WIDTH;
    }

    public static int headerSaveX(
            int screenWidth
    ) {

        return screenWidth
                - HEADER_SAVE_RIGHT_GAP
                - HEADER_BUTTON_WIDTH;
    }

    public static int workflowMenuX() {

        return SIDEBAR_EXPANDED_WIDTH
                - WORKFLOW_MENU_WIDTH
                - WORKFLOW_MENU_HORIZONTAL_OFFSET;
    }

    public static int workflowMenuY(
            int itemY,
            int screenHeight
    ) {

        int minY =
                HEADER_HEIGHT
                        + WORKFLOW_MENU_MIN_TOP_OFFSET;

        int maxY =
                screenHeight
                        - FOOTER_HEIGHT
                        - WORKFLOW_MENU_HEIGHT
                        - WORKFLOW_MENU_BOTTOM_OFFSET;

        return Math.max(
                minY,
                Math.min(
                        itemY,
                        maxY
                )
        );
    }
}
