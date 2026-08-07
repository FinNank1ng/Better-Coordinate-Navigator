package io.github.FinNank1ng.better_coordinate_navigator.util;

import net.minecraftforge.fml.ModList;

public class ModVersion {


    private static final String MOD_ID = "better_coordinate_navigator";

    private static final String mod_name = "Better coordinate navigator";

    /**
     * 获取当前 Mod 版本
     */
    public static String getVersion() {

        return ModList.get()
                .getModContainerById(MOD_ID)
                .orElseThrow()
                .getModInfo()
                .getVersion()
                .toString();

    }


    /**
     * 固定信息
     */
    public static String getAuthors(){

        return "github@FinNank1ng(ShirohaRen) / Bilibili - 星丶白羽莲";

    }


}