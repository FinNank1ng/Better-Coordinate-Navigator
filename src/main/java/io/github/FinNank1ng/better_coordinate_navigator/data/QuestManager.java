package io.github.FinNank1ng.better_coordinate_navigator.data;
import static com.mojang.text2speech.Narrator.LOGGER;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class QuestManager extends SavedData {

    private final List<QuestMarker> markers = new ArrayList<>();

    public QuestManager() {
        super();
    }

    /**
     * 从 NBT 加载数据
     */
    public static QuestManager load(CompoundTag tag) {
        QuestManager manager = new QuestManager();
        ListTag listTag = tag.getList("markers", 10); // 10 是 CompoundTag 的类型 ID

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag markerTag = listTag.getCompound(i);
            manager.markers.add(QuestMarker.load(markerTag));
        }

        return manager;
    }

    /**
     * 保存数据到 NBT
     */
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag listTag = new ListTag();

        for (QuestMarker marker : markers) {
            listTag.add(marker.save(new CompoundTag()));
        }

        tag.put("markers", listTag);
        return tag;
    }

    /**
     * 获取世界数据实例
     */
    public static QuestManager get(
            ServerLevel level
    ) {

        DimensionDataStorage storage =
                level.getDataStorage();


        return storage.computeIfAbsent(
                QuestManager::load,
                QuestManager::new,
                "quest_manager"
        );
    }

    /**
     * 添加标记并标记数据为脏（需要保存）
     */
    public void addMarker(QuestMarker marker) {

        LOGGER.debug(
                "[BCN] Added Marker "
                        + marker.name
        );

        markers.add(marker);

        LOGGER.debug(
                "[BCN] Current Marker Count = "
                        + markers.size()
        );

        setDirty();
    }
    /**
     * 检查是否存在同名标记
     */
    public boolean existsMarker(String name) {

        return markers.stream()
                .anyMatch(marker ->
                        marker.name.equalsIgnoreCase(name)
                );
    }
    /**
     * 根据名称获取标记
     */
    public QuestMarker getMarker(String name) {

        return markers.stream()
                .filter(marker ->
                        marker.name.equalsIgnoreCase(name)
                )
                .findFirst()
                .orElse(null);
    }
    /**
     * 根据名称删除标记
     */
    public boolean removeMarker(String name) {

        boolean removed = markers.removeIf(
                marker ->
                        marker.name.equalsIgnoreCase(name)
        );

        if (removed) {
            setDirty();
        }

        return removed;
    }
    /**
     * 重命名名称
     */
    public boolean renameMarker(
            String oldName,
            String newName
    ) {

        QuestMarker marker =
                getMarker(oldName);

        if (marker == null) {
            return false;
        }

        marker.name = newName;

        setDirty();

        return true;
    }

    /**
     * 获取所有标记
     */
    public List<QuestMarker> getMarkers() {
        return markers;
    }

    /**
     * 取消所有标点追踪
     */
    public void clearTrackedMarkers() {

        for (QuestMarker marker : markers) {
            marker.tracked = false;
        }

        setDirty();
    }
    /**
     * 取消单个标点追踪
     */
    public boolean untrackMarker(String name){

        QuestMarker marker = getMarker(name);

        if(marker == null){
            return false;
        }

        marker.tracked = false;

        setDirty();

        return true;
    }

    /**
     * 添加或设置标点追踪
     */
    public boolean trackMarker(String name) {

        QuestMarker marker = getMarker(name);

        if(marker == null){
            return false;
        }


        if(!marker.tracked){

            marker.tracked = true;

            setDirty();
        }

        return true;
    }
    // 未用
    public List<QuestMarker> getTrackedMarkers(){

        return markers.stream()
                .filter(marker -> marker.tracked)
                .toList();
    }
    /**
     * 客户端缓存层
     */
    public static class Client {
        private static List<QuestMarker> CLIENT_MARKERS = new ArrayList<>();

        public static void setMarkers(List<QuestMarker> markers) {
            CLIENT_MARKERS = markers;
        }

        public static List<QuestMarker> getMarkers() {
            return CLIENT_MARKERS;
        }

        public static void clear() {
            CLIENT_MARKERS.clear();
        }
    }
}