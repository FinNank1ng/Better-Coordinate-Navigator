package io.github.FinNank1ng.better_coordinate_navigator.road;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * BCN 路线管理器
 */
public class RoadManager {

    /**
     * 所有已注册的路线
     */
    private final Map<UUID, Road> roads = new HashMap<>();

    /**
     * 玩家当前正在进行的路线
     */
    private final Map<UUID, RoadProgress> playerProgress =
            new HashMap<>();

    /**
     * 注册路线
     */
    public void registerRoad(Road road) {

        Objects.requireNonNull(
                road,
                "Road 不能为 null"
        );

        roads.put(
                road.getId(),
                road
        );
    }

    /**
     * 删除路线
     */
    public void removeRoad(UUID roadId) {

        Objects.requireNonNull(
                roadId,
                "roadId 不能为 null"
        );

        roads.remove(roadId);

        /*
         * 如果有玩家正在进行这条路线，同时清除他们的路线进度
         */
        playerProgress.entrySet().removeIf(
                entry -> entry.getValue().roadId().equals(roadId)
        );
    }

    /**
     * 获取路线
     */
    public Road getRoad(UUID roadId) {
        return roads.get(roadId);
    }

    /**
     * 开始一条路线
     */
    public boolean startRoad(
            ServerPlayer player,
            UUID roadId
    ) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        Objects.requireNonNull(
                roadId,
                "roadId 不能为 null"
        );

        Road road = roads.get(roadId);

        if (road == null) {
            return false;
        }

        if (road.getNodeCount() == 0) {
            return false;
        }

        playerProgress.put(
                player.getUUID(),
                new RoadProgress(
                        roadId,
                        0
                )
        );

        System.out.println(
                "[BCN] 玩家 "
                        + player.getGameProfile().getName()
                        + " 开始路线 "
                        + road.getName()
        );

        return true;
    }

    /**
     * 停止玩家当前路线
     */
    public void stopRoad(ServerPlayer player) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        playerProgress.remove(
                player.getUUID()
        );
    }

    /**
     * 获取玩家当前路线进度
     */
    public RoadProgress getProgress(ServerPlayer player) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        return playerProgress.get(
                player.getUUID()
        );
    }

    /**
     * 玩家是否正在进行路线
     */
    public boolean isRunning(ServerPlayer player) {

        return playerProgress.containsKey(
                player.getUUID()
        );
    }

    /**
     * 路线进度
     *
     * @param roadId 当前路线
     * @param currentNodeIndex 当前激活的检查点
     */
    public record RoadProgress(
            UUID roadId,
            int currentNodeIndex
    ) {
    }


    /**
     * 每个服务器 Tick 检查玩家当前的路线进度
     */
    public void tick(ServerPlayer player) {

        Objects.requireNonNull(
                player,
                "player 不能为 null"
        );

        RoadProgress progress = playerProgress.get(
                player.getUUID()
        );

        /*
         * 玩家没有进行路线
         */
        if (progress == null) {
            return;
        }

        Road road = roads.get(
                progress.roadId()
        );

        /*
         * 路线不存在
         */
        if (road == null) {
            playerProgress.remove(
                    player.getUUID()
            );
            return;
        }

        /*
         * 所有节点已经完成
         */
        if (progress.currentNodeIndex() >= road.getNodeCount()) {

            playerProgress.remove(
                    player.getUUID()
            );

            System.out.println(
                    "[BCN] 玩家 "
                            + player.getGameProfile().getName()
                            + " 完成路线 "
                            + road.getName()
            );

            return;
        }

        RoadNode currentNode = road.getNode(
                progress.currentNodeIndex()
        );

        /*
         * 检查维度
         */
        if (!player.level().dimension().equals(
                currentNode.getDimension()
        )) {
            return;
        }

        /*
         * 检查玩家是否进入当前检查点
         */
        if (!currentNode.isInside(player.blockPosition())) {
            return;
        }

        /*
         * 当前检查点完成
         */
        int completedNodeIndex =
                progress.currentNodeIndex();

        int nextNodeIndex =
                completedNodeIndex + 1;

        System.out.println(
                "[BCN] 玩家 "
                        + player.getGameProfile().getName()
                        + " 到达检查点 "
                        + completedNodeIndex
                        + " / "
                        + (road.getNodeCount() - 1)
        );

        /*
         * 已经到达终点
         */
        if (nextNodeIndex >= road.getNodeCount()) {

            playerProgress.remove(
                    player.getUUID()
            );

            System.out.println(
                    "[BCN] 玩家 "
                            + player.getGameProfile().getName()
                            + " 完成路线 "
                            + road.getName()
            );

            return;
        }

        /*
         * 开启下一个检查点
         */
        playerProgress.put(
                player.getUUID(),
                new RoadProgress(
                        progress.roadId(),
                        nextNodeIndex
                )
        );

        System.out.println(
                "[BCN] 下一检查点："
                        + nextNodeIndex
        );
    }
}