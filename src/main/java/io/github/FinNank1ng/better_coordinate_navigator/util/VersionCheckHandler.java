package io.github.FinNank1ng.better_coordinate_navigator.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

import static com.mojang.text2speech.Narrator.LOGGER;

public final class VersionCheckHandler {

    private VersionCheckHandler() {
    }


    /**
     * 检查 Mod 是否有新版本
     *
     * 只在客户端进入游戏时调用一次。
     */
    public static void checkForUpdate() {

        LOGGER.info("[BCN] Starting version check...");


        CompletableFuture.runAsync(() -> {

            try {

                String current =
                        ModVersion.getVersion();

                String latest =
                        VersionChecker.getLatestVersion();


                LOGGER.info(
                        "[BCN] Current version: {}",
                        current
                );

                LOGGER.info(
                        "[BCN] Latest version: {}",
                        latest
                );


                if (latest == null) {

                    LOGGER.warn(
                            "[BCN] Unable to determine latest version."
                    );


                    /*
                     * GitHub 无法访问
                     */
                    Minecraft.getInstance().execute(() -> {

                        Minecraft mc =
                                Minecraft.getInstance();

                        if (mc.player == null) {
                            return;
                        }


                        mc.player.sendSystemMessage(
                                Component.literal(
                                        "§6[Better Coordinate Navigator]"
                                )
                        );

                        mc.player.sendSystemMessage(
                                Component.literal(
                                        "§c版本检查失败"
                                )
                        );

                        mc.player.sendSystemMessage(
                                Component.literal(
                                        "§7当前版本: §f"
                                                + current
                                )
                        );

                    });

                    return;
                }


                int result =
                        VersionChecker.compareVersions(
                                latest,
                                current
                        );


                /*
                 * 回到 Minecraft 客户端线程
                 */
                Minecraft.getInstance().execute(() -> {

                    Minecraft mc =
                            Minecraft.getInstance();

                    if (mc.player == null) {
                        return;
                    }


                    /*
                     * 有新版本
                     */
                    if (result > 0) {

                        LOGGER.info(
                                "[BCN] New version available: {} -> {}",
                                current,
                                latest
                        );


                        mc.player.sendSystemMessage(
                                Component.literal(
                                        "§6§l[Better Coordinate Navigator]"
                                )
                        );

                        mc.player.sendSystemMessage(
                                Component.literal(
                                        "§e发现新版本！"
                                )
                        );

                        mc.player.sendSystemMessage(
                                Component.literal(
                                        "§7当前版本: §f"
                                                + current
                                )
                        );

                        mc.player.sendSystemMessage(
                                Component.literal(
                                        "§7最新版本: §a"
                                                + latest
                                )
                        );

                        mc.player.sendSystemMessage(
                                Component.literal(
                                        "§7建议前往 GitHub Release 获取最新版本。"
                                )
                        );

                    }


                    /*
                     * 当前已经是最新版本
                     */
                    else {

                        LOGGER.info(
                                "[BCN] You are using the latest version."
                        );


                        mc.player.sendSystemMessage(
                                Component.literal(
                                        "§6§l[Better Coordinate Navigator]"
                                )
                        );

                        mc.player.sendSystemMessage(
                                Component.literal(
                                        "§a当前已是最新版本"
                                )
                        );

                        mc.player.sendSystemMessage(
                                Component.literal(
                                        "§7当前版本: §f"
                                                + current
                                )
                        );

                        mc.player.sendSystemMessage(
                                Component.literal(
                                        "§7最新版本: §f"
                                                + latest
                                )
                        );

                    }

                });


            } catch (Exception e) {

                LOGGER.warn(
                        "[BCN] Version check failed.",
                        e
                );

            }

        });

    }

}