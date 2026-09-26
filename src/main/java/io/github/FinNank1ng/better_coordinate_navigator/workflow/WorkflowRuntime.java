package io.github.FinNank1ng.better_coordinate_navigator.workflow;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

/*
 * BCN 工作流服务器运行时
 */
@Mod.EventBusSubscriber(
        modid = "better_coordinate_navigator"
)
public final class WorkflowRuntime {

    private static WorkflowManager workflowManager;

    private WorkflowRuntime() {
    }

    /*
     * 服务器启动
     */
    @SubscribeEvent
    public static void onServerStarted(
            ServerStartedEvent event
    ) {

        MinecraftServer server =
                event.getServer();

        WorkflowSavedData savedData =
                WorkflowDataManager.get(
                        server.overworld()
                );

        workflowManager =
                new WorkflowManager(
                        savedData
                );
    }

    /*
     * 服务器 Tick
     */
    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {

        if (event.phase
                != TickEvent.Phase.END) {
            return;
        }

        if (workflowManager == null) {
            return;
        }

        MinecraftServer server =
                event.getServer();

        for (ServerPlayer player :
                server.getPlayerList().getPlayers()) {

            workflowManager.tick(
                    player
            );
        }
    }

    /*
     * 服务器停止
     */
    @SubscribeEvent
    public static void onServerStopping(
            ServerStoppingEvent event
    ) {
        workflowManager = null;
    }

    /*
     * 获取当前服务器的工作流管理器
     */
    public static WorkflowManager getManager() {

        return Objects.requireNonNull(
                workflowManager,
                "WorkflowManager 尚未初始化"
        );
    }
}