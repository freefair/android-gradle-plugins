package io.freefair.gradle.plugins.android.maven;

import com.android.build.gradle.TestedExtension;
import io.freefair.gradle.plugins.android.AndroidProjectPlugin;
import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.HashMap;
import java.util.Map;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

public class AndroidSourcesJarPlugin extends AndroidProjectPlugin {

    private TaskProvider<Task> allSourcesJarTask;

    @Getter
    private Map<String, TaskProvider<Jar>> variantSourcesJarTasks = new HashMap<>();

    @Override
    public void apply(Project project) {
        super.apply(project);

        allSourcesJarTask = project.getTasks().register("sourcesJar", asjTask -> {
            asjTask.setDescription("Generate the sources jar for all variants");
            asjTask.setGroup("jar");
        });
    }

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);

        getAndroidVariants().all(variant -> {
            TaskProvider<Jar> sourcesJarTask = getProject().getTasks().register("sources" + capitalize((CharSequence) variant.getName()) + "Jar", Jar.class, jar -> {
                jar.setDescription("Generate the sources jar for the " + variant.getName() + " variant");
                jar.setGroup("jar");

                jar.getArchiveClassifier().set("sources");
                jar.getArchiveAppendix().set(variant.getName());
                jar.from(variant.getJavaCompileProvider().map(JavaCompile::getSource));
            });

            variantSourcesJarTasks.put(variant.getName(), sourcesJarTask);

            allSourcesJarTask.configure(t -> t.dependsOn(sourcesJarTask));

            if (publishVariant(variant)) {
                getProject().getArtifacts().add(Dependency.ARCHIVES_CONFIGURATION, sourcesJarTask);
            }
        });
    }
}
