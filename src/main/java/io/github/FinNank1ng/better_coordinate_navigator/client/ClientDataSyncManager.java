
package io.github.FinNank1ng.better_coordinate_navigator.client;

import io.github.FinNank1ng.better_coordinate_navigator.compat.xaero.XaeroClientSync;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ClientDataSyncManager {

    /**
     * 上一次已经同步到 Xaero 的 Marker UUID
     */
    private static final Set<UUID> LAST_SYNCED_MARKERS =
            new HashSet<>();

    private ClientDataSyncManager() {
    }

    /**
     * 客户端数据发生变化后调用
     */
    public static void refreshAll() {

        Set<UUID> currentMarkers = new HashSet<>();

        /*
         * 获取当前正在追踪的 Marker
         */
        for (QuestMarker marker : ClientQuestCache.getMarkers()) {

            if (marker == null) {
                continue;
            }

            if (!marker.active) {
                continue;
            }

            UUID markerId = marker.getId();

            if (!ClientQuestCache.isTracked(markerId)) {
                continue;
            }

            currentMarkers.add(markerId);
        }

        /*
         * 删除已经不存在或取消追踪的 Marker
         */
        for (UUID oldMarkerId : LAST_SYNCED_MARKERS) {

            if (!currentMarkers.contains(oldMarkerId)) {

                XaeroClientSync.removeMarker(oldMarkerId);
            }
        }

        /*
         * 同步当前正在追踪的 Marker
         */
        XaeroClientSync.syncAll();

        /*
         * 保存本次同步状态
         */
        LAST_SYNCED_MARKERS.clear();

        LAST_SYNCED_MARKERS.addAll(currentMarkers);
    }

    /**
     * 客户端离开世界或数据清空时调用
     */
    public static void clearAll() {

        XaeroClientSync.clearAll();

        LAST_SYNCED_MARKERS.clear();
    }
}
