package io.github.FinNank1ng.better_coordinate_navigator.road;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * BCN 一条路线
 */
public class Road {

    /**
     * 路线唯一 ID
     */
    private final UUID id;

    /**
     * 路线名称
     */
    private String name;

    /**
     * 路线所在维度
     */
    private final ResourceKey<Level> dimension;

    /**
     * 路线检查点
     */
    private final List<RoadNode> nodes = new ArrayList<>();

    public Road(
            UUID id,
            String name,
            ResourceKey<Level> dimension
    ) {
        this.id = Objects.requireNonNull(
                id,
                "Road id 不能为 null"
        );

        this.name = Objects.requireNonNull(
                name,
                "Road name 不能为 null"
        );

        this.dimension = Objects.requireNonNull(
                dimension,
                "Road dimension 不能为 null"
        );
    }

    /**
     * 创建一条新的路线
     */
    public static Road create(
            String name,
            ResourceKey<Level> dimension
    ) {
        return new Road(
                UUID.randomUUID(),
                name,
                dimension
        );
    }

    /**
     * 添加一个检查点
     */
    public void addNode(RoadNode node) {

        Objects.requireNonNull(
                node,
                "RoadNode 不能为 null"
        );

        if (!node.getDimension().equals(dimension)) {
            throw new IllegalArgumentException(
                    "RoadNode 与 Road 不在同一个维度"
            );
        }

        nodes.add(node);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {

        this.name = Objects.requireNonNull(
                name,
                "Road name 不能为 null"
        );
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    /**
     * 获取路线中的所有节点
     */
    public List<RoadNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /**
     * 获取路线节点数量
     */
    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * 获取指定顺序的节点
     */
    public RoadNode getNode(int index) {

        if (index < 0 || index >= nodes.size()) {
            throw new IndexOutOfBoundsException(
                    "RoadNode index 越界: " + index
            );
        }

        return nodes.get(index);
    }

    @Override
    public String toString() {
        return "Road{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", dimension=" + dimension.location()
                + ", nodes=" + nodes.size()
                + '}';
    }
}