package io.github.FinNank1ng.better_coordinate_navigator.data;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientQuestCache {

    /**
     * 世界中的全部标点 QuestMarker 自身包含唯一 UUID。
     */
    private static final List<QuestMarker> MARKERS =
            new CopyOnWriteArrayList<>();

    /**
     * 当前客户端玩家追踪的标点名称
     */
    private static final List<String> TRACKED_MARKERS =
            new CopyOnWriteArrayList<>();

    /**
     * 设置客户端标点缓存
     */
    public static void set(
            List<QuestMarker> markers
    ) {

        // 先清空旧数据
        MARKERS.clear();
        // 添加新数据
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
     *
     * @param id 标点 UUID
     * @return 对应标点，不存在时返回 null
     */
    public static QuestMarker getMarker(UUID id) {

        if (id == null) {
            return null;
        }

        for (QuestMarker marker : MARKERS) {

            if (marker.getId().equals(id)) {

                return marker;
            }
        }

        return null;
    }

    /**
     * 根据名称获取客户端标点
     */
    public static QuestMarker getMarker(String name) {

        if (name == null) {
            return null;
        }

        QuestMarker result = null;

        for (QuestMarker marker : MARKERS) {

            if (!marker.name.equalsIgnoreCase(name)) {
                continue;
            }

            if (result == null) {

                result = marker;

                continue;
            }

            return null;
        }

        return result;
    }


    /**
     * 查找全部同名客户端标点
     */
    public static List<QuestMarker> findMarkers(
            String name
    ) {

        if (name == null) {
            return List.of();
        }

        String target =
                name.trim();

        if (target.isEmpty()) {
            return List.of();
        }

        List<QuestMarker> result =
                new java.util.ArrayList<>();

        for (QuestMarker marker : MARKERS) {

            if (marker.name.equalsIgnoreCase(target)) {

                result.add(marker);
            }
        }

        return result;
    }

    /**
     * 设置当前玩家追踪列表
     */
    public static void setTrackedMarkers(
            Collection<String> markers
    ) {

        TRACKED_MARKERS.clear();

        if (markers != null) {

            TRACKED_MARKERS.addAll(markers);
        }
    }

    /**
     * 判断当前玩家是否追踪指定名称的标点
     */
    public static boolean isTracked(
            String name
    ) {

        if (name == null) {
            return false;
        }

        return TRACKED_MARKERS
                .stream()
                .anyMatch(
                        marker ->
                                marker.equalsIgnoreCase(name)
                );
    }

    /**
     * 判断当前玩家是否追踪指定 UUID 的标点
     */
    public static boolean isTracked(
            UUID markerId
    ) {

        if (markerId == null) {
            return false;
        }

        QuestMarker marker =
                getMarker(markerId);

        if (marker == null) {
            return false;
        }

        return isTracked(marker.name);
    }

    /**
     * 获取当前玩家追踪列表
     */
    public static List<String> getTrackedMarkers() {

        return TRACKED_MARKERS;
    }

    /**
     * 获取当前玩家追踪的 QuestMarker
     */
    public static List<QuestMarker> getTrackedMarkerObjects() {

        List<QuestMarker> result =
                new java.util.ArrayList<>();

        for (String name : TRACKED_MARKERS) {

            List<QuestMarker> matches =
                    findMarkers(name);


            if (matches.size() == 1) {

                result.add(matches.get(0));
            }
        }

        return result;
    }

    /**
     * 清空客户端缓存
     */
    public static void clear() {

        MARKERS.clear();

        TRACKED_MARKERS.clear();
    }
}