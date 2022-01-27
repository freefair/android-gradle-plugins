package io.freefair.gradle.plugins.android;

import com.android.build.api.dsl.AndroidSourceSet;
import com.android.build.gradle.TestedExtension;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet;
import com.android.builder.model.SourceProvider;
import io.freefair.gradle.plugins.lombok.LombokBasePlugin;
import io.freefair.gradle.plugins.lombok.LombokPlugin;
import io.freefair.gradle.plugins.lombok.internal.ConfigUtil;
import io.freefair.gradle.plugins.lombok.tasks.Delombok;
import io.freefair.gradle.plugins.lombok.tasks.LombokConfig;
import org.gradle.api.Task;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.*;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

/**
 * @author Lars Grefer
 */
public class AndroidLombokPlugin extends AndroidProjectPlugin {

    private LombokBasePlugin lombokBasePlugin;

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);

        LombokPlugin lombokPlugin = getProject().getPlugins().apply(LombokPlugin.class);
        lombokBasePlugin = lombokPlugin.getLombokBasePlugin();

        extension.getSourceSets().all(androidSourceSet -> {
            getProject().getConfigurations().getByName(androidSourceSet.getCompileOnlyConfigurationName())
                    .extendsFrom(lombokBasePlugin.getLombokConfiguration());
            getProject().getConfigurations().getByName(androidSourceSet.getAnnotationProcessorConfigurationName())
                    .extendsFrom(lombokBasePlugin.getLombokConfiguration());

            getProject().afterEvaluate(p -> createSourceSetConfigTasks(androidSourceSet));
        });

        getAndroidVariants().all(variant -> {
            String delombokName = "delombok" + capitalize(variant.getName());

            TaskProvider<Delombok> delombokProvider = getProject().getTasks().register(delombokName, Delombok.class, delombok -> {

                delombok.setDescription("Runs delombok on the " + delombokName + " variant");
            });

            TaskProvider<JavaCompile> compileJava = variant.getJavaCompileProvider();
            compileJava.configure(cj -> {
                cj.getOptions().getCompilerArgs().add("-Xlint:-processing");
            });

            getProject().afterEvaluate(p -> {
                handleLombokConfig(variant, compileJava);

                delombokProvider.configure(delombok -> {
                    delombok.getEncoding().set(compileJava.get().getOptions().getEncoding());
                    delombok.getClasspath().from(getCompileClasspath(variant));

                    variant.getSourceSets().forEach(sourceProvider -> {
                        delombok.getInput().from(sourceProvider.getJavaDirectories());
                    });
                });
            });

            getProject().getPlugins().withType(AndroidJavadocPlugin.class, androidJavadocPlugin -> {
                androidJavadocPlugin.getJavadocTask(getProject(), variant)
                        .configure(javadoc -> javadoc.setSource(delombokProvider));
            });
        });
    }

    Map<String, Collection<TaskProvider<LombokConfig>>> lombokConfigTasksBySourceSet = new HashMap<>();

    private void createSourceSetConfigTasks(AndroidSourceSet sourceSet) {
        if (lombokBasePlugin.getLombokExtension().getDisableConfig().get()) {
            return;
        }

        DefaultAndroidSourceDirectorySet java = (DefaultAndroidSourceDirectorySet) sourceSet.getJava();

        Map<File, TaskProvider<LombokConfig>> lombokConfigTasks = ConfigUtil.getLombokConfigTasks(getProject(), sourceSet.getName(), java.getSrcDirs());

        String taskName = "generate" + capitalize(sourceSet.getName()) + "EffectiveLombokConfigs";
        TaskProvider<Task> generateConfigsTask = getProject().getTasks().register(taskName, genConfigsTask -> {
            genConfigsTask.setGroup("lombok");
            genConfigsTask.setDescription("Generate effective Lombok configurations for source-set '" + sourceSet.getName() + "'");
            lombokConfigTasks.values().forEach(genConfigsTask::dependsOn);
        });

        lombokConfigTasksBySourceSet.put(sourceSet.getName(), lombokConfigTasks.values());
    }

    private void handleLombokConfig(BaseVariant variant, TaskProvider<JavaCompile> compileTaskProvider) {
        if (lombokBasePlugin.getLombokExtension().getDisableConfig().get()) {
            return;
        }

        Set<TaskProvider<LombokConfig>> configTasks = new HashSet<>();

        for (SourceProvider sourceSet : variant.getSourceSets()) {
            configTasks.addAll(lombokConfigTasksBySourceSet.get(sourceSet.getName()));
        }

        variant.getJavaCompileProvider().configure(javaCompile -> {
            configTasks.forEach((lombokConfigTaskProvider) -> {
                javaCompile.getInputs().file(lombokConfigTaskProvider.get().getOutputFile())
                        .withPathSensitivity(PathSensitivity.NONE)
                        .optional();
            });
        });
    }
}
