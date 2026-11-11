
package io.github.FinNank1ng.better_coordinate_navigator.workflow;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 工作流运行进度持久化数据
 *
 * <p>保存玩家 UUID、工作流 UUID、当前步骤和运行状态</p>
 */
public class WorkflowProgressSavedData extends SavedData {

    private static final String DATA_NAME =
            "better_coordinate_navigator_workflow_progress";

    private static final String TAG_PLAYER_PROGRESS =
            "playerProgress";

    private static final String TAG_PLAYER_ID =
            "playerId";

    private static final String TAG_WORKFLOWS =
            "workflows";

    private static final String TAG_WORKFLOW_ID =
            "workflowId";

    private static final String TAG_STEP_INDEX =
            "stepIndex";

    private static final String TAG_STATUS =
            "status";

    /*
     * 玩家 UUID
     *     -> 工作流 UUID
     *         -> WorkflowProgress
     */
    private final Map<UUID, Map<UUID, WorkflowManager.WorkflowProgress>> progress =
            new HashMap<>();

    public WorkflowProgressSavedData() {
    }

    /**
     * 从 NBT 加载运行进度
     */
    public static WorkflowProgressSavedData load(CompoundTag tag) {

        WorkflowProgressSavedData data =
                new WorkflowProgressSavedData();

        ListTag playerList =
                tag.getList(
                        TAG_PLAYER_PROGRESS,
                        CompoundTag.TAG_COMPOUND
                );

        for (int i = 0; i < playerList.size(); i++) {

            CompoundTag playerTag =
                    playerList.getCompound(i);

            if (!playerTag.hasUUID(TAG_PLAYER_ID)) {
                continue;
            }

            UUID playerId =
                    playerTag.getUUID(TAG_PLAYER_ID);

            Map<UUID, WorkflowManager.WorkflowProgress> playerProgress =
                    new HashMap<>();

            ListTag workflowList =
                    playerTag.getList(
                            TAG_WORKFLOWS,
                            CompoundTag.TAG_COMPOUND
                    );

            for (int j = 0; j < workflowList.size(); j++) {

                CompoundTag workflowTag =
                        workflowList.getCompound(j);

                if (!workflowTag.hasUUID(TAG_WORKFLOW_ID)) {
                    continue;
                }

                UUID workflowId =
                        workflowTag.getUUID(TAG_WORKFLOW_ID);

                int stepIndex =
                        workflowTag.getInt(TAG_STEP_INDEX);

                String statusName =
                        workflowTag.getString(TAG_STATUS);

                WorkflowManager.WorkflowProgress.Status status;

                try {

                    status =
                            WorkflowManager.WorkflowProgress.Status.valueOf(
                                    statusName
                            );

                } catch (IllegalArgumentException exception) {

                    continue;
                }

                if (stepIndex < 0) {
                    continue;
                }

                WorkflowManager.WorkflowProgress workflowProgress =
                        new WorkflowManager.WorkflowProgress(
                                workflowId,
                                stepIndex,
                                status
                        );

                playerProgress.put(
                        workflowId,
                        workflowProgress
                );
            }

            if (!playerProgress.isEmpty()) {

                data.progress.put(
                        playerId,
                        playerProgress
                );
            }
        }

        return data;
    }

    /**
     * 保存全部玩家运行进度
     */
    @Override
    public CompoundTag save(CompoundTag tag) {

        ListTag playerList = new ListTag();

        for (Map.Entry<UUID, Map<UUID, WorkflowManager.WorkflowProgress>> playerEntry
                : progress.entrySet()) {

            CompoundTag playerTag = new CompoundTag();

            playerTag.putUUID(
                    TAG_PLAYER_ID,
                    playerEntry.getKey()
            );

            ListTag workflowList = new ListTag();

            for (WorkflowManager.WorkflowProgress workflowProgress
                    : playerEntry.getValue().values()) {

                CompoundTag workflowTag = new CompoundTag();

                workflowTag.putUUID(
                        TAG_WORKFLOW_ID,
                        workflowProgress.workflowId()
                );

                workflowTag.putInt(
                        TAG_STEP_INDEX,
                        workflowProgress.stepIndex()
                );

                workflowTag.putString(
                        TAG_STATUS,
                        workflowProgress.status().name()
                );

                workflowList.add(workflowTag);
            }

            playerTag.put(
                    TAG_WORKFLOWS,
                    workflowList
            );

            playerList.add(playerTag);
        }

        tag.put(
                TAG_PLAYER_PROGRESS,
                playerList
        );

        return tag;
    }

    /**
     * 获取主世界中的进度数据实例
     */
    public static WorkflowProgressSavedData get(ServerLevel level) {

        ServerLevel overworld =
                level.getServer().overworld();

        return overworld.getDataStorage().computeIfAbsent(
                WorkflowProgressSavedData::load,
                WorkflowProgressSavedData::new,
                DATA_NAME
        );
    }

    /**
     * 获取全部玩家进度快照
     */
    public Map<UUID, Map<UUID, WorkflowManager.WorkflowProgress>> getAllProgress() {

        Map<UUID, Map<UUID, WorkflowManager.WorkflowProgress>> result =
                new HashMap<>();

        for (Map.Entry<UUID, Map<UUID, WorkflowManager.WorkflowProgress>> entry
                : progress.entrySet()) {

            Map<UUID, WorkflowManager.WorkflowProgress> playerCopy =
                    new HashMap<>();

            for (WorkflowManager.WorkflowProgress current
                    : entry.getValue().values()) {

                playerCopy.put(
                        current.workflowId(),
                        new WorkflowManager.WorkflowProgress(
                                current.workflowId(),
                                current.stepIndex(),
                                current.status()
                        )
                );
            }

            result.put(
                    entry.getKey(),
                    playerCopy
            );
        }

        return result;
    }

    /**
     * 写入或更新玩家工作流进度
     */
    public void putProgress(
            UUID playerId,
            WorkflowManager.WorkflowProgress workflowProgress
    ) {

        if (playerId == null || workflowProgress == null) {
            return;
        }

        progress.computeIfAbsent(
                playerId,
                uuid -> new HashMap<>()
        ).put(
                workflowProgress.workflowId(),
                new WorkflowManager.WorkflowProgress(
                        workflowProgress.workflowId(),
                        workflowProgress.stepIndex(),
                        workflowProgress.status()
                )
        );

        setDirty();
    }

    /**
     * 删除指定玩家的指定工作流进度
     */
    public void removeProgress(
            UUID playerId,
            UUID workflowId
    ) {

        if (playerId == null || workflowId == null) {
            return;
        }

        Map<UUID, WorkflowManager.WorkflowProgress> playerProgress =
                progress.get(playerId);

        if (playerProgress == null) {
            return;
        }

        if (playerProgress.remove(workflowId) == null) {
            return;
        }

        if (playerProgress.isEmpty()) {
            progress.remove(playerId);
        }

        setDirty();
    }

    /**
     * 删除指定工作流的全部玩家进度
     */
    public void removeWorkflow(UUID workflowId) {

        if (workflowId == null) {
            return;
        }

        boolean changed = false;

        for (Map<UUID, WorkflowManager.WorkflowProgress> playerProgress
                : progress.values()) {

            changed |= playerProgress.remove(workflowId) != null;
        }

        progress.entrySet().removeIf(
                entry -> entry.getValue().isEmpty()
        );

        if (changed) {
            setDirty();
        }
    }

    /**
     * 删除指定玩家的全部工作流进度
     */
    public void removePlayer(UUID playerId) {

        if (playerId == null) {
            return;
        }

        if (progress.remove(playerId) != null) {
            setDirty();
        }
    }
}
