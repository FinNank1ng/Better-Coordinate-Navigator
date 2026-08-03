package io.github.FinNank1ng.better_coordinate_navigator.data;


import static com.mojang.text2speech.Narrator.LOGGER;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public class QuestManager extends SavedData {


    /**
     * 玩家独立追踪数据
     */
    private final Map<UUID, Set<String>> playerTrackers = new HashMap<>();

    private final List<QuestMarker> markers = new ArrayList<>();

    public QuestManager() {
        super();
    }


    /**
     * 从 NBT 加载数据
     */
    public static QuestManager load(CompoundTag tag) {

        QuestManager manager = new QuestManager();

        /*
         * 加载标记点
         */
        ListTag listTag =
                tag.getList(
                        "markers",
                        10
                );


        for (int i = 0; i < listTag.size(); i++) {

            CompoundTag markerTag = listTag.getCompound(i);


            manager.markers.add(QuestMarker.load(markerTag));

        }

        /*
         * 加载玩家追踪数据
         */
        ListTag trackerList =
                tag.getList(
                        "playerTrackers",
                        10
                );


        for (int i = 0; i < trackerList.size(); i++) {


            CompoundTag playerTag = trackerList.getCompound(i);


            UUID uuid =
                    playerTag.getUUID(
                            "uuid"
                    );


            Set<String> markers = new HashSet<>();


            ListTag names =
                    playerTag.getList(
                            "markers",
                            8
                    );

            for (int j = 0; j < names.size(); j++) {

                markers.add(names.getString(j));

            }

            manager.playerTrackers.put(
                    uuid,
                    markers
            );

        }

        return manager;

    }

    /**
     * 保存数据到 NBT
     */
    @Override
    public CompoundTag save(CompoundTag tag) {

        /*
         * 保存标记
         */
        ListTag listTag = new ListTag();

        for (QuestMarker marker : markers) {

            listTag.add(marker.save(new CompoundTag()));

        }

        tag.put("markers", listTag);

        /*
         * 保存玩家追踪
         */
        ListTag trackerList = new ListTag();

        for (var entry : playerTrackers.entrySet()) {


            CompoundTag playerTag = new CompoundTag();

            playerTag.putUUID(
                    "uuid",
                    entry.getKey()
            );

            ListTag markerList = new ListTag();

            for (String name : entry.getValue()) {


                markerList.add(StringTag.valueOf(name));

            }


            playerTag.put(
                    "markers",
                    markerList
            );

            trackerList.add(playerTag);

        }


        tag.put(
                "playerTrackers",
                trackerList
        );

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
     * 添加标记
     */
    public void addMarker(
            QuestMarker marker
    ) {

        LOGGER.debug(
                "[BCN] Added Marker "
                        + marker.name
        );


        markers.add(marker);


        setDirty();

    }

    /**
     * 检查是否存在同名标记
     */
    public boolean existsMarker(
            String name
    ) {

        return markers.stream()
                .anyMatch(marker ->
                        marker.name.equalsIgnoreCase(name)
                );

    }

    /**
     * 获取标记
     */
    public QuestMarker getMarker(
            String name
    ) {

        return markers.stream()
                .filter(marker ->
                        marker.name.equalsIgnoreCase(name)
                )
                .findFirst()
                .orElse(null);

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


        if(existsMarker(newName)){
            return false;
        }

        marker.name = newName;


        for(Set<String> list : playerTrackers.values()){

            if(list.remove(oldName)){

                list.add(newName);

            }

        }

        setDirty();

        return true;
    }

    /**
     * 移除标记
     */
    public boolean removeMarker(
            String name
    ){

        boolean removed =
                markers.removeIf(
                        marker ->
                                marker.name.equalsIgnoreCase(name)
                );


        if(!removed){
            return false;
        }


        for(Set<String> set : playerTrackers.values()){

            set.remove(name);

        }


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
     * 获取玩家追踪的标记名称
     */
    public List<String> getPlayerTrackedMarkers(
            UUID uuid
    ) {

        Set<String> trackers =
                playerTrackers.get(uuid);

        if (trackers == null) {

            return new ArrayList<>();

        }

        return new ArrayList<>(trackers);
    }

    /**
     * 玩家添加追踪
     */
    public boolean trackPlayerMarker(
            UUID uuid,
            String name
    ) {

        QuestMarker marker = getMarker(name);

        if (marker == null) {

            return false;

        }

        playerTrackers
                .computeIfAbsent(
                        uuid,
                        k -> new HashSet<>()
                )
                .add(name);

        setDirty();

        return true;

    }

    /**
     * 玩家取消追踪
     */
    public boolean untrackPlayerMarker(
            UUID uuid,
            String name
    ) {

        Set<String> list = playerTrackers.get(uuid);

        if (list == null) {

            return false;

        }

        boolean result = list.remove(name);

        if (result) {

            setDirty();

        }

        return result;

    }

    /**
     * 清空玩家追踪
     */
    public void clearPlayerTrackers(
            UUID uuid
    ) {

        if (playerTrackers.remove(uuid) != null) {

            setDirty();

        }
    }

    /**
     * 判断玩家是否追踪
     */
    public boolean isPlayerTracking (
            UUID uuid,
            String name
    ){

        return getPlayerTrackedMarkers(uuid)
                .contains(name);

    }
}