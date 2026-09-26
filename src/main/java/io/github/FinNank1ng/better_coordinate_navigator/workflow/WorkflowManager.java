package io.github.FinNank1ng.better_coordinate_navigator.workflow;

import com.mojang.logging.LogUtils;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestManager;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 工作流运行管理器
 */
public class WorkflowManager {

    private static final Logger LOGGER =
            LogUtils.getLogger();

    /*
     * 已注册的全部工作流
     */
    private final Map<UUID, Workflow> workflows =
            new HashMap<>();

    /*
     * 每个玩家的工作流进度
     */
    private final Map<UUID, Map<UUID, WorkflowProgress>> progress =
            new HashMap<>();

    /*
     * 记录“当前步骤找不到 Marker”已经警告过哪一步
     *
     * 避免一个错误 Marker 导致每个 Server Tick 都刷日志
     */
    private final Map<UUID, Map<UUID, Integer>> missingMarkerWarningSteps =
            new HashMap<>();


    /*
     * 创建工作流运行管理器
     */
    public WorkflowManager(
            WorkflowSavedData savedData
    ) {

        loadFromSavedData(
                savedData
        );
    }


    /*
     * 注册工作流
     */
    public void registerWorkflow(
            Workflow workflow
    ) {

        Objects.requireNonNull(
                workflow,
                "Workflow 不能为 null"
        );

        workflows.put(
                workflow.getId(),
                workflow
        );
    }


    /*
     * 删除工作流
     */
    public void removeWorkflow(
            UUID workflowId
    ) {

        Objects.requireNonNull(
                workflowId,
                "workflowId 不能为 null"
        );

        workflows.remove(
                workflowId
        );

        progress.values().forEach(
                playerProgress ->
                        playerProgress.remove(
                                workflowId
                        )
        );

        missingMarkerWarningSteps.values().forEach(
                playerWarnings ->
                        playerWarnings.remove(
                                workflowId
                        )
        );

        cleanupEmptyPlayerProgress();
        cleanupEmptyMissingMarkerWarnings();
    }


    /*
     * 替换工作流
     */
    public void replaceWorkflow(
            Workflow workflow
    ) {

        Objects.requireNonNull(
                workflow,
                "Workflow 不能为 null"
        );

        if (!workflows.containsKey(
                workflow.getId()
        )) {

            throw new IllegalArgumentException(
                    "不存在的 Workflow: "
                            + workflow.getId()
            );
        }

        workflows.put(
                workflow.getId(),
                workflow
        );
    }


    /*
     * 获取工作流
     */
    public Workflow getWorkflow(
            UUID workflowId
    ) {

        if (workflowId == null) {
            return null;
        }

        return workflows.get(
                workflowId
        );
    }


    /*
     * 按名称查找工作流
     */
    public List<Workflow> findWorkflowsByName(
            String name
    ) {

        if (name == null
                || name.isBlank()) {

            return List.of();
        }

        List<Workflow> matches = new ArrayList<>();

        for (Workflow workflow :
                workflows.values()) {

            if (name.equals(
                    workflow.getName()
            )) {

                matches.add(
                        workflow
                );
            }
        }

        return List.copyOf(
                matches
        );
    }


    /*
     * 按输入解析工作流
     */
    public Workflow resolveWorkflow(
            String input
    ) {

        if (input == null
                || input.isBlank()) {

            return null;
        }

        /*
         * 优先尝试 UUID
         */
        try {

            UUID workflowId =
                    UUID.fromString(input);

            Workflow workflow =
                    workflows.get(
                            workflowId
                    );

            if (workflow != null) {

                return workflow;
            }

        } catch (IllegalArgumentException ignored) {

            /*
             * 不是合法 UUID，继续按照名称查找
             */
        }

        List<Workflow> matches =
                findWorkflowsByName(
                        input
                );

        if (matches.isEmpty()) {

            return null;
        }

        /*
         * 同名工作流属于歧义情况
         *
         * 不自动挑一个，避免启动 / 追踪错误任务
         */
        if (matches.size() > 1) {

            throw new IllegalStateException(
                    "存在同名 Workflow: "
                            + input
                            + "，请使用 UUID 区分"
            );
        }

        return matches.get(0);
    }


    /*
     * 获取全部工作流
     */
    public List<Workflow> getWorkflows() {

        return List.copyOf(
                workflows.values()
        );
    }


