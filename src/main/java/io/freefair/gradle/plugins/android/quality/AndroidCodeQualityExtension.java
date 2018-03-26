package io.freefair.gradle.plugins.android.quality;

import lombok.Data;

import java.io.File;

/**
 * Copy of {@link org.gradle.api.plugins.quality.CodeQualityExtension} without source sets.
 *
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.CodeQualityExtension
 */
@Data
public class AndroidCodeQualityExtension {

    private String toolVersion;
    private boolean ignoreFailures;
    private File reportsDir;

    /**
     * The version of the code quality tool to be used.
     */
    public String getToolVersion() {
        return toolVersion;
    }

    /**
     * The version of the code quality tool to be used.
     */
    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }

    /**
     * Whether to allow the build to continue if there are warnings.
     *
     * Example: ignoreFailures = true
     */
    public boolean isIgnoreFailures() {
        return ignoreFailures;
    }

    /**
     * Whether to allow the build to continue if there are warnings.
     *
     * Example: ignoreFailures = true
     */
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
    }

    /**
     * The directory where reports will be generated.
     */
    public File getReportsDir() {
        return reportsDir;
    }

    /**
     * The directory where reports will be generated.
     */
    public void setReportsDir(File reportsDir) {
        this.reportsDir = reportsDir;
    }
}
