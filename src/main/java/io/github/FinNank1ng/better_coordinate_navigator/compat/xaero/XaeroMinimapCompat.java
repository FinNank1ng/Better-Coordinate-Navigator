package io.github.FinNank1ng.better_coordinate_navigator.compat.xaero;

import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import net.minecraft.resources.ResourceLocation;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointSet;
import xaero.common.minimap.waypoints.WaypointWorld;
import xaero.common.minimap.waypoints.WaypointsManager;

import java.util.ArrayList;
import java.util.List;

public final class XaeroMinimapCompat {

    /**
     * BCN 在 Xaero 中的第三方来源标识
     */
    private static final ResourceLocation BCN_ORIGIN =
            new ResourceLocation(
                    "better_coordinate_navigator",
                    "marker"
            );

    private static final int DEFAULT_COLOR = 0xFFFFFF;
    private static final String DEFAULT_SYMBOL = "B";

    private XaeroMinimapCompat() {
    }

    /**
     * 同步单个 BCN Marker
     */
    public static void syncMarker(QuestMarker marker) {

        if (marker == null) {
            return;
        }

        if (!XaeroCompat.isMinimapLoaded()) {
            return;
        }

        WaypointSet set = getCurrentWaypointSet();

        if (set == null) {
            return;
        }

        Waypoint waypoint =
                findMarker(
                        set,
                        marker.name
                );

        if (waypoint == null) {

            waypoint =
                    createWaypoint(
                            marker
                    );

            set.add(waypoint);

            System.out.println(
                    "[BCN] Added Xaero waypoint: "
                            + marker.name
            );

        } else {

            updateWaypoint(
                    waypoint,
                    marker
            );

            System.out.println(
                    "[BCN] Updated Xaero waypoint: "
                            + marker.name
            );
        }

        updateWaypoints();
    }

    /**
     * 创建 BCN → Xaero waypoint
     */
    private static Waypoint createWaypoint(
            QuestMarker marker
    ) {

        Waypoint waypoint =
                new Waypoint(
                        (int) Math.round(marker.x),
                        (int) Math.round(marker.y),
                        (int) Math.round(marker.z),
                        marker.name,
                        DEFAULT_SYMBOL,
                        DEFAULT_COLOR
                );

        /*
         * 标记为 BCN 第三方 waypoint
         */
        waypoint.setThirdPartyOrigin(
                BCN_ORIGIN
        );

        waypoint.setYIncluded(
                true
        );

        waypoint.setPurpose(
                xaero.hud.minimap.waypoint.WaypointPurpose.NORMAL
        );

        return waypoint;
    }

    /**
     * 更新已有 waypoint
     */
    private static void updateWaypoint(
            Waypoint waypoint,
            QuestMarker marker
    ) {

        waypoint.setX(
                (int) Math.round(marker.x)
        );

        waypoint.setY(
                (int) Math.round(marker.y)
        );

        waypoint.setZ(
                (int) Math.round(marker.z)
        );

        waypoint.setName(
                marker.name
        );

        waypoint.setYIncluded(
                true
        );

        waypoint.setThirdPartyDeleted(
                false
        );
    }

    /**
     * 在当前 waypoint 集合中寻找 BCN Marker
     */
    private static Waypoint findMarker(
            WaypointSet set,
            String markerName
    ) {

        if (markerName == null) {
            return null;
        }

        for (Waypoint waypoint : set.getList()) {

            if (!waypoint.isThirdParty()) {
                continue;
            }

            if (!BCN_ORIGIN.equals(
                    waypoint.getThirdPartyOrigin()
            )) {
                continue;
            }

            if (!markerName.equals(
                    waypoint.getName()
            )) {
                continue;
            }

            return waypoint;
        }

        return null;
    }

    /**
     * 删除单个 BCN waypoint
     */
    public static void removeMarker(
            String markerName
    ) {

        if (markerName == null) {
            return;
        }

        if (!XaeroCompat.isMinimapLoaded()) {
            return;
        }

        WaypointSet set = getCurrentWaypointSet();

        if (set == null) {
            return;
        }

        List<Waypoint> removeList = new ArrayList<>();

        for (Waypoint waypoint : set.getList()) {

            if (!isBcnWaypoint(waypoint)) {
                continue;
            }

            if (!markerName.equals(
                    waypoint.getName()
            )) {
                continue;
            }

            removeList.add(
                    waypoint
            );
        }

        if (removeList.isEmpty()) {
            return;
        }

        set.removeAll(
                removeList
        );

        updateWaypoints();

        System.out.println(
                "[BCN] Removed Xaero waypoint: "
                        + markerName
        );
    }

    /**
     * 删除全部 BCN waypoint
     *
     * 不会碰玩家自己创建的 waypoint
     */
    public static void clearAll() {

        if (!XaeroCompat.isMinimapLoaded()) {
            return;
        }

        WaypointSet set = getCurrentWaypointSet();

        if (set == null) {
            return;
        }

        List<Waypoint> removeList = new ArrayList<>();

        for (Waypoint waypoint : set.getList()) {

            if (isBcnWaypoint(
                    waypoint
            )) {
                removeList.add(
                        waypoint
                );
            }
        }

        if (removeList.isEmpty()) {
            return;
        }

        set.removeAll(
                removeList
        );

        updateWaypoints();

        System.out.println(
                "[BCN] Cleared "
                        + removeList.size()
                        + " BCN Xaero waypoints."
        );
    }

    /**
     * 判断 waypoint 是否由 BCN 创建
     */
    private static boolean isBcnWaypoint(
            Waypoint waypoint
    ) {

        if (waypoint == null) {
            return false;
        }

        if (!waypoint.isThirdParty()) {
            return false;
        }

        return BCN_ORIGIN.equals(
                waypoint.getThirdPartyOrigin()
        );
    }

    /**
     * 获取当前 Xaero waypoint set
     */
    private static WaypointSet getCurrentWaypointSet() {

        XaeroMinimapSession session = XaeroMinimapSession.getCurrentSession();

        if (session == null) {
            return null;
        }

        WaypointsManager manager = session.getWaypointsManager();

        if (manager == null) {
            return null;
        }

        WaypointWorld world = manager.getCurrentWorld();

        if (world == null) {
            return null;
        }

        return world.getCurrentSet();
    }

    /**
     * 通知 Xaero waypoint 数据发生变化
     */
    private static void updateWaypoints() {

        XaeroMinimapSession session =
                XaeroMinimapSession.getCurrentSession();

        if (session == null) {
            return;
        }

        WaypointsManager manager =
                session.getWaypointsManager();

        if (manager == null) {
            return;
        }

        manager.updateWaypoints();
    }
}