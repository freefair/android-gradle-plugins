package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.api.BaseVariant;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * Copy of {@link org.gradle.api.plugins.quality.CodeQualityExtension} which uses {@link BaseVariant variants}
 *
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.CodeQualityExtension
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VariantBasedCodeQualityExtension extends AndroidCodeQualityExtension {

    private Collection<BaseVariant> variants;

}