    /*
     * 从持久化数据加载工作流定义
     */
    public void loadFromSavedData(
            WorkflowSavedData savedData
    ) {

        Objects.requireNonNull(
                savedData,
                "WorkflowSavedData 不能为 null"
        );

        workflows.clear();

        for (Workflow workflow :
                savedData.getWorkflows()) {

            workflows.put(
                    workflow.getId(),
                    workflow
            );
        }
    }


    /*
     * 启动 / 继续一个工作流
     */
    public boolean startWorkflow(
            ServerPlayer player,
            UUID workflowId
    ) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        Objects.requireNonNull(
                workflowId,
                "workflowId 不能为 null"
        );

        Workflow workflow =
                workflows.get(
                        workflowId
                );

        if (workflow == null) {

            LOGGER.warn(
                    "[BCN] 尝试启动不存在的工作流: {}",
                    workflowId
            );

            return false;
        }

        if (workflow.isEmpty()) {

            LOGGER.warn(
                    "[BCN] 尝试启动空工作流: {} ({})",
                    workflow.getName(),
                    workflowId
            );

            return false;
        }

        Map<UUID, WorkflowProgress> playerProgress =
                progress.computeIfAbsent(
                        player.getUUID(),
                        uuid -> new HashMap<>()
                );

        WorkflowProgress current =
                playerProgress.get(
                        workflowId
                );

        /*
         * 第一次启动
         */
        if (current == null) {

            current =
                    new WorkflowProgress(
                            workflowId
                    );

            playerProgress.put(
                    workflowId,
                    current
            );
        }

        /*
         * 已经运行中
         */
        if (current.isRunning()) {

            return false;
        }

        /*
         * 已完成
         */
        if (current.isCompleted()) {

            return false;
        }

        /*
         * PAUSED / NOT_STARTED -> RUNNING
         */
        current.start();

        clearMissingMarkerWarning(
                player.getUUID(),
                workflowId
        );

        LOGGER.info(
                "[BCN] 玩家 {} {}工作流 {} ({})，当前步骤 {}",
                player.getGameProfile().getName(),
                current.stepIndex() == 0
                        ? "开始"
                        : "继续",
                workflow.getName(),
                workflowId,
                current.stepIndex()
        );

