package io.freefair.gradle.plugins.android.maven;

import com.android.build.gradle.TestedExtension;
import io.freefair.gradle.plugins.android.AndroidProjectPlugin;
import org.gradle.api.Project;

public class AndroidMavenPublishPlugin extends AndroidProjectPlugin {

    private AndroidMavenPublishBasePlugin mavenPublishBasePlugin;

    @Override
    public void apply(Project project) {
        mavenPublishBasePlugin = project.getPlugins().apply(AndroidMavenPublishBasePlugin.class);
        super.apply(project);
    }

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);

        getAndroidVariants().all(variant -> {
            if (publishVariant(variant)) {
                mavenPublishBasePlugin.createMavenPublication(variant);
            }
        });
    }
}
