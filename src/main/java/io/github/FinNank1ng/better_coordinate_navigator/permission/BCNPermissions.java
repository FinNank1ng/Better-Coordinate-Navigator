package io.github.FinNank1ng.better_coordinate_navigator.permission;

import net.minecraft.server.level.ServerPlayer;

public final class BCNPermissions {

    /*
     * BCN 管理功能所需的最低 OP 等级
     */
    private static final int ADMIN_LEVEL = 2;

    private BCNPermissions() {
    }

    /*
     * 判断玩家是否拥有 BCN 管理权限
     */
    public static boolean hasAdminPermission(
            ServerPlayer player
    ) {
        return player != null
                && player.hasPermissions(
                ADMIN_LEVEL
        );
    }
}