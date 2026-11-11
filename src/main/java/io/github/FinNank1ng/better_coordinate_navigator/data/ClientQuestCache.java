
package io.github.FinNank1ng.better_coordinate_navigator.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientQuestCache {

    /**
     * 世界中的全部标点
     */
    private static final List<QuestMarker> MARKERS =
            new CopyOnWriteArrayList<>();

    /**
     * 当前客户端玩家追踪的标点 UUID
     */
    private static final Set<UUID> TRACKED_MARKER_IDS =
            new HashSet<>();

    private ClientQuestCache() {
    }

    /**
     * 设置客户端标点缓存
     */
    public static void set(List<QuestMarker> markers) {

        MARKERS.clear();

        if (markers != null) {
            MARKERS.addAll(markers);
        }
    }

    /**
     * 获取全部客户端标点
     */
    public static List<QuestMarker> getMarkers() {
        return MARKERS;
    }

    /**
     * 根据 UUID 获取客户端标点
     */
    public static QuestMarker getMarker(UUID id) {

        if (id == null) {
            return null;
        }

        for (QuestMarker marker : MARKERS) {

            if (marker != null && marker.getId().equals(id)) {
                return marker;
            }
        }

        return null;
    }

    /**
     * 根据名称获取唯一的客户端标点
     */
    public static QuestMarker getMarker(String name) {

        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        QuestMarker result = null;

        for (QuestMarker marker : MARKERS) {

            if (marker == null || marker.name == null) {
                continue;
            }

            if (!marker.name.equalsIgnoreCase(name.trim())) {
                continue;
            }

            if (result != null) {
                return null;
            }

            result = marker;
        }

        return result;
    }

    /**
     * 查找全部同名客户端标点
     */
    public static List<QuestMarker> findMarkers(String name) {

        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }

        String target = name.trim();

        List<QuestMarker> result = new ArrayList<>();

        for (QuestMarker marker : MARKERS) {

            if (marker == null || marker.name == null) {
                continue;
            }

            if (marker.name.equalsIgnoreCase(target)) {
                result.add(marker);
            }
        }

        return result;
    }

    /**
     * 设置当前玩家追踪的标点 UUID
     */
    public static void setTrackedMarkerIds(Collection<UUID> markerIds) {

        TRACKED_MARKER_IDS.clear();

        if (markerIds == null) {
            return;
        }

        for (UUID markerId : markerIds) {

            if (markerId != null) {
                TRACKED_MARKER_IDS.add(markerId);
            }
        }
    }

    /**
     * 判断当前玩家是否追踪指定 UUID 的标点
     */
    public static boolean isTracked(UUID markerId) {

        if (markerId == null) {
            return false;
        }

        return TRACKED_MARKER_IDS.contains(markerId);
    }

    /**
     * 获取当前玩家追踪的全部标点 UUID
     */
    public static Set<UUID> getTrackedMarkerIds() {
        return new HashSet<>(TRACKED_MARKER_IDS);
    }

    /**
     * 获取当前玩家追踪列表中的标点名称
     * 仅用于兼容旧代码
     */
    @Deprecated
    public static List<String> getTrackedMarkers() {

        List<String> result = new ArrayList<>();

        for (UUID markerId : TRACKED_MARKER_IDS) {

            QuestMarker marker = getMarker(markerId);

            if (marker != null) {
                result.add(marker.name);
            }
        }

        return result;
    }

    /**
     * 通过旧版名称接口设置追踪列表
     * 仅接受能够唯一匹配的标点
     */
    @Deprecated
    public static void setTrackedMarkers(Collection<String> markers) {

        TRACKED_MARKER_IDS.clear();

        if (markers == null) {
            return;
        }

        for (String name : markers) {

            QuestMarker marker = getMarker(name);

            if (marker != null) {
                TRACKED_MARKER_IDS.add(marker.getId());
            }
        }
    }

    /**
     * 通过名称判断是否追踪标点
     * 仅用于兼容旧代码
     */
    @Deprecated
    public static boolean isTracked(String name) {

        QuestMarker marker = getMarker(name);

        if (marker == null) {
            return false;
        }

        return isTracked(marker.getId());
    }

    /**
     * 获取当前玩家追踪的 QuestMarker
     */
    public static List<QuestMarker> getTrackedMarkerObjects() {

        List<QuestMarker> result = new ArrayList<>();

        for (UUID markerId : TRACKED_MARKER_IDS) {

            QuestMarker marker = getMarker(markerId);

            if (marker != null) {
                result.add(marker);
            }
        }

        return result;
    }

    /**
     * 清空客户端缓存
     */
    public static void clear() {

        MARKERS.clear();

        TRACKED_MARKER_IDS.clear();
    }
}
