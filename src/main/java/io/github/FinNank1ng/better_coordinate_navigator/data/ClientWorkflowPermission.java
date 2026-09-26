package io.github.FinNank1ng.better_coordinate_navigator.data;

/*
 * 客户端工作流管理权限缓存
 */
public final class ClientWorkflowPermission {

    /*
     * 当前玩家是否拥有工作流管理权限
     */
    private static boolean canManage;

    private ClientWorkflowPermission() {
    }

    /*
     * 设置当前玩家的工作流管理权限
     */
    public static void setCanManage(
            boolean value
    ) {
        canManage = value;
    }

    /*
     * 判断当前玩家是否可以管理工作流
     */
    public static boolean canManage() {
        return canManage;
    }

    /*
     * 清空客户端权限状态
     */
    public static void clear() {
        canManage = false;
    }
}