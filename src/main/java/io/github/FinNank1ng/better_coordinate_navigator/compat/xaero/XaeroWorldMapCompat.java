package io.github.FinNank1ng.better_coordinate_navigator.compat.xaero;

import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;

public final class XaeroWorldMapCompat {

    private XaeroWorldMapCompat() {
    }

    /**
     * 同步 BCN Marker 到 Xaero World Map
     */
    public static void syncMarker(
            QuestMarker marker
    ) {

        if (marker == null) {
            return;
        }

        if (!XaeroCompat.isWorldMapLoaded()) {
            return;
        }

        /* TIPs:
         * World Map 当前使用 Xaero 的 WaypointWorld 数据
         *
         * 因此实际 waypoint 同步由 XaeroMinimapCompat 完成
         *
         * 保留这个入口是为了以后 World Map 专属功能扩展
         */
    }

    /**
     * 删除 BCN Marker。
     */
    public static void removeMarker(
            String markerName
    ) {

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
     * 清理全部 BCN World Map Marker。
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