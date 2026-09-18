package io.github.FinNank1ng.better_coordinate_navigator.data;

import static com.mojang.text2speech.Narrator.LOGGER;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

/**
 * BCN 标点数据管理器
 */
public class QuestManager extends SavedData {

    /**
     * 玩家独立追踪数据
     */
    private final Map<UUID, Set<UUID>> playerTrackers = new HashMap<>();

    /**
     * 世界中的全部标点
     */
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
         * 加载标点
         */
        ListTag listTag = tag.getList(
                "markers",
                10
        );

        for (int i = 0; i < listTag.size(); i++) {

            CompoundTag markerTag = listTag.getCompound(i);

            manager.markers.add(
                    QuestMarker.load(markerTag)
            );
        }

        ListTag trackerList =
                tag.getList(
                        "playerTrackers",
                        10
                );

        for (int i = 0; i < trackerList.size(); i++) {

            CompoundTag playerTag = trackerList.getCompound(i);

            if (!playerTag.hasUUID("uuid")) {
                continue;
            }

            UUID playerUuid = playerTag.getUUID("uuid");

            Set<UUID> trackedMarkers =
                    new HashSet<>();

            ListTag markerList =
                    playerTag.getList(
                            "markers",
                            10
                    );

            /*
             * 新版数据
             */
            for (int j = 0; j < markerList.size(); j++) {

                /*
                 * 根据 TAG 类型判断当前是不是新版数据
                 */
                int elementType = markerList.getElementType();

                if (elementType == 10) {

                    CompoundTag markerTag = markerList.getCompound(j);

                    if (!markerTag.hasUUID("id")) {
                        continue;
                    }

                    UUID markerId = markerTag.getUUID("id");


                    if (manager.getMarker(markerId) != null) {
                        trackedMarkers.add(markerId);
                    }

                } else if (elementType == 8) {

                    /*
                     * 尝试根据名称迁移
                     */
                    String markerName =
                            markerList.getString(j);

                    List<QuestMarker> matches =
                            manager.findMarkers(markerName);

                    /*
                     * 只有唯一匹配时才自动迁移
                     */
                    if (matches.size() == 1) {

                        trackedMarkers.add(
                                matches.get(0).getId()
                        );

                    } else if (matches.size() > 1) {

                        LOGGER.warn(
                                "[BCN] 无法迁移玩家 {} 的旧追踪标点「{}」：存在 {} 个同名标点",
                                playerUuid,
                                markerName,
                                matches.size()
                        );
                    }
                }
            }

            if (!trackedMarkers.isEmpty()) {

                manager.playerTrackers.put(
                        playerUuid,
                        trackedMarkers
                );
            }
        }

