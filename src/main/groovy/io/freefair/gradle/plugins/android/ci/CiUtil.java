package io.freefair.gradle.plugins.android.ci;

class CiUtil {

    static boolean isCi() {
        return "true".equals(System.getenv("CI")) ||
                "true".equals(System.getenv("TRAVIS")) ||
                "true".equals(System.getenv("CIRCLECI"));
    }
}
