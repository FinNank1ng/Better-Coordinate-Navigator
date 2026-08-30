package io.github.FinNank1ng.better_coordinate_navigator.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.mojang.text2speech.Narrator.LOGGER;

public final class VersionChecker {

    private static final String API_URL =
            "https://api.github.com/repos/FinNank1ng/Better-Coordinate-Navigator/releases/latest";

    private static final HttpClient CLIENT =
            HttpClient.newBuilder()
                    .build();


    private VersionChecker() {
    }


    /**
     * 获取 GitHub 最新正式 Release 版本
     */
    public static String getLatestVersion() {

        try {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(API_URL))
                            .header(
                                    "Accept",
                                    "application/vnd.github+json"
                            )
                            .header(
                                    "User-Agent",
                                    "Better-Coordinate-Navigator"
                            )
                            .GET()
                            .build();


            HttpResponse<String> response =
                    CLIENT.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );


            if (response.statusCode() != 200) {

                LOGGER.warn(
                        "[BCN] GitHub API returned HTTP {}",
                        response.statusCode()
                );

                return null;
            }


            JsonObject json =
                    JsonParser.parseString(
                            response.body()
                    ).getAsJsonObject();


            if (!json.has("tag_name")) {

                LOGGER.warn(
                        "[BCN] GitHub release does not contain tag_name."
                );

                return null;
            }


            String tag =
                    json.get("tag_name")
                            .getAsString();


            /*
             * 对字母v的处理
             */
            if (tag.startsWith("v")) {

                tag = tag.substring(1);

            }


            return tag;


        } catch (Exception e) {

            LOGGER.warn(
                    "[BCN] Failed to check latest version.",
                    e
            );

            return null;
        }
    }


    /**
     * 判断是否存在更新
     */
    public static boolean hasUpdate(
            String current,
            String latest
    ) {

        if (current == null || latest == null) {

            return false;

        }


        return compareVersions(
                latest,
                current
        ) > 0;

    }


    /**
     * 比较两个版本
     */
    public static int compareVersions(
            String latest,
            String current
    ) {

        String[] latestParts =
                normalize(latest)
                        .split("\\.");

        String[] currentParts =
                normalize(current)
                        .split("\\.");


        int length =
                Math.max(
                        latestParts.length,
                        currentParts.length
                );


        for (int i = 0; i < length; i++) {

            int latestNumber =
                    i < latestParts.length
                            ? parsePart(latestParts[i])
                            : 0;

            int currentNumber =
                    i < currentParts.length
                            ? parsePart(currentParts[i])
                            : 0;


            if (latestNumber != currentNumber) {

                return Integer.compare(
                        latestNumber,
                        currentNumber
                );

            }

        }


        return 0;
    }


    /**
     * 清理版本号
     */
    private static String normalize(
            String version
    ) {

        if (version == null) {

            return "0";

        }


        version =
                version.trim()
                        .replaceFirst(
                                "^v",
                                ""
                        );


        int index =
                version.indexOf("-");


        if (index >= 0) {

            version =
                    version.substring(
                            0,
                            index
                    );

        }


        return version;
    }


    private static int parsePart(
            String part
    ) {

        try {

            return Integer.parseInt(part);

        } catch (NumberFormatException e) {

            return 0;

        }

    }

}