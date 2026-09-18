package io.github.FinNank1ng.better_coordinate_navigator.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class QuestMarker {

    /*
     * 基础信息
     */
    private final UUID id;

    public double x;
    public double y;
    public double z;

    public String name;

    public String description;

    // 保留以兼容旧存档
    public boolean tracked;

    /*
     * 是否启用
     */
    public boolean active;

    /*
     * 渲染类型
     *
     * DEFAULT
     * NPC
     * BOSS
     * DUNGEON
     * CUSTOM
     */
    public String iconType;

    /*
     * 自定义贴图
     *
     * assets/...png
     */
    public String iconName;

    /*
     * 显示距离
     */
    public double visibleDistance;

    /*
     * 是否显示名字
     */
    public boolean showName;

    /*
     * 是否显示导航图标
     */
    public boolean showNavigator;

    /*
     * 是否显示实体图标
     */
    public boolean showWorldMarker;

    /*
     * 任务状态
     */
    public String state;

    /*
     * 扩展数据
     */
    public String extraData;

    public QuestMarker(
            double x,
            double y,
            double z,
            String name
    ) {

        this(
                UUID.randomUUID(),
                x,
                y,
                z,
                name
        );
    }


    private QuestMarker(
            UUID id,
            double x,
            double y,
            double z,
            String name
    ) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "QuestMarker id 不能为 null"
            );
        }

        if (name == null) {
            throw new IllegalArgumentException(
                    "QuestMarker name 不能为 null"
            );
        }

        this.id = id;

        this.x = x;
        this.y = y;
        this.z = z;

        this.name = name;

        /*
         * 默认值
         */
        this.description = "";

        this.active = true;

        this.iconType = "DEFAULT";

        this.iconName = "";

        this.visibleDistance = 256.0D;

        this.showName = true;

        this.showNavigator = true;

        this.showWorldMarker = true;

        this.state = "ACTIVE";

        this.extraData = "";
    }


    /*
     * 获取标点 UUID
     */
    public UUID getId() {
        return id;
    }


    /*
     * 网络包读取
     */
    public QuestMarker(FriendlyByteBuf buf) {

        this.id = buf.readUUID();

        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();

        name = buf.readUtf();

        description = buf.readUtf();

        active = buf.readBoolean();

        iconType = buf.readUtf();

        iconName = buf.readUtf();

        visibleDistance = buf.readDouble();

        showName = buf.readBoolean();

        showNavigator = buf.readBoolean();

        showWorldMarker = buf.readBoolean();

        state = buf.readUtf();

        extraData = buf.readUtf();

        /*
         * tracked 不参与网络同步
         */
        tracked = false;
    }

    /*
     * 网络包写入
     */
    public void encode(FriendlyByteBuf buf) {

        buf.writeUUID(id);

        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);

        buf.writeUtf(name);

        buf.writeUtf(description);

        buf.writeBoolean(active);

        buf.writeUtf(iconType);

        buf.writeUtf(iconName);

        buf.writeDouble(visibleDistance);

        buf.writeBoolean(showName);

        buf.writeBoolean(showNavigator);

        buf.writeBoolean(showWorldMarker);

        buf.writeUtf(state);

        buf.writeUtf(extraData);
    }

    /*
     * NBT保存
     */
    public CompoundTag save(CompoundTag tag) {

        /*
         * UUID
         */
        tag.putUUID(
                "id",
                id
        );

        /*
         * 坐标
         */
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);

        /*
         * 基础信息
         */
        tag.putString(
                "name",
                name
        );

        tag.putString(
                "description",
                description
        );

        /*
         * 启用状态
         */
        tag.putBoolean(
                "active",
                active
        );

        /*
         * 图标
         */
        tag.putString(
                "iconType",
                iconType
        );

        tag.putString(
                "iconName",
                iconName
        );

        /*
         * 显示设置
         */
        tag.putDouble(
                "visibleDistance",
                visibleDistance
        );

        tag.putBoolean(
                "showName",
                showName
        );

        tag.putBoolean(
                "showNavigator",
                showNavigator
        );

        tag.putBoolean(
                "showWorldMarker",
                showWorldMarker
        );

        /*
         * 旧版 tracked
         */
        tag.putBoolean(
                "tracked",
                tracked
        );

        /*
         * 任务状态
         */
        tag.putString(
                "state",
                state
        );

        /*
         * 扩展数据
         */
        tag.putString(
                "extraData",
                extraData
        );

        return tag;
    }

    /*
     * NBT读取
     */
    public static QuestMarker load(
            CompoundTag tag
    ) {

        UUID id =
                tag.hasUUID("id")
                        ? tag.getUUID("id")
                        : UUID.randomUUID();

        QuestMarker marker =
                new QuestMarker(
                        id,
                        tag.getDouble("x"),
                        tag.getDouble("y"),
                        tag.getDouble("z"),
                        tag.getString("name")
                );

        /*
         * 基础信息
         */
        marker.description =
                tag.getString("description");

        /*
         * active
         */
        marker.active =
                tag.contains("active")
                        ? tag.getBoolean("active")
                        : true;

        /*
         * 图标
         */
        marker.iconType =
                tag.contains("iconType")
                        ? tag.getString("iconType")
                        : "DEFAULT";

        marker.iconName =
                tag.contains("iconName")
                        ? tag.getString("iconName")
                        : "";

        /*
         * 显示距离
         */
        marker.visibleDistance =
                tag.contains("visibleDistance")
                        ? tag.getDouble("visibleDistance")
                        : 256.0D;

        /*
         * 显示名字
         */
        marker.showName =
                !tag.contains("showName")
                        || tag.getBoolean("showName");

        /*
         * 导航图标
         */
        marker.showNavigator =
                !tag.contains("showNavigator")
                        || tag.getBoolean("showNavigator");

        /*
         * 世界实体图标
         */
        marker.showWorldMarker =
                !tag.contains("showWorldMarker")
                        || tag.getBoolean("showWorldMarker");

        /*
         * tracked
         */
        marker.tracked =
                tag.contains("tracked")
                        && tag.getBoolean("tracked");

        /*
         * 状态
         */
        marker.state =
                tag.contains("state")
                        ? tag.getString("state")
                        : "ACTIVE";

        /*
         * 扩展数据
         */
        marker.extraData =
                tag.getString("extraData");

        return marker;
    }


    @Override
    public String toString() {

        return "QuestMarker{"
                + "id=" + id
                + ", x=" + x
                + ", y=" + y
                + ", z=" + z
                + ", name='" + name + '\''
                + ", active=" + active
                + ", state='" + state + '\''
                + '}';
    }
}