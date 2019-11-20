package io.freefair.gradle.plugins.android;

import com.android.build.gradle.TestedExtension;
import io.freefair.gradle.plugins.lombok.LombokBasePlugin;
import io.freefair.gradle.plugins.lombok.LombokPlugin;
import io.freefair.gradle.plugins.lombok.tasks.Delombok;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

/**
 * @author Lars Grefer
 */
public class AndroidLombokPlugin extends AndroidProjectPlugin {

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);

        LombokPlugin lombokPlugin = getProject().getPlugins().apply(LombokPlugin.class);
        LombokBasePlugin lombokBasePlugin = lombokPlugin.getLombokBasePlugin();
        lombokBasePlugin.getLombokExtension().getVersion().set("1.16.20");
        lombokBasePlugin.getLombokExtension().getConfig().put("lombok.addGeneratedAnnotation", "false");
        lombokBasePlugin.getLombokExtension().getConfig().put("lombok.addJavaxGeneratedAnnotation", "false");
        lombokBasePlugin.getLombokExtension().getConfig().put("lombok.anyConstructor.suppressConstructorProperties", "true");

        extension.getSourceSets().all(androidSourceSet -> {
            getProject().getConfigurations().getByName(androidSourceSet.getCompileOnlyConfigurationName())
                    .extendsFrom(lombokBasePlugin.getLombokConfiguration());
            getProject().getConfigurations().getByName(androidSourceSet.getAnnotationProcessorConfigurationName())
                    .extendsFrom(lombokBasePlugin.getLombokConfiguration());
        });

        getAndroidVariants().forEach(variant -> {
            String delombokName = "delombok" + capitalize((CharSequence) variant.getName());

            TaskProvider<Delombok> delombokProvider = getProject().getTasks().register(delombokName, Delombok.class, delombok -> {

                delombok.setDescription("Runs delombok on the " + delombokName + " variant");
            });

            TaskProvider<JavaCompile> compileJava = variant.getJavaCompileProvider();
            compileJava.configure(cj -> {
                cj.dependsOn(lombokPlugin.getGenerateLombokConfig());
                cj.getOptions().getCompilerArgs().add("-Xlint:-processing");
            });

            getProject().afterEvaluate(p -> {
                compileJava.configure(cj -> {
                    cj.getInputs().file(lombokPlugin.getGenerateLombokConfig().get().getOutputFile());
                });

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
}
