package io.freefair.gradle.plugins.android.maven;

import com.android.build.gradle.TestedExtension;
import io.freefair.gradle.plugins.android.AndroidJavadocPlugin;
import io.freefair.gradle.plugins.android.AndroidProjectPlugin;
import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.bundling.Jar;

import java.util.HashMap;
import java.util.Map;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

public class AndroidJavadocJarPlugin extends AndroidProjectPlugin {

    private TaskProvider<Task> allJavadocJarTask;

    @Getter
    private Map<String, TaskProvider<Jar>> variantJavadocJarTasks = new HashMap<>();

    @Override
    public void apply(Project project) {
        super.apply(project);

        project.getPluginManager().apply(AndroidJavadocPlugin.class);

        allJavadocJarTask = project.getTasks().register("javadocJar", ajdjTask -> {
            ajdjTask.setDescription("Generate the javadoc jar for all variants");
            ajdjTask.setGroup("jar");
        });

    }

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);

        getAndroidVariants().all(variant -> {

            AndroidJavadocPlugin androidJavadocPlugin = getProject().getPlugins().findPlugin(AndroidJavadocPlugin.class);
            TaskProvider<Javadoc> javadocTask = androidJavadocPlugin.getJavadocTask(getProject(), variant);

            TaskProvider<Jar> javadocJarTask = getProject().getTasks().register("javadoc" + capitalize((CharSequence) variant.getName()) + "Jar", Jar.class, jar -> {
                jar.dependsOn(javadocTask);

                jar.setDescription("Generate the javadoc jar for the " + variant.getName() + " variant");
                jar.setGroup("jar");

                jar.getArchiveAppendix().set(variant.getName());
                jar.getArchiveClassifier().set("javadoc");
                jar.from(javadocTask.get().getDestinationDir());
            });

            variantJavadocJarTasks.put(variant.getName(), javadocJarTask);

            allJavadocJarTask.configure(t -> t.dependsOn(javadocJarTask));

            if (publishVariant(variant)) {
                getProject().getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, javadocJarTask);
            }
        });
    }
}
