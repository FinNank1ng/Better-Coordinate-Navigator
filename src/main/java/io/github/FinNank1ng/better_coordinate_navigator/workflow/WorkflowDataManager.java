package io.github.FinNank1ng.better_coordinate_navigator.workflow;

import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

/*
 * BCN 工作流数据管理器
 */
public final class WorkflowDataManager {

    private static final String DATA_NAME =
            "better_coordinate_navigator_workflows";

    private WorkflowDataManager() {
    }

    /*
     * 获取服务器世界中的工作流持久化数据
     */
    public static WorkflowSavedData get(
            ServerLevel level
    ) {

        Objects.requireNonNull(
                level,
                "ServerLevel 不能为 null"
        );

        return level.getDataStorage().computeIfAbsent(
                WorkflowSavedData::load,
                WorkflowSavedData::new,
                DATA_NAME
        );
    }
}