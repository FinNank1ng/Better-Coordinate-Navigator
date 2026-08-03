package io.github.FinNank1ng.better_coordinate_navigator.data;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientQuestCache {

    // 使用线程安全的列表，防止渲染线程和主线程冲突
    private static final List<QuestMarker> MARKERS = new CopyOnWriteArrayList<>();

    /**
     * 当前客户端玩家追踪的标记名称
     */
    private static final List<String> TRACKED_MARKERS = new CopyOnWriteArrayList<>();


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

    public static List<QuestMarker> getMarkers() {

        return MARKERS;

    }

    /**
     * 设置当前玩家追踪列表
     */
    public static void setTrackedMarkers(

            Collection<String> markers

    ){

        TRACKED_MARKERS.clear();


        if(markers != null){

            TRACKED_MARKERS.addAll(markers);

        }

    }

    /**
     * 判断当前玩家是否追踪该标记
     */
    public static boolean isTracked(
            String name
    ){

        if(name == null){

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
     * 获取当前玩家追踪列表
     */
    public static List<String> getTrackedMarkers(){

        return TRACKED_MARKERS;

    }

    /**
     * 清空客户端缓存
     */
    public static void clear(){

        MARKERS.clear();

        TRACKED_MARKERS.clear();

    }

}