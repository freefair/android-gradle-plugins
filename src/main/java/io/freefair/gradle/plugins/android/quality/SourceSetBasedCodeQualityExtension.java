package io.freefair.gradle.plugins.android.quality;

import com.android.build.api.dsl.AndroidSourceSet;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * Copy of {@link org.gradle.api.plugins.quality.CodeQualityExtension} which uses {@link AndroidSourceSet AndroidSourceSets}
 *
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.CodeQualityExtension
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SourceSetBasedCodeQualityExtension extends AndroidCodeQualityExtension {

    private Collection<AndroidSourceSet> sourceSets;

}
