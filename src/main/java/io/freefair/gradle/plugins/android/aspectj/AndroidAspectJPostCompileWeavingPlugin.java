package io.freefair.gradle.plugins.android.aspectj;

import com.android.build.gradle.TestedExtension;
import io.freefair.gradle.plugins.android.AndroidProjectPlugin;
import io.freefair.gradle.plugins.aspectj.AspectJBasePlugin;
import org.gradle.api.Incubating;

/**
 * Implements AspectJ Post-Compile Weaving using the {@link com.android.build.api.transform.Transform} API.
 */
@Incubating
public class AndroidAspectJPostCompileWeavingPlugin extends AndroidProjectPlugin {

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);

        getProject().getLogger().warn("The android aspectj support is still incubating and subject to change.");

        AspectJBasePlugin basePlugin = getProject().getPlugins().apply(AspectJBasePlugin.class);

        AspectJTransform transform = new AspectJTransform(getProject(), extension, basePlugin.getAspectjConfiguration());

        extension.registerTransform(transform);
    }
}
