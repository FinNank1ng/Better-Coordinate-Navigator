package io.github.FinNank1ng.better_coordinate_navigator.data;

import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientQuestCache {

    // 使用线程安全的列表，防止渲染线程和主线程冲突
    private static final List<QuestMarker> MARKERS = new CopyOnWriteArrayList<>();

    public static void set(List<QuestMarker> markers) {
        // 先清空旧数据
        MARKERS.clear();
        // 添加新数据
        if (markers != null) {
            MARKERS.addAll(markers);
        }
    }

    public static List<QuestMarker> getMarkers() {
        return MARKERS;
    }
}