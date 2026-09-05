package io.github.FinNank1ng.better_coordinate_navigator.client;

import io.github.FinNank1ng.better_coordinate_navigator.compat.xaero.XaeroClientSync;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;

import java.util.HashSet;
import java.util.Set;

public final class ClientDataSyncManager {

    /**
     * 上一次已经同步到 Xaero 的 Marker
     */
    private static final Set<String> LAST_SYNCED_MARKERS =
            new HashSet<>();

    private ClientDataSyncManager() {
    }

    /**
     * 客户端数据发生变化后调用
     */
    public static void refreshAll() {

        Set<String> currentMarkers =
                new HashSet<>();


        // 获取当前正在追踪的 Marker
        ClientQuestCache.getMarkers()
                .forEach(marker -> {

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

                    currentMarkers.add(
                            marker.name
                    );
                });


        // 删除已经不存在 / 取消追踪的
        for (String oldMarker :
                LAST_SYNCED_MARKERS) {

            if (!currentMarkers.contains(
                    oldMarker
            )) {

                XaeroClientSync.removeMarker(
                        oldMarker
                );
            }
        }


        // 同步当前正在追踪的
        XaeroClientSync.syncAll();

        // 保存本次状态
        LAST_SYNCED_MARKERS.clear();

        LAST_SYNCED_MARKERS.addAll(
                currentMarkers
        );
    }

    /**
     * 客户端离开世界或数据清空时调用
     */
    public static void clearAll() {

        XaeroClientSync.clearAll();

        LAST_SYNCED_MARKERS.clear();
    }
}