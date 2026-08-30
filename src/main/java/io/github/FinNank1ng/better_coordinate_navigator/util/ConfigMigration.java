package io.github.FinNank1ng.better_coordinate_navigator.util;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public final class ConfigMigration {

    /*
     * 旧版本目录 来源于 v0.3.0的正式版
     */

    private static final Path OLD_DIR =
            FMLPaths.GAMEDIR.get()
                    .resolve("better_coordinate_navigator");

    private static final Path OLD_CONFIG_DIR =
            OLD_DIR.resolve("config");

    private static final Path OLD_CONFIG =
            OLD_CONFIG_DIR.resolve("config.json");

    private static final Path OLD_PICTURE =
            OLD_DIR.resolve("Picture");


    /*
     * 新版本目录 版本 > v0.3.0
     */

    private static final Path NEW_DIR =
            FMLPaths.CONFIGDIR.get()
                    .resolve("better_coordinate_navigator");

    private static final Path NEW_CONFIG_DIR =
            NEW_DIR.resolve("config");

    private static final Path NEW_CONFIG =
            NEW_CONFIG_DIR.resolve("config.json");

    private static final Path NEW_PICTURE =
            NEW_DIR.resolve("Picture");


    private ConfigMigration() {
    }


    /**
     * 检查并迁移旧版本 BCN 数据
     */
    public static void migrate() {

        /*
         * 判断旧版本目录是否存在
         */
        if (!Files.exists(OLD_DIR)) {

            LOGGER.debug(
                    "[BCN] No legacy BCN directory found."
            );

            return;
        }


        /*
         * 判断旧配置和旧图片是否存在
         */
        boolean hasOldConfig =
                Files.exists(OLD_CONFIG);

        boolean hasOldPicture =
                Files.exists(OLD_PICTURE);


        /*
         * 什么都没有，不需要迁移
         */
        if (!hasOldConfig && !hasOldPicture) {

            LOGGER.debug(
                    "[BCN] No legacy BCN data found."
            );

            return;
        }


        LOGGER.info(
                "[BCN] Legacy BCN data detected."
        );


        try {

            /*
             * 创建新的 BCN 根目录
             */
            Files.createDirectories(
                    NEW_DIR
            );


            /*
             * 迁移 config.json
             */

            if (hasOldConfig) {

                Files.createDirectories(
                        NEW_CONFIG_DIR
                );


                /*
                 * 新配置不存在，迁移旧配置
                 */
                if (!Files.exists(NEW_CONFIG)) {

                    LOGGER.info(
                            "[BCN] Migrating config: {} -> {}",
                            OLD_CONFIG,
                            NEW_CONFIG
                    );


                    Files.move(
                            OLD_CONFIG,
                            NEW_CONFIG
                    );


                    LOGGER.info(
                            "[BCN] Config migration completed."
                    );

                } else {

                    /*
                     * 新配置已经存在，不覆盖新配置
                     */
                    LOGGER.info(
                            "[BCN] New config already exists, keeping it: {}",
                            NEW_CONFIG
                    );
                }
            }


            /*
             * 迁移 Picture
             */

            if (hasOldPicture) {

                LOGGER.info(
                        "[BCN] Migrating Picture directory: {} -> {}",
                        OLD_PICTURE,
                        NEW_PICTURE
                );


                /*
                 * Picture 是否存在，统一逐个迁移里面的内容
                 */
                migrateDirectoryContents(
                        OLD_PICTURE,
                        NEW_PICTURE
                );
            }


            /*
             * 清理旧目录
             */

            deleteIfEmpty(
                    OLD_CONFIG_DIR
            );


            deleteIfEmpty(
                    OLD_DIR
            );


            LOGGER.info(
                    "[BCN] Legacy BCN data migration completed."
            );


        } catch (IOException e) {

            LOGGER.error(
                    "[BCN] Failed to migrate legacy BCN data.",
                    e
            );
        }
    }


    /**
     * 递归迁移目录内容
     */
    private static void migrateDirectoryContents(
            Path source,
            Path target
    ) throws IOException {

        /*
         * 创建目标目录
         */
        Files.createDirectories(
                target
        );


        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(source)) {

            for (Path file : stream) {

                Path targetFile =
                        target.resolve(
                                file.getFileName()
                        );


                /*
                 * 子目录
                 */
                if (Files.isDirectory(file)) {

                    migrateDirectoryContents(
                            file,
                            targetFile
                    );

                } else {

                    /*
                     * 目标文件不存在
                     */
                    if (!Files.exists(targetFile)) {

                        LOGGER.info(
                                "[BCN] Migrating file: {} -> {}",
                                file,
                                targetFile
                        );


                        Files.move(
                                file,
                                targetFile
                        );

                    } else {

                        /*
                         * 目标已经存在，保留目标文件
                         */
                        LOGGER.debug(
                                "[BCN] File already exists, keeping: {}",
                                targetFile
                        );
                    }
                }
            }
        }


        /*
         * 目录为空以后删除
         */
        deleteIfEmpty(
                source
        );
    }


    /**
     * 如果目录为空，则删除
     */
    private static void deleteIfEmpty(
            Path directory
    ) throws IOException {

        if (!Files.exists(directory)) {
            return;
        }


        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(directory)) {

            if (!stream.iterator().hasNext()) {

                Files.delete(
                        directory
                );
            }
        }
    }
}