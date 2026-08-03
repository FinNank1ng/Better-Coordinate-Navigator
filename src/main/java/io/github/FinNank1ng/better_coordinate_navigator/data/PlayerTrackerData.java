package io.github.FinNank1ng.better_coordinate_navigator.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;


public class PlayerTrackerData extends SavedData {


    private final Map<UUID, Set<String>> trackers = new HashMap<>();

    /**
     * 添加玩家追踪
     */
    public void track(
            UUID player,
            String marker
    ){

        trackers.computeIfAbsent(
                player,
                k -> new HashSet<>()
        ).add(marker);

        setDirty();

    }



    /**
     * 移除玩家追踪
     */
    public void untrack(
            UUID player,
            String marker
    ){

        Set<String> list = trackers.get(player);

        if(list != null){

            list.remove(marker);

            setDirty();

        }

    }



    /**
     * 获取玩家追踪列表
     */
    public Set<String> getTracked(
            UUID player
    ){

        return trackers.getOrDefault(
                player,
                new HashSet<>()
        );

    }



    /**
     * 判断是否追踪
     */
    public boolean isTracked(
            UUID player,
            String marker
    ){

        return getTracked(player)
                .contains(marker);

    }



    @Override
    public CompoundTag save(CompoundTag tag) {


        ListTag players =
                new ListTag();


        for(var entry : trackers.entrySet()){


            CompoundTag player =
                    new CompoundTag();

            player.putUUID("uuid", entry.getKey());

            ListTag markers =
                    new ListTag();


            for(String name : entry.getValue()){

                markers.add(
                        StringTag.valueOf(name)
                );

            }

            player.put("markers", markers);

            players.add(player);

        }


        tag.put("players", players);


        return tag;

    }

}