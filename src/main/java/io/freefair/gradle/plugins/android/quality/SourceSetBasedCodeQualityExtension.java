package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.api.AndroidSourceSet;

import java.util.Collection;

/**
 * Copy of {@link org.gradle.api.plugins.quality.CodeQualityExtension} which uses {@link AndroidSourceSet AndroidSourceSets}
 *
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.CodeQualityExtension
 */
public class SourceSetBasedCodeQualityExtension extends AndroidCodeQualityExtension {

    private Collection<AndroidSourceSet> sourceSets;

    public Collection<AndroidSourceSet> getSourceSets() {
        return this.sourceSets;
    }

    public void setSourceSets(Collection<AndroidSourceSet> sourceSets) {
        this.sourceSets = sourceSets;
    }

}
