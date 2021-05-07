package io.freefair.gradle.plugins.android.maven;

import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.FeatureVariant;
import com.android.build.gradle.api.LibraryVariant;
import io.freefair.gradle.plugins.android.AndroidProjectPlugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

import javax.annotation.Nonnull;

public class AndroidMavenPublishBasePlugin extends AndroidProjectPlugin {

    @Override
    public void apply(@Nonnull Project project) {
        project.getPlugins().apply(MavenPublishPlugin.class);
        super.apply(project);
    }

    public MavenPublication createMavenPublication(BaseVariant variant) {

        PublishingExtension publishing = getProject().getExtensions().getByType(PublishingExtension.class);

        MavenPublication mavenPublication;

        if (variant instanceof LibraryVariant) {
            mavenPublication = publishing.getPublications().create(variant.getName() + "AndroidLibrary", MavenPublication.class, publication -> {
                publication.getPom().setPackaging("aar");
            });
        }
        else if (variant instanceof ApplicationVariant) {
            mavenPublication = publishing.getPublications().create(variant.getName() + "AndroidApplication", MavenPublication.class, publication -> {
                publication.getPom().setPackaging("apk");
            });
        }
        else if (variant instanceof FeatureVariant) {
            mavenPublication = publishing.getPublications().create(variant.getName() + "AndroidFeature", MavenPublication.class, publication -> {
                publication.getPom().setPackaging("apk");
            });
        }
        else {
            throw new IllegalStateException(variant.getClass().getName());
        }

        getProject().afterEvaluate(p -> {
            mavenPublication.from(p.getComponents().getByName(variant.getName()));
        });

        getProject().getPlugins().withType(AndroidJavadocJarPlugin.class, androidJavadocJarPlugin -> {
            TaskProvider<Jar> javadocJarTask = androidJavadocJarPlugin.getVariantJavadocJarTasks().get(variant.getName());
            mavenPublication.artifact(javadocJarTask.get(), mavenArtifact -> mavenArtifact.setClassifier("javadoc"));
        });

        getProject().getPlugins().withType(AndroidSourcesJarPlugin.class, androidJavadocJarPlugin -> {
            TaskProvider<Jar> sourcesJarTask = androidJavadocJarPlugin.getVariantSourcesJarTasks().get(variant.getName());
            mavenPublication.artifact(sourcesJarTask.get(), mavenArtifact -> mavenArtifact.setClassifier("sources"));
        });

        return mavenPublication;
    }
}
