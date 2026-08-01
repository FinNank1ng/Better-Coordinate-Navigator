package io.github.FinNank1ng.better_coordinate_navigator.client;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;



public class ConfigLoader {


    /**
     * 当前配置版本
     */
    private static final int CURRENT_VERSION = 1;



    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();



    public static ClientConfig CONFIG;



    private static File getConfigFile(){

        return new File(
                Minecraft.getInstance()
                        .gameDirectory,

                "better_coordinate_navigator/config/config.json"
        );

    }



    public static void init(){

        File file = getConfigFile();

        File folder = file.getParentFile();


        if(!folder.exists()){

            folder.mkdirs();

        }



        if(!file.exists()){

            CONFIG = new ClientConfig();

            save();

        }
        else{

            load();

        }

    }




    /**
     * 保存配置
     */
    public static void save(){


        try(
                FileWriter writer = new FileWriter(getConfigFile())
        ){


            GSON.toJson(
                    CONFIG,
                    writer
            );


        }
        catch(IOException e){

            e.printStackTrace();

        }

    }




    /**
     * 读取配置
     */
    public static void load(){


        try(
                FileReader reader = new FileReader(getConfigFile())
        ){


            CONFIG = GSON.fromJson(
                            reader,
                            ClientConfig.class
                    );



            /*
             * 配置为空
             */
            if(CONFIG == null){

                CONFIG = new ClientConfig();

            }



            /*
             * 配置迁移
             */
            migrate();



            /*
             * 配置检查
             */
            CONFIG.validate();



            /*
             * 保存修正后的配置
             */
            save();



        }
        catch(IOException e){


            CONFIG =
                    new ClientConfig();


            save();


        }

    }




    /**
     * 配置版本迁移
     */
    private static void migrate(){


        /*
         * 防止旧配置没有版本字段
         */
        if(CONFIG.configVersion <= 0){

            CONFIG.configVersion = 1;

        }



        /*
         * 当前版本
         */
        if(CONFIG.configVersion < CURRENT_VERSION){

            CONFIG.configVersion =
                    CURRENT_VERSION;


        }

    }

}