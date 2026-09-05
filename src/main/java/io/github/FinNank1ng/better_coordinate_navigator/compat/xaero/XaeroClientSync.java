package io.github.FinNank1ng.better_coordinate_navigator.compat.xaero;

import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;

public final class XaeroClientSync {

    private XaeroClientSync() {
    }

    /**
     * 将 BCN 当前客户端数据同步到 Xaero
     */
    public static void syncAll() {

        for (QuestMarker marker :
                ClientQuestCache.getMarkers()) {

            if (marker == null) {
                continue;
            }

            if (!marker.active) {
                continue;
            }

            /*
             * ClientQuestCache 的实际客户端追踪状态保存在 TRACKED_MARKERS
             */
            if (!ClientQuestCache.isTracked(
                    marker.name
            )) {
                continue;
            }

            /*
             * 实际创建 / 更新 Xaero waypoint
             */
            XaeroMinimapCompat.syncMarker(
                    marker
            );

            /*
             * World Map 当前复用同一套 Xaero WaypointWorld
             */
            XaeroWorldMapCompat.syncMarker(
                    marker
            );
        }
    }

    /**
     * 清理 BCN 在 Xaero 中的全部内容
     */
    public static void clearAll() {

        XaeroMinimapCompat.clearAll();

        XaeroWorldMapCompat.clearAll();
    }

    /**
     * 同步一个 Marker
     */
    public static void syncMarker(
            QuestMarker marker
    ) {

        if (marker == null) {
            return;
        }

        if (!marker.active) {
            return;
        }

        if (!ClientQuestCache.isTracked(
                marker.name
        )) {
            return;
        }

        XaeroMinimapCompat.syncMarker(
                marker
        );

        XaeroWorldMapCompat.syncMarker(
                marker
        );
    }

    /**
     * 从 Xaero 中移除一个 Marker
     */
    public static void removeMarker(
            String markerName
    ) {

        if (markerName == null) {
            return;
        }

        XaeroMinimapCompat.removeMarker(
                markerName
        );

        XaeroWorldMapCompat.removeMarker(
                markerName
        );
    }
}