package io.github.FinNank1ng.better_coordinate_navigator.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class QuestMarker {

    /*
     * 基础信息
     */
    public double x;
    public double y;
    public double z;

    public String name;

    public String description;

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
    public String iconTexture;

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

    public String extraData;

    public QuestMarker(
            double x,
            double y,
            double z,
            String name
    ) {

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

        this.iconTexture = "";

        this.visibleDistance = 256.0D;

        this.showName = true;

        this.showNavigator = true;

        this.showWorldMarker = true;

        this.state = "ACTIVE";

        this.extraData = "";
    }

    /*
     * 网络包读取
     */
    public QuestMarker(FriendlyByteBuf buf) {

        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();

        name = buf.readUtf();

        description = buf.readUtf();

        active = buf.readBoolean();

        iconType = buf.readUtf();

        iconTexture = buf.readUtf();

        visibleDistance = buf.readDouble();

        showName = buf.readBoolean();

        showNavigator = buf.readBoolean();

        showWorldMarker = buf.readBoolean();

        tracked = buf.readBoolean();

        state = buf.readUtf();

        extraData = buf.readUtf();
    }

    /*
     * 网络包写入
     */
    public void encode(FriendlyByteBuf buf) {

        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);

        buf.writeUtf(name);

        buf.writeUtf(description);

        buf.writeBoolean(active);

        buf.writeUtf(iconType);

        buf.writeUtf(iconTexture);

        buf.writeDouble(visibleDistance);

        buf.writeBoolean(showName);

        buf.writeBoolean(showNavigator);

        buf.writeBoolean(showWorldMarker);

        buf.writeBoolean(tracked);

        buf.writeUtf(state);

        buf.writeUtf(extraData);
    }

    /*
     * NBT保存
     */
    public CompoundTag save(CompoundTag tag) {

        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);

        tag.putString("name", name);

        tag.putString("description", description);

        tag.putBoolean("active", active);

        tag.putString("iconType", iconType);

        tag.putString("iconTexture", iconTexture);

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

        tag.putString(
                "state",
                state
        );

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

        QuestMarker marker =
                new QuestMarker(
                        tag.getDouble("x"),
                        tag.getDouble("y"),
                        tag.getDouble("z"),
                        tag.getString("name")
                );

        marker.description =
                tag.getString("description");

        marker.active =
                tag.getBoolean("active");

        marker.iconType =
                tag.getString("iconType");

        marker.iconTexture =
                tag.getString("iconTexture");

        marker.visibleDistance =
                tag.contains("visibleDistance")
                        ? tag.getDouble(
                        "visibleDistance"
                )
                        : 256.0D;

        marker.showName =
                !tag.contains("showName")
                        || tag.getBoolean(
                        "showName"
                );

        marker.showNavigator =
                !tag.contains("showNavigator")
                        || tag.getBoolean(
                        "showNavigator"
                );

        marker.showWorldMarker =
                !tag.contains("showWorldMarker")
                        || tag.getBoolean(
                        "showWorldMarker"
                );

        marker.state =
                tag.contains("state")
                        ? tag.getString("state")
                        : "ACTIVE";

        marker.extraData =
                tag.getString("extraData");

        return marker;
    }

}