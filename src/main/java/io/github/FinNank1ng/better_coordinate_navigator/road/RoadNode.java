package io.github.FinNank1ng.better_coordinate_navigator.road;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * BCN 路线检查点
 */
public class RoadNode {

    /**
     * 检查点在路线中的顺序
     */
    private final int order;

    /**
     * 检查点世界坐标
     */
    private final BlockPos position;

    /**
     * 检查点所在维度
     */
    private final ResourceKey<Level> dimension;

    /**
     * 触发半径
     */
    private final double radius;

    public RoadNode(
            int order,
            BlockPos position,
            ResourceKey<Level> dimension,
            double radius
    ) {
        if (order < 0) {
            throw new IllegalArgumentException(
                    "RoadNode order 不能小于 0"
            );
        }

        Objects.requireNonNull(
                position,
                "RoadNode position 不能为 null"
        );

        Objects.requireNonNull(
                dimension,
                "RoadNode dimension 不能为 null"
        );

        if (radius <= 0) {
            throw new IllegalArgumentException(
                    "RoadNode radius 必须大于 0"
            );
        }

        this.order = order;
        this.position = position.immutable();
        this.dimension = dimension;
        this.radius = radius;
    }

    public int getOrder() {
        return order;
    }

    public BlockPos getPosition() {
        return position;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public double getRadius() {
        return radius;
    }

    /**
     * 判断指定位置是否进入检查点触发范围。
     */
    public boolean isInside(BlockPos playerPosition) {

        Objects.requireNonNull(
                playerPosition,
                "playerPosition 不能为 null"
        );

        return position.distSqr(playerPosition)
                <= radius * radius;
    }

    @Override
    public String toString() {
        return "RoadNode{"
                + "order=" + order
                + ", position=" + position
                + ", dimension=" + dimension.location()
                + ", radius=" + radius
                + '}';
    }
}