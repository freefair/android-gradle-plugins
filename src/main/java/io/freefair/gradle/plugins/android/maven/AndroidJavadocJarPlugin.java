package io.freefair.gradle.plugins.android.maven;

import io.freefair.gradle.plugins.android.AndroidJavadocPlugin;
import io.freefair.gradle.plugins.android.AndroidProjectPlugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.jvm.tasks.Jar;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

public class AndroidJavadocJarPlugin extends AndroidProjectPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);

        project.getPluginManager().apply(AndroidJavadocPlugin.class);

        Task allJavadocJarTask = project.getTasks().create("javadocJar", ajdjTask -> {
            ajdjTask.setDescription("Generate the javadoc jar for all variants");
            ajdjTask.setGroup("jar");
        });

        getAndroidVariants().all(variant -> {

            AndroidJavadocPlugin androidJavadocPlugin = project.getPlugins().findPlugin(AndroidJavadocPlugin.class);
            Javadoc javadocTask = androidJavadocPlugin.getJavadocTask(project, variant);

            Jar javadocJarTask = project.getTasks().create("javadoc" + capitalize((CharSequence) variant.getName()) + "Jar", Jar.class, jar -> {
                jar.dependsOn(javadocTask);

                jar.setDescription("Generate the javadoc jar for the " + variant.getName() + " variant");
                jar.setGroup("jar");

                jar.setAppendix(variant.getName());
                jar.setClassifier("javadoc");
                jar.from(javadocTask.getDestinationDir());
            });

            allJavadocJarTask.dependsOn(javadocJarTask);

            if (publishVariant(variant)) {
                project.getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, javadocJarTask);
            }
        });
    }
}
