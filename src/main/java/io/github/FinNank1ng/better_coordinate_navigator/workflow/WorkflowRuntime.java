
package io.github.FinNank1ng.better_coordinate_navigator.workflow;

import net.minecraft.server.MinecraftServer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(
        modid = "better_coordinate_navigator"
)
public class WorkflowRuntime {

    private static WorkflowManager workflowManager;


    /*
     * 服务器启动时加载工作流定义和运行进度
     */
    @SubscribeEvent
    public static void onServerStarted(
            ServerStartedEvent event
    ) {

        MinecraftServer server =
                event.getServer();

        WorkflowSavedData workflowData =
                WorkflowDataManager.get(
                        server.overworld()
                );

        WorkflowProgressSavedData progressData =
                WorkflowProgressSavedData.get(
                        server.overworld()
                );

        workflowManager =
                new WorkflowManager(
                        workflowData,
                        progressData
                );
    }


    /*
     * 每个服务器 Tick 更新在线玩家的工作流
     */
    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (workflowManager == null) {
            return;
        }

        MinecraftServer server =
                event.getServer();

        for (var player :
                server.getPlayerList().getPlayers()) {

            workflowManager.tick(
                    player
            );
        }
    }


    /*
     * 服务器停止时补充标记进度数据
     */
    @SubscribeEvent
    public static void onServerStopping(
            ServerStoppingEvent event
    ) {

        if (workflowManager != null) {

            workflowManager.flushProgress();

            workflowManager = null;
        }
    }


    /*
     * 获取工作流管理器
     */
    public static WorkflowManager getManager() {

        return Objects.requireNonNull(
                workflowManager,
                "WorkflowManager 尚未初始化"
        );
    }
}
