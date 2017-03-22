package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.api.BaseVariant;

import java.util.Collection;

/**
 * Copy of {@link org.gradle.api.plugins.quality.CodeQualityExtension} which uses {@link BaseVariant variants}
 *
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.CodeQualityExtension
 */
public class VariantBasedCodeQualityExtension extends AndroidCodeQualityExtension {

    private Collection<BaseVariant> variants;

    public Collection<BaseVariant> getVariants() {
        return this.variants;
    }

    public void setVariants(Collection<BaseVariant> variants) {
        this.variants = variants;
    }

}