        return true;
    }


    /*
     * 暂停指定工作流，而暂停不会删除进度
     */
    public boolean pauseWorkflow(
            ServerPlayer player,
            UUID workflowId
    ) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        Objects.requireNonNull(
                workflowId,
                "workflowId 不能为 null"
        );

        WorkflowProgress current =
                getProgress(
                        player,
                        workflowId
                );

        if (current == null) {

            return false;
        }

        if (!current.isRunning()) {

            return false;
        }

        current.pause();

        Workflow workflow =
                workflows.get(
                        workflowId
                );

        LOGGER.info(
                "[BCN] 玩家 {} 暂停工作流 {}，当前步骤 {}",
                player.getGameProfile().getName(),
                workflow == null
                        ? workflowId
                        : workflow.getName(),
                current.stepIndex()
        );

        return true;
    }


    /*
     * 停止指定工作流
     *
     * 停止后回到 NOT_STARTED，同时将当前步骤重置为 Step 0
     */
    public boolean stopWorkflow(
            ServerPlayer player,
            UUID workflowId
    ) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        Objects.requireNonNull(
                workflowId,
                "workflowId 不能为 null"
        );

        WorkflowProgress current =
                getProgress(
                        player,
                        workflowId
                );

        if (current == null) {

            return false;
        }

        if (!current.isRunning()
                && !current.isPaused()) {

            return false;
        }

        current.stop();

        clearMissingMarkerWarning(
                player.getUUID(),
                workflowId
        );

        Workflow workflow =
                workflows.get(
                        workflowId
                );

        LOGGER.info(
                "[BCN] 玩家 {} 停止工作流 {}",
                player.getGameProfile().getName(),
                workflow == null
                        ? workflowId
                        : workflow.getName()
        );

        return true;
    }


    /*
     * 重置工作流进度
     *
     * 重置后再次 start 会从 Step 0 开始
     */
    public boolean resetWorkflow(
            ServerPlayer player,
            UUID workflowId
    ) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        Objects.requireNonNull(
                workflowId,
                "workflowId 不能为 null"
        );

        Map<UUID, WorkflowProgress> playerProgress =
                progress.get(
                        player.getUUID()
                );

        if (playerProgress == null) {

            return false;
        }

        WorkflowProgress removed =
                playerProgress.remove(
                        workflowId
                );

        clearMissingMarkerWarning(
                player.getUUID(),
                workflowId
        );

        if (playerProgress.isEmpty()) {

            progress.remove(
                    player.getUUID()
            );
        }

        if (removed == null) {

            return false;
        }

        LOGGER.info(
                "[BCN] 玩家 {} 重置工作流 {}",
                player.getGameProfile().getName(),
                workflowId
        );

        return true;
    }


    /*
     * 获取指定工作流的运行进度
     */
    public WorkflowProgress getProgress(
            ServerPlayer player,
            UUID workflowId
    ) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        Objects.requireNonNull(
                workflowId,
                "workflowId 不能为 null"
        );

        Map<UUID, WorkflowProgress> playerProgress =
                progress.get(
                        player.getUUID()
                );

        if (playerProgress == null) {

            return null;
        }

        return playerProgress.get(
                workflowId
        );
    }


    /*
     * 获取指定工作流当前步骤
     */
    public WorkflowStep getCurrentStep(
            ServerPlayer player,
            UUID workflowId
    ) {

        Workflow workflow =
                getWorkflow(
                        workflowId
                );

        WorkflowProgress current =
                getProgress(
                        player,
                        workflowId
                );

        if (workflow == null
                || current == null
                || current.isCompleted()) {

            return null;
        }

        int stepIndex =
                current.stepIndex();

        if (stepIndex < 0
                || stepIndex >= workflow.getStepCount()) {

            return null;
        }

        return workflow.getStep(
                stepIndex
        );
    }


    /*
     * 判断工作流是否正在运行
     */
    public boolean isRunning(
            ServerPlayer player,
            UUID workflowId
    ) {

        WorkflowProgress current =
                getProgress(
                        player,
                        workflowId
                );

        return current != null
                && current.isRunning();
    }


    /*
     * 判断工作流是否已暂停
     */
    public boolean isPaused(
            ServerPlayer player,
            UUID workflowId
    ) {

        WorkflowProgress current =
                getProgress(
                        player,
                        workflowId
                );

        return current != null
                && current.isPaused();
    }


    /*
     * 判断工作流是否已完成
     */
    public boolean isCompleted(
            ServerPlayer player,
            UUID workflowId
    ) {

        WorkflowProgress current =
                getProgress(
                        player,
                        workflowId
                );

        return current != null
                && current.isCompleted();
    }


    /*
     * 判断玩家是否正在运行任意工作流
     */
    public boolean isRunning(
            ServerPlayer player
    ) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        Map<UUID, WorkflowProgress> playerProgress =
                progress.get(
                        player.getUUID()
                );

        if (playerProgress == null
                || playerProgress.isEmpty()) {

            return false;
        }

        for (WorkflowProgress current :
                playerProgress.values()) {

            if (current.isRunning()) {

                return true;
            }
        }

        return false;
    }


    /*
     * 每个服务器 Tick 调用
     */
    public void tick(
            ServerPlayer player
    ) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        Map<UUID, WorkflowProgress> playerProgress =
                progress.get(
                        player.getUUID()
                );

        if (playerProgress == null
                || playerProgress.isEmpty()) {

            return;
        }

        /*
         * 使用快照进行 Tick
         */
        List<WorkflowProgress> activeWorkflows =
                new ArrayList<>(
                        playerProgress.values()
                );

        for (WorkflowProgress currentProgress :
                activeWorkflows) {

            if (!currentProgress.isRunning()) {

                continue;
            }

            tickWorkflow(
                    player,
                    currentProgress
            );
        }
    }


    /*
     * Tick 单个工作流
     */
    private void tickWorkflow(
            ServerPlayer player,
            WorkflowProgress currentProgress
    ) {

        Workflow workflow =
                workflows.get(
                        currentProgress.workflowId()
                );

        if (workflow == null) {

            /*
             * Workflow 定义已经不存在时进度也必须失效
             */
            LOGGER.warn(
                    "[BCN] 工作流 {} 已不存在，移除玩家 {} 的运行进度",
                    currentProgress.workflowId(),
                    player.getGameProfile().getName()
            );

            removePlayerWorkflow(
                    player,
                    currentProgress.workflowId()
            );

            return;
        }

        int stepIndex =
                currentProgress.stepIndex();

        /*
         * 进度已经走到工作流末尾
         * 这是一个防御性分支，正常情况下最后一步会直接调用 completeWorkflow
         */
        if (stepIndex >= workflow.getStepCount()) {

            completeWorkflow(
                    player,
                    workflow
            );

            return;
        }

        if (stepIndex < 0) {

            LOGGER.error(
                    "[BCN] 工作流 {} 出现非法步骤索引: {}，玩家: {}",
                    workflow.getName(),
                    stepIndex,
                    player.getGameProfile().getName()
            );

            resetWorkflow(
                    player,
                    workflow.getId()
            );

            return;
        }

        WorkflowStep step =
                workflow.getStep(
                        stepIndex
                );

        if (step == null) {

            LOGGER.error(
                    "[BCN] 工作流 {} 的步骤 {} 不存在，玩家: {}",
                    workflow.getName(),
                    stepIndex,
                    player.getGameProfile().getName()
            );

            return;
        }

        QuestMarker marker =
                findMarker(
                        player,
                        step.getMarkerId()
                );

        if (marker == null) {

            warnMissingMarkerOnce(
                    player,
                    workflow,
                    stepIndex,
                    step.getMarkerId()
            );

            return;
        }

        clearMissingMarkerWarning(
                player.getUUID(),
                workflow.getId()
        );

        if (!isInsideMarker(
                player,
                marker,
                step.getTriggerRadius()
        )) {

            return;
        }

        onStepReached(
                player,
                workflow,
                step,
                marker,
                stepIndex
        );
    }


    /*
     * 玩家到达步骤
     */
    private void onStepReached(
            ServerPlayer player,
            Workflow workflow,
            WorkflowStep step,
            QuestMarker marker,
            int stepIndex
    ) {

        WorkflowProgress currentProgress =
                getProgress(
                        player,
                        workflow.getId()
                );

        /*
         * 防御性检查：只有当前进度仍然对应这个步骤时，
         * 才允许执行 Action
         */
        if (currentProgress == null
                || !currentProgress.isRunning()
                || currentProgress.stepIndex() != stepIndex) {

            return;
        }

        LOGGER.info(
                "[BCN] 玩家 {} 到达工作流 {} 步骤 {}，目标标点: {}，位置: ({}, {}, {})",
                player.getGameProfile().getName(),
                workflow.getName(),
                stepIndex,
                marker.name,
                marker.x,
                marker.y,
                marker.z
        );

        /*
         * 当前节点的所有 Action 只会在当前步骤被触发一次
         */
        executeActions(
                player,
                step
        );

        int nextStep =
                stepIndex + 1;

        /*
         * 当前节点已经是最后一个节点
         */
        if (nextStep >= workflow.getStepCount()) {

            completeWorkflow(
                    player,
                    workflow
            );

            return;
        }

        /*
         * 当前对象仍然有效时，直接推进步骤
         */
        WorkflowProgress latestProgress =
                getProgress(
                        player,
                        workflow.getId()
                );

        if (latestProgress == null
                || !latestProgress.isRunning()) {

            return;
        }

        latestProgress.setStepIndex(
                nextStep
        );

        clearMissingMarkerWarning(
                player.getUUID(),
                workflow.getId()
        );

        LOGGER.info(
                "[BCN] 工作流 {} 步骤推进: {} -> {}，玩家: {}",
                workflow.getName(),
                stepIndex,
                nextStep,
                player.getGameProfile().getName()
        );
    }


    /*
     * 执行步骤中的全部动作
     */
    private void executeActions(
            ServerPlayer player,
            WorkflowStep step
    ) {

        for (WorkflowAction action :
                step.getActions()) {

            try {

                executeAction(
                        player,
                        action
                );

            } catch (Exception exception) {

                LOGGER.error(
                        "[BCN] Workflow Action 执行失败: type={}, data={}",
                        action.getType(),
                        action.getData(),
                        exception
                );
            }
        }
    }


    /*
     * 执行单个 Action
     */
    private void executeAction(
            ServerPlayer player,
            WorkflowAction action
    ) {

        switch (action.getType()) {

            case EXECUTE_COMMAND -> {

                String command =
                        action.getData();

                if (command == null
                        || command.isBlank()) {

                    return;
                }

                if (command.startsWith("/")) {

                    command =
                            command.substring(1);
                }

                player.server
                        .getCommands()
                        .performPrefixedCommand(
                                player.createCommandSourceStack(),
                                command
                        );
            }


            case MESSAGE -> {

                String message =
                        action.getData();

                if (message == null
                        || message.isEmpty()) {

                    return;
                }

                player.sendSystemMessage(
                        Component.literal(
                                message
                        )
                );
            }


            case GIVE_ITEM -> {

                String itemId =
                        action.getData();

                if (itemId == null
                        || itemId.isBlank()
                        || action.getCount() <= 0) {

                    return;
                }

                String command =
                        "give @s "
                                + itemId
                                + " "
                                + action.getCount();

                player.server
                        .getCommands()
                        .performPrefixedCommand(
                                player.createCommandSourceStack(),
                                command
                        );
            }


            case ENABLE_MARKER -> {

                /*
                 * TODO:
                 * 后续接入标点状态系统
                 */
                LOGGER.debug(
                        "[BCN] ENABLE_MARKER: {}",
                        action.getData()
                );
            }


            case DISABLE_MARKER -> {

                /*
                 * TODO:
                 * 后续接入标点状态系统
                 */
                LOGGER.debug(
                        "[BCN] DISABLE_MARKER: {}",
                        action.getData()
                );
            }


            case SOUND -> {

                String soundId =
                        action.getData();

                if (soundId == null
                        || soundId.isBlank()) {

                    LOGGER.warn(
                            "[BCN] SOUND 音效 ID 为空"
                    );

                    return;
                }

                ResourceLocation soundLocation =
                        ResourceLocation.tryParse(
                                soundId
                        );

                if (soundLocation == null) {

                    LOGGER.warn(
                            "[BCN] 无效的声音 ID: {}",
                            soundId
                    );

                    return;
                }

                SoundEvent sound =
                        BuiltInRegistries.SOUND_EVENT
                                .getOptional(
                                        soundLocation
                                )
                                .orElse(null);

                if (sound == null) {

                    LOGGER.warn(
                            "[BCN] 找不到声音: {}",
                            soundId
                    );

                    return;
                }

                player.playNotifySound(
                        sound,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );

                LOGGER.info(
                        "[BCN] 播放声音: {}",
                        soundId
                );
            }
        }
    }


    /*
     * 查找标点
     */
    private QuestMarker findMarker(
            ServerPlayer player,
            UUID markerId
    ) {

        if (markerId == null) {
            return null;
        }

        QuestManager manager =
                QuestManager.get(
                        player.serverLevel()
                );

        for (QuestMarker marker :
                manager.getMarkers()) {

            if (markerId.equals(
                    marker.getId()
            )) {

                return marker;
            }
        }

        return null;
    }


    /*
     * 判断玩家是否进入标点触发范围
     *
     * 使用平方距离，避免调用 sqrt
     */
    private boolean isInsideMarker(
            ServerPlayer player,
            QuestMarker marker,
            double radius
    ) {

        double dx =
                player.getX() - marker.x;

        double dy =
                player.getY() - marker.y;

        double dz =
                player.getZ() - marker.z;

        double squaredDistance =
                dx * dx
                        + dy * dy
                        + dz * dz;

        double squaredRadius =
                radius * radius;

        return squaredDistance <= squaredRadius;
    }


    /*
     * 完成工作流
     */
    private void completeWorkflow(
            ServerPlayer player,
            Workflow workflow
    ) {

        WorkflowProgress current =
                getProgress(
                        player,
                        workflow.getId()
                );

        if (current == null) {

            return;
        }

        /*
         * 防止完成逻辑重复执行
         */
        if (current.isCompleted()) {

            return;
        }

        current.complete();

        /*
         * stepIndex 最终对齐到 stepCount
         */
        current.setStepIndex(
                workflow.getStepCount()
        );

        clearMissingMarkerWarning(
                player.getUUID(),
                workflow.getId()
        );

        player.sendSystemMessage(
                Component.literal(
                        "§a任务完成：§f"
                                + workflow.getName()
                )
        );

        LOGGER.info(
                "[BCN] 玩家 {} 完成工作流 {} ({})",
                player.getGameProfile().getName(),
                workflow.getName(),
                workflow.getId()
        );
    }


    /*
     * 从玩家运行状态中移除指定工作流
     * 不用于普通“暂停”
     */
    private void removePlayerWorkflow(
            ServerPlayer player,
            UUID workflowId
    ) {

        Map<UUID, WorkflowProgress> playerProgress =
                progress.get(
                        player.getUUID()
                );

        if (playerProgress == null) {

            return;
        }

        playerProgress.remove(
                workflowId
        );

        clearMissingMarkerWarning(
                player.getUUID(),
                workflowId
        );

        if (playerProgress.isEmpty()) {

            progress.remove(
                    player.getUUID()
            );
        }
    }


    /*
     * 清理空玩家进度 Map
     */
    private void cleanupEmptyPlayerProgress() {

        progress.entrySet().removeIf(
                entry ->
                        entry.getValue().isEmpty()
        );
    }


    /*
     * Marker 缺失时只警告一次，
     * 直到该工作流进入下一步骤或 Marker 恢复
     */
    private void warnMissingMarkerOnce(
            ServerPlayer player,
            Workflow workflow,
            int stepIndex,
            UUID markerId
    ) {

        Map<UUID, Integer> playerWarnings =
                missingMarkerWarningSteps.computeIfAbsent(
                        player.getUUID(),
                        uuid -> new HashMap<>()
                );

        Integer lastWarningStep =
                playerWarnings.get(
                        workflow.getId()
                );

        if (Objects.equals(
                lastWarningStep,
                stepIndex
        )) {

            return;
        }

        playerWarnings.put(
                workflow.getId(),
                stepIndex
        );

        LOGGER.warn(
                "[BCN] 工作流 {} ({}) 的步骤 {} 找不到对应标点: {}，玩家: {}",
                workflow.getName(),
                workflow.getId(),
                stepIndex,
                markerId,
                player.getGameProfile().getName()
        );
    }


    /*
     * 清除指定工作流的缺失 Marker 警告状态
     */
    private void clearMissingMarkerWarning(
            UUID playerId,
            UUID workflowId
    ) {

        Map<UUID, Integer> playerWarnings =
                missingMarkerWarningSteps.get(
                        playerId
                );

        if (playerWarnings == null) {

            return;
        }

        playerWarnings.remove(
                workflowId
        );

        if (playerWarnings.isEmpty()) {

            missingMarkerWarningSteps.remove(
                    playerId
            );
        }
    }


    /*
     * 清理空的 Marker 警告 Map
     */
    private void cleanupEmptyMissingMarkerWarnings() {

        missingMarkerWarningSteps.entrySet().removeIf(
                entry ->
                        entry.getValue().isEmpty()
        );
    }


    /*
     * 工作流运行进度
     */
    public static final class WorkflowProgress {

        public enum Status {

            NOT_STARTED,

            RUNNING,

            PAUSED,

            COMPLETED
        }

        private final UUID workflowId;

        private int stepIndex;

        private Status status;


        public WorkflowProgress(
                UUID workflowId
        ) {

            this(
                    workflowId,
                    0,
                    Status.NOT_STARTED
            );
        }


        public WorkflowProgress(
                UUID workflowId,
                int stepIndex,
                Status status
        ) {

            this.workflowId =
                    Objects.requireNonNull(
                            workflowId,
                            "workflowId 不能为 null"
                    );

            if (stepIndex < 0) {

                throw new IllegalArgumentException(
                        "stepIndex 不能小于 0"
                );
            }

            this.stepIndex =
                    stepIndex;

            this.status =
                    Objects.requireNonNull(
                            status,
                            "status 不能为 null"
                    );
        }


        public UUID workflowId() {

            return workflowId;
        }


        public int stepIndex() {

            return stepIndex;
        }


        public Status status() {

            return status;
        }


        public boolean isRunning() {

            return status == Status.RUNNING;
        }


        public boolean isPaused() {

            return status == Status.PAUSED;
        }


        public boolean isCompleted() {

            return status == Status.COMPLETED;
        }


        public void start() {

            if (status == Status.COMPLETED) {

                throw new IllegalStateException(
                        "已完成的 WorkflowProgress 不能直接启动，"
                                + "请先 reset"
                );
            }

            status = Status.RUNNING;

        }


        public void pause() {

            if (status != Status.RUNNING) {

                return;
            }

            status = Status.PAUSED;

        }


        public void stop() {

            stepIndex = 0;

            status = Status.NOT_STARTED;

        }


        public void setStepIndex(
                int stepIndex
        ) {

            if (stepIndex < 0) {

                throw new IllegalArgumentException(
                        "stepIndex 不能小于 0"
                );
            }

            this.stepIndex =
                    stepIndex;
        }


        public void advanceStep() {

            setStepIndex(
                    stepIndex + 1
            );
        }


        public void complete() {

            status =
                    Status.COMPLETED;
        }
    }
}
