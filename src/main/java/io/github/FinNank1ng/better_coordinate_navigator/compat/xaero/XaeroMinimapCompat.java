
package io.github.FinNank1ng.better_coordinate_navigator.compat.xaero;

import io.github.FinNank1ng.better_coordinate_navigator.client.ConfigLoader;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import net.minecraft.resources.ResourceLocation;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointSet;
import xaero.common.minimap.waypoints.WaypointWorld;
import xaero.common.minimap.waypoints.WaypointsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class XaeroMinimapCompat {

    /**
     * BCN 旧版第三方来源标识
     */
    private static final ResourceLocation BCN_ORIGIN =
            new ResourceLocation(
                    "better_coordinate_navigator",
                    "marker"
            );

    /**
     * BCN 新版来源标识前缀
     */
    private static final String BCN_ORIGIN_PATH_PREFIX = "marker/";

    private static final int DEFAULT_COLOR = 6;

    private XaeroMinimapCompat() {
    }

    /**
     * 获取指定 Marker 的独立 Xaero 来源标识
     */
    private static ResourceLocation getMarkerOrigin(UUID markerId) {
        if (markerId == null) {
            return null;
        }

        return new ResourceLocation(
                "better_coordinate_navigator",
                BCN_ORIGIN_PATH_PREFIX + markerId
        );
    }

    /**
     * 从 Xaero 来源标识中解析 Marker UUID
     */
    private static UUID getMarkerId(Waypoint waypoint) {
        if (!isBcnWaypoint(waypoint)) {
            return null;
        }

        ResourceLocation origin = waypoint.getThirdPartyOrigin();

        if (origin == null) {
            return null;
        }

        String path = origin.getPath();

        if (!path.startsWith(BCN_ORIGIN_PATH_PREFIX)) {
            return null;
        }

        String uuidText = path.substring(BCN_ORIGIN_PATH_PREFIX.length());

        try {
            return UUID.fromString(uuidText);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * 同步单个 BCN Marker
     */
    public static void syncMarker(QuestMarker marker) {

        if (marker == null || marker.getId() == null) {
            return;
        }

        if (ConfigLoader.CONFIG != null
                && !ConfigLoader.CONFIG.Xaero.enableWaypointSync) {
            return;
        }

        if (!XaeroCompat.isMinimapLoaded()) {
            return;
        }

        WaypointSet set = getCurrentWaypointSet();

        if (set == null) {
            return;
        }

        UUID markerId = marker.getId();

        Waypoint waypoint = findMarker(set, markerId);

        // 兼容迁移旧版只使用名称标识的 BCN 航点
        if (waypoint == null) {
            waypoint = findLegacyMarker(set, marker.name);
        }

        if (waypoint == null) {

            waypoint = createWaypoint(marker);

            set.add(waypoint);

            System.out.println(
                    "[BCN] Added Xaero waypoint: " + marker.name
            );

        } else {

            updateWaypoint(waypoint, marker);

            System.out.println(
                    "[BCN] Updated Xaero waypoint: " + marker.name
            );
        }

        updateWaypoints();
    }

    /**
     * 获取动态航点图标
     */
    private static String getWaypointSymbol(QuestMarker marker) {

        if (marker.name == null || marker.name.isEmpty()) {
            return "★";
        }

        return marker.name.substring(0, 1);
    }

    /**
     * 创建 BCN Xaero waypoint
     */
    private static Waypoint createWaypoint(QuestMarker marker) {

        Waypoint waypoint = new Waypoint(
                (int) Math.round(marker.x),
                (int) Math.round(marker.y),
                (int) Math.round(marker.z),
                marker.name,
                getWaypointSymbol(marker),
                DEFAULT_COLOR
        );

        waypoint.setThirdPartyOrigin(
                getMarkerOrigin(marker.getId())
        );

        waypoint.setYIncluded(true);

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

        waypoint.setName(marker.name);

        waypoint.setSymbol(
                getWaypointSymbol(marker)
        );

        waypoint.setYIncluded(true);

        waypoint.setThirdPartyOrigin(
                getMarkerOrigin(marker.getId())
        );

        waypoint.setThirdPartyDeleted(false);
    }

    /**
     * 根据 UUID 查找 BCN Marker
     */
    private static Waypoint findMarker(
            WaypointSet set,
            UUID markerId
    ) {

        if (markerId == null) {
            return null;
        }

        ResourceLocation expectedOrigin = getMarkerOrigin(markerId);

        for (Waypoint waypoint : set.getList()) {

            if (!isBcnWaypoint(waypoint)) {
                continue;
            }

            if (expectedOrigin.equals(
                    waypoint.getThirdPartyOrigin()
            )) {
                return waypoint;
            }
        }

        return null;
    }

    /**
     * 查找旧版 BCN 航点
     *
     * 只有名称唯一时才允许迁移，避免误认领同名航点
     */
    private static Waypoint findLegacyMarker(
            WaypointSet set,
            String markerName
    ) {

        if (markerName == null) {
            return null;
        }

        Waypoint result = null;

        for (Waypoint waypoint : set.getList()) {

            if (!BCN_ORIGIN.equals(
                    waypoint.getThirdPartyOrigin()
            )) {
                continue;
            }

            if (!markerName.equals(waypoint.getName())) {
                continue;
            }

            if (result != null) {
                return null;
            }

            result = waypoint;
        }

        return result;
    }

    /**
     * 删除指定 UUID 的 BCN waypoint
     */
    public static void removeMarker(UUID markerId) {

        if (markerId == null) {
            return;
        }

        if (ConfigLoader.CONFIG != null
                && !ConfigLoader.CONFIG.Xaero.enableWaypointSync) {
            return;
        }

        if (!XaeroCompat.isMinimapLoaded()) {
            return;
        }

        WaypointSet set = getCurrentWaypointSet();

        if (set == null) {
            return;
        }

        ResourceLocation expectedOrigin = getMarkerOrigin(markerId);

        List<Waypoint> removeList = new ArrayList<>();

        for (Waypoint waypoint : set.getList()) {

            if (!isBcnWaypoint(waypoint)) {
                continue;
            }

            if (!expectedOrigin.equals(
                    waypoint.getThirdPartyOrigin()
            )) {
                continue;
            }

            removeList.add(waypoint);
        }

        if (removeList.isEmpty()) {
            return;
        }

        set.removeAll(removeList);

        updateWaypoints();

        System.out.println(
                "[BCN] Removed Xaero waypoint: " + markerId
        );
    }

    /**
     * 兼容旧版按名称删除的调用
     */
    @Deprecated
    public static void removeMarker(String markerName) {

        if (markerName == null) {
            return;
        }

        if (ConfigLoader.CONFIG != null
                && !ConfigLoader.CONFIG.Xaero.enableWaypointSync) {
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

            if (!markerName.equals(waypoint.getName())) {
                continue;
            }

            removeList.add(waypoint);
        }

        if (removeList.isEmpty()) {
            return;
        }

        set.removeAll(removeList);

        updateWaypoints();

        System.out.println(
                "[BCN] Removed Xaero waypoint: " + markerName
        );
    }

    /**
     * 删除全部 BCN waypoint
     *
     * 不会删除玩家自己创建的 waypoint
     */
    public static void clearAll() {

        if (ConfigLoader.CONFIG != null
                && !ConfigLoader.CONFIG.Xaero.enableWaypointSync) {
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

            if (isBcnWaypoint(waypoint)) {
                removeList.add(waypoint);
            }
        }

        if (removeList.isEmpty()) {
            return;
        }

        set.removeAll(removeList);

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
    private static boolean isBcnWaypoint(Waypoint waypoint) {

        if (waypoint == null || !waypoint.isThirdParty()) {
            return false;
        }

        ResourceLocation origin = waypoint.getThirdPartyOrigin();

        if (origin == null) {
            return false;
        }

        if (!"better_coordinate_navigator".equals(
                origin.getNamespace()
        )) {
            return false;
        }

        String path = origin.getPath();

        return BCN_ORIGIN.getPath().equals(path)
                || path.startsWith(BCN_ORIGIN_PATH_PREFIX);
    }

    /**
     * 获取当前 Xaero waypoint set
     */
    private static WaypointSet getCurrentWaypointSet() {

        XaeroMinimapSession session =
                XaeroMinimapSession.getCurrentSession();

        if (session == null) {
            return null;
        }

        WaypointsManager manager =
                session.getWaypointsManager();

        if (manager == null) {
            return null;
        }

        WaypointWorld world =
                manager.getCurrentWorld();

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
