package io.freefair.gradle.plugins.android.quality;

import lombok.NoArgsConstructor;

import java.io.File;

/**
 * Copy of {@link org.gradle.api.plugins.quality.CodeQualityExtension} without source sets.
 *
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.CodeQualityExtension
 */
@NoArgsConstructor
public class AndroidCodeQualityExtension {

    private String toolVersion;

    private boolean ignoreFailures;
    private File reportsDir;

    public String getToolVersion() {
        return this.toolVersion;
    }

    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }

    public boolean isIgnoreFailures() {
        return this.ignoreFailures;
    }

    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
    }

    public File getReportsDir() {
        return this.reportsDir;
    }

    public void setReportsDir(File reportsDir) {
        this.reportsDir = reportsDir;
    }
}
