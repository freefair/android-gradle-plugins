package io.freefair.gradle.plugin.android.ci;

/**
 * Created by larsgrefer on 01.10.16.
 */
class CiUtil {

    static boolean isCi() {
        return "true".equals(System.getenv("CI")) ||
                "true".equals(System.getenv("TRAVIS")) ||
                "true".equals(System.getenv("CIRCLECI"));
    }
}
