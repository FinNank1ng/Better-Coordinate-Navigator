
package io.github.FinNank1ng.better_coordinate_navigator.compat.xaero;

import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;

import java.util.UUID;

public final class XaeroWorldMapCompat {

    private XaeroWorldMapCompat() {
    }

    /**
     * 同步 BCN Marker 到 Xaero World Map
     */
    public static void syncMarker(QuestMarker marker) {

        if (marker == null) {
            return;
        }

        if (!XaeroCompat.isWorldMapLoaded()) {
            return;
        }

        /*
         * World Map 当前使用 Xaero 的 WaypointWorld 数据
         * 实际 waypoint 同步由 XaeroMinimapCompat 完成
         *
         * 保留这个入口供以后扩展 World Map 专属功能
         */
    }

    /**
     * 根据 UUID 删除 BCN Marker
     */
    public static void removeMarker(UUID markerId) {

        if (markerId == null) {
            return;
        }

        if (!XaeroCompat.isWorldMapLoaded()) {
            return;
        }

        /*
         * 当前无需单独操作
         * 由 XaeroMinimapCompat 统一管理 waypoint 数据
         */
    }

    /**
     * 兼容旧版按名称删除接口
     */
    @Deprecated
    public static void removeMarker(String markerName) {

        if (markerName == null) {
            return;
        }

        if (!XaeroCompat.isWorldMapLoaded()) {
            return;
        }

        /*
         * 当前无需单独操作
         */
    }

    /**
     * 清理全部 BCN World Map Marker
     */
    public static void clearAll() {

        if (!XaeroCompat.isWorldMapLoaded()) {
            return;
        }

        /*
         * 当前由 XaeroMinimapCompat.clearAll()
         * 统一清理 waypoint 数据
         */
    }
}
