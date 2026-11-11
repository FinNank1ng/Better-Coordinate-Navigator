
package io.github.FinNank1ng.better_coordinate_navigator.compat.xaero;

import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;

import java.util.UUID;

public final class XaeroClientSync {

    private XaeroClientSync() {
    }

    /**
     * 将 BCN 当前客户端数据同步到 Xaero
     */
    public static void syncAll() {

        for (QuestMarker marker : ClientQuestCache.getMarkers()) {

            if (marker == null) {
                continue;
            }

            if (!marker.active) {
                continue;
            }

            if (!ClientQuestCache.isTracked(marker.getId())) {
                continue;
            }

            /**
             * 同步 Xaero Minimap
             */
            XaeroMinimapCompat.syncMarker(marker);

            /**
             * World Map 当前复用同一套 Xaero WaypointWorld
             */
            XaeroWorldMapCompat.syncMarker(marker);
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
    public static void syncMarker(QuestMarker marker) {

        if (marker == null) {
            return;
        }

        if (!marker.active) {
            return;
        }

        if (!ClientQuestCache.isTracked(marker.getId())) {
            return;
        }

        XaeroMinimapCompat.syncMarker(marker);

        XaeroWorldMapCompat.syncMarker(marker);
    }

    /**
     * 从 Xaero 中移除一个 Marker
     */
    public static void removeMarker(UUID markerId) {

        if (markerId == null) {
            return;
        }

        XaeroMinimapCompat.removeMarker(markerId);

        XaeroWorldMapCompat.removeMarker(markerId);
    }
}