        return manager;
    }


    /**
     * 获取玩家追踪的标点名称
     */
    public List<String> getPlayerTrackedMarkerNames(UUID uuid) {

        List<QuestMarker> trackedMarkers = getPlayerTrackedMarkers(uuid);

        List<String> result =
                new ArrayList<>();

        for (QuestMarker marker : trackedMarkers) {
            result.add(marker.name);
        }

        return result;
    }


    /**
     * 保存数据到 NBT
     */
    @Override
    public CompoundTag save(CompoundTag tag) {

        ListTag markerList = new ListTag();

        for (QuestMarker marker : markers) {

            markerList.add(
                    marker.save(new CompoundTag())
            );
        }

        tag.put(
                "markers",
                markerList
        );

        /*
         * 保存玩家追踪数据
         */
        ListTag trackerList = new ListTag();

        for (Map.Entry<UUID, Set<UUID>> entry
                : playerTrackers.entrySet()) {

            CompoundTag playerTag = new CompoundTag();

            playerTag.putUUID(
                    "uuid",
                    entry.getKey()
            );

            ListTag trackedMarkerList = new ListTag();

            for (UUID markerId : entry.getValue()) {

                CompoundTag markerTag = new CompoundTag();

                markerTag.putUUID(
                        "id",
                        markerId
                );

                trackedMarkerList.add(
                        markerTag
                );
            }

            playerTag.put(
                    "markers",
                    trackedMarkerList
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
    public static QuestManager get(ServerLevel level) {

        DimensionDataStorage storage =
                level.getDataStorage();

        return storage.computeIfAbsent(
                QuestManager::load,
                QuestManager::new,
                "quest_manager"
        );
    }

    /**
     * 添加标点
     */
    public void addMarker(QuestMarker marker) {

        Objects.requireNonNull(
                marker,
                "QuestMarker 不能为 null"
        );

        /*
         * UUID 是程序身份，因此理论上不应该重复
         */
        if (getMarker(marker.getId()) != null) {

            throw new IllegalArgumentException(
                    "不能添加重复 UUID 的标点: "
                            + marker.getId()
            );
        }

        LOGGER.debug(
                "[BCN] Added Marker {} ({})",
                marker.name,
                marker.getId()
        );

        markers.add(marker);

        setDirty();
    }

    /**
     * 根据 UUID 精确获取标点
     *
     * @param id 标点 UUID
     * @return 对应标点，不存在时返回 null
     */
    public QuestMarker getMarker(UUID id) {

        if (id == null) {
            return null;
        }

        for (QuestMarker marker : markers) {

            if (marker.getId().equals(id)) {
                return marker;
            }
        }

        return null;
    }

    /**
     * 根据名称查找全部标点
     */
    public List<QuestMarker> findMarkers(String name) {

        if (name == null) {
            return Collections.emptyList();
        }

        String target = name.trim();

        if (target.isEmpty()) {
            return Collections.emptyList();
        }

        List<QuestMarker> result =
                new ArrayList<>();

        for (QuestMarker marker : markers) {

            if (marker.name.equalsIgnoreCase(target)) {

                result.add(marker);
            }
        }

        return result;
    }

    /**
     * 检查是否存在同名标点
     */
    public boolean existsMarker(String name) {

        return !findMarkers(name).isEmpty();
    }

    /**
     * 获取标点
     */
    public QuestMarker getMarker(String name) {

        List<QuestMarker> matches = findMarkers(name);

        if (matches.size() != 1) {
            return null;
        }

        return matches.get(0);
    }

    /**
     * 重命名标点
     *
     * <p>
     * 现在允许重命名成已经存在的名称。
     * 因为名称不是唯一身份。
     */
    public boolean renameMarker(
            String oldName,
            String newName
    ) {

        if (oldName == null || newName == null) {
            return false;
        }

        String oldValue = oldName.trim();

        String newValue = newName.trim();

        if (oldValue.isEmpty() || newValue.isEmpty()) {
            return false;
        }

        QuestMarker marker = getMarker(oldValue);

        if (marker == null) {
            return false;
        }

        marker.name = newValue;

        /*
         * 玩家追踪数据使用 UUID
         */

        LOGGER.debug(
                "[BCN] Renamed Marker {} -> {} ({})",
                oldValue,
                newValue,
                marker.getId()
        );

        setDirty();

        return true;
    }

    /**
     * 根据 UUID 删除标点
     */
    public boolean removeMarker(UUID id) {

        if (id == null) {
            return false;
        }

        QuestMarker marker = getMarker(id);

        if (marker == null) {
            return false;
        }

        boolean removed = markers.remove(marker);

        if (!removed) {
            return false;
        }

        /*
         * 同时从所有玩家追踪数据中删除该 UUID
         */
        for (Set<UUID> tracked
                : playerTrackers.values()) {

            tracked.remove(id);
        }

        LOGGER.debug(
                "[BCN] Removed Marker {} ({})",
                marker.name,
                marker.getId()
        );

        setDirty();

        return true;
    }

    /**
     * 根据名称删除标点
     */
    public boolean removeMarker(String name) {

        QuestMarker marker = getMarker(name);

        if (marker == null) {
            return false;
        }

        return removeMarker(marker.getId());
    }

    /**
     * 获取所有标点
     */
    public List<QuestMarker> getMarkers() {

        return Collections.unmodifiableList(
                markers
        );
    }

    /**
     * 获取玩家追踪的标点 UUID
     */
    public Set<UUID> getPlayerTrackedMarkerIds(
            UUID uuid
    ) {

        if (uuid == null) {
            return Collections.emptySet();
        }

        Set<UUID> trackers = playerTrackers.get(uuid);

        if (trackers == null) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(
                trackers
        );
    }

    /**
     * 获取玩家追踪的标点
     */
    public List<QuestMarker> getPlayerTrackedMarkers(
            UUID uuid
    ) {

        if (uuid == null) {
            return Collections.emptyList();
        }

        Set<UUID> trackedIds = playerTrackers.get(uuid);

        if (trackedIds == null || trackedIds.isEmpty()) {

            return Collections.emptyList();

        }

        List<QuestMarker> result = new ArrayList<>();

        for (UUID markerId : trackedIds) {

            QuestMarker marker =
                    getMarker(markerId);

            if (marker != null) {
                result.add(marker);
            }
        }

        return result;
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

        return trackPlayerMarker(
                uuid,
                marker.getId()
        );
    }

    /**
     * 玩家通过 UUID 添加追踪
     */
    public boolean trackPlayerMarker(
            UUID uuid,
            UUID markerId
    ) {

        if (uuid == null || markerId == null) {
            return false;
        }

        QuestMarker marker = getMarker(markerId);

        if (marker == null) {
            return false;
        }

        playerTrackers
                .computeIfAbsent(
                        uuid,
                        ignored -> new HashSet<>()
                )
                .add(markerId);

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

        QuestMarker marker = getMarker(name);

        if (marker == null) {
            return false;
        }

        return untrackPlayerMarker(
                uuid,
                marker.getId()
        );
    }

    /**
     * 玩家通过 UUID 取消追踪
     */
    public boolean untrackPlayerMarker(
            UUID uuid,
            UUID markerId
    ) {

        if (uuid == null || markerId == null) {
            return false;
        }

        Set<UUID> trackers = playerTrackers.get(uuid);

        if (trackers == null) {
            return false;
        }

        boolean result =
                trackers.remove(markerId);

        if (trackers.isEmpty()) {

            playerTrackers.remove(uuid);
        }

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

        if (uuid == null) {
            return;
        }

        if (playerTrackers.remove(uuid) != null) {
            setDirty();
        }
    }

    /**
     * 判断玩家是否追踪
     */
    public boolean isPlayerTracking(
            UUID uuid,
            String name
    ) {

        QuestMarker marker =
                getMarker(name);

        if (marker == null) {
            return false;
        }

        return isPlayerTracking(
                uuid,
                marker.getId()
        );
    }

    /*
     * 判断玩家是否追踪某个 UUID 标点
     */
    public boolean isPlayerTracking(
            UUID uuid,
            UUID markerId
    ) {

        if (uuid == null || markerId == null) {
            return false;
        }

        Set<UUID> trackers =
                playerTrackers.get(uuid);

        return trackers != null
                && trackers.contains(markerId);
    }
}