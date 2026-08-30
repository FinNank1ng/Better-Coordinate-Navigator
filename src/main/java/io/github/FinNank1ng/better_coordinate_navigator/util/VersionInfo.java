package io.github.FinNank1ng.better_coordinate_navigator.util;

public record VersionInfo(
        String currentVersion,
        String latestVersion,
        String releaseUrl,
        UpdateStatus status
) {

    public enum UpdateStatus {

        /**
         * 当前已经是最新正式版本
         */
        UP_TO_DATE,

        /**
         * 发现新版本
         */
        UPDATE_AVAILABLE,

        /**
         * 无法检查更新
         */
        CHECK_FAILED

    }

    public boolean hasUpdate() {

        return status == UpdateStatus.UPDATE_AVAILABLE;

    }

}