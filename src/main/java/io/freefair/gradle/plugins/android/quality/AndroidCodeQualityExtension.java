package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.api.AndroidSourceSet;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.Collection;

/**
 * Copy of {@link org.gradle.api.plugins.quality.CodeQualityExtension} which uses {@link AndroidSourceSet AndroidSourceSets}
 *
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.CodeQualityExtension
 */
@NoArgsConstructor
public class AndroidCodeQualityExtension {

    private String toolVersion;
    private Collection<AndroidSourceSet> sourceSets;
    private boolean ignoreFailures;
    private File reportsDir;

    public String getToolVersion() {
        return this.toolVersion;
    }

    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }

    public Collection<AndroidSourceSet> getSourceSets() {
        return this.sourceSets;
    }

    public void setSourceSets(Collection<AndroidSourceSet> sourceSets) {
        this.sourceSets = sourceSets;
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
