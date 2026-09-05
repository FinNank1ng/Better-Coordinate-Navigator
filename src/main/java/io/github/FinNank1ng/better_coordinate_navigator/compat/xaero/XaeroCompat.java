package io.github.FinNank1ng.better_coordinate_navigator.compat.xaero;

import net.minecraftforge.fml.ModList;

public final class XaeroCompat {

    private static final String XAERO_WORLD_MAP = "xaeroworldmap";
    private static final String XAERO_MINIMAP = "xaerominimap";

    private XaeroCompat() {
    }

    /**
     * 是否安装 Xaero World Map
     */
    public static boolean isWorldMapLoaded() {
        return ModList.get().isLoaded(XAERO_WORLD_MAP);
    }

    /**
     * 是否安装 Xaero Minimap
     */
    public static boolean isMinimapLoaded() {
        return ModList.get().isLoaded(XAERO_MINIMAP);
    }

    /**
     * 是否安装任意 Xaero 地图模组
     */
    public static boolean isAnyXaeroLoaded() {
        return isWorldMapLoaded() || isMinimapLoaded();
    }
}