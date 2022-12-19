package io.freefair.gradle.plugins.android;

import com.android.build.api.dsl.CommonExtension;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Variant;
import io.freefair.gradle.plugins.lombok.LombokBasePlugin;
import io.freefair.gradle.plugins.lombok.LombokPlugin;
import io.freefair.gradle.plugins.lombok.internal.AndroidConfigUtil;
import io.freefair.gradle.plugins.lombok.tasks.Delombok;
import io.freefair.gradle.plugins.lombok.tasks.LombokConfig;
import lombok.Getter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

/**
 * @author Lars Grefer
 */
public class AndroidLombokPlugin implements Plugin<Project> {

    @Getter
    private Project project;

    private LombokBasePlugin lombokBasePlugin;

    @Override
    public void apply(Project project) {
        this.project = project;

        LombokPlugin lombokPlugin = project.getPlugins().apply(LombokPlugin.class);
        lombokBasePlugin = lombokPlugin.getLombokBasePlugin();

        CommonExtension<?, ?, ?, ?> android = AndroidProjectUtil.getAndroidExtension(project);
        AndroidComponentsExtension<?, ?, ?> androidComponents = AndroidProjectUtil.getAndroidComponentsExtension(project);

        android.getSourceSets().all(androidSourceSet -> {
            project.getConfigurations().getByName(androidSourceSet.getCompileOnlyConfigurationName())
                    .extendsFrom(lombokBasePlugin.getLombokConfiguration());
            project.getConfigurations().getByName(androidSourceSet.getAnnotationProcessorConfigurationName())
                    .extendsFrom(lombokBasePlugin.getLombokConfiguration());
        });

        androidComponents.onVariants(androidComponents.selector().all(), variant -> {
            String delombokName = "delombok" + capitalize(variant.getName());

            TaskProvider<Delombok> delombokProvider = project.getTasks().register(delombokName, Delombok.class, delombok -> {
                delombok.setDescription("Runs delombok on the " + delombokName + " variant");
            });

            TaskProvider<JavaCompile> compileJava = AndroidProjectUtil.getJavaCompileTaskProvider(project, variant);
            compileJava.configure(cj -> {
                cj.getOptions().getCompilerArgs().add("-Xlint:-processing");
            });

            project.afterEvaluate(p -> {
                handleLombokConfig(variant, compileJava, delombokProvider);

                delombokProvider.configure(delombok -> {
                    delombok.getEncoding().set(compileJava.get().getOptions().getEncoding());
                    delombok.getClasspath().from(variant.getCompileClasspath());

                    delombok.getInput().from(variant.getSources().getJava().getAll());

                });
            });

            project.getPlugins().withType(AndroidJavadocPlugin.class, androidJavadocPlugin -> {
                androidJavadocPlugin.getJavadocTask(project, variant)
                        .configure(javadoc -> javadoc.setSource(delombokProvider));
            });
        });
    }

    private void handleLombokConfig(Variant sourceSet, TaskProvider<JavaCompile> compileTaskProvider, TaskProvider<Delombok> delombokTaskProvider) {
        if (lombokBasePlugin.getLombokExtension().getDisableConfig().get()) {
            return;
        }

        TaskProvider<LombokConfig> lombokConfigTask = AndroidConfigUtil.getLombokConfigTask(project, sourceSet);

        compileTaskProvider.configure(javaCompile -> {
            javaCompile.getInputs().file(lombokConfigTask.get().getOutputFile())
                    .withPropertyName("lombok.config")
                    .withPathSensitivity(PathSensitivity.NONE)
                    .optional();
        });

        delombokTaskProvider.configure(delombok -> {
            delombok.getInputs().file(lombokConfigTask.get().getOutputFile())
                    .withPropertyName("lombok.config")
                    .withPathSensitivity(PathSensitivity.NONE)
                    .optional();
        });
    }


}
