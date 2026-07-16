package io.github.FinNank1ng.better_coordinate_navigator.client;


import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class IconManager {


    private static final Logger LOGGER =
            LogUtils.getLogger();



    /**
     * 图标缓存
     */
    private static final Map<String, ResourceLocation> ICON_CACHE =
            new HashMap<>();


    /**
     * 防止重复警告
     */
    private static final Map<String, Boolean> WARN_CACHE =
            new HashMap<>();


    /**
     * 动态纹理保存
     * 防止GC回收
     */
    private static final Map<String, DynamicTexture> TEXTURE_CACHE =
            new HashMap<>();



    /**
     * 获取玩家自定义图片目录
     */
    private static File getIconFolder(){

        return new File(
                Minecraft.getInstance()
                        .gameDirectory,

                "better_coordinate_navigator/picture"
        );

    }



    /**
     * 初始化
     */
    public static void init(){

        File folder =
                getIconFolder();


        if(!folder.exists()){

            boolean result =
                    folder.mkdirs();


            if(result){

                LOGGER.info(
                        "[BCN] Created icon folder: {}",
                        folder.getAbsolutePath()
                );

            }

        }

    }



    /**
     * 获取图标
     *
     * @param name 图片文件名
     */
    public static ResourceLocation getIcon(
            String name
    ){


        if(name == null || name.isEmpty()){

            return null;

        }



        /*
         * 已经加载
         */
        if(ICON_CACHE.containsKey(name)){

            return ICON_CACHE.get(name);

        }



        File folder =
                getIconFolder();



        if(!folder.exists()){

            folder.mkdirs();

        }



        File file =
                new File(
                        folder,
                        name
                );



        /*
         * 文件不存在
         */
        if(!file.exists()){


            if(!WARN_CACHE.containsKey(name)){


                LOGGER.warn(
                        "[BCN] Missing custom icon: {}",
                        name
                );


                WARN_CACHE.put(
                        name,
                        true
                );

            }


            return null;

        }



        try(
                InputStream input =
                        new FileInputStream(file)
        ){

            /*
             * PNG读取
             */
            NativeImage image =
                    NativeImage.read(input);



            DynamicTexture texture =
                    new DynamicTexture(
                            image
                    );



            ResourceLocation location =
                    Minecraft.getInstance()
                            .getTextureManager()
                            .register(
                                    "bcn_picture/" + name,
                                    texture
                            );



            /*
             * 保存缓存
             */
            ICON_CACHE.put(
                    name,
                    location
            );


            TEXTURE_CACHE.put(
                    name,
                    texture
            );



            LOGGER.info(
                    "[BCN] Loaded custom icon: {}",
                    name
            );


            return location;


        }
        catch(IOException e){


            LOGGER.error(
                    "[BCN] Failed loading icon: {}",
                    name,
                    e
            );


        }


        return null;

    }




    /**
     * 绘制图标
     */
    public static void drawIcon(
            GuiGraphics g,
            ResourceLocation texture,
            int x,
            int y
    ){


        g.blit(
                texture,

                x - 8,
                y - 8,

                0,
                0,

                16,
                16,

                16,
                16
        );

    }




    /**
     * 重载资源
     */
    public static void reload(){

        ICON_CACHE.clear();

        WARN_CACHE.clear();

        TEXTURE_CACHE.clear();


        LOGGER.info(
                "[BCN] Icon cache cleared"
        );

    }

}