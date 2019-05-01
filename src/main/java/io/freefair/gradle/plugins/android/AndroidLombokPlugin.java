package io.freefair.gradle.plugins.android;

import com.android.build.gradle.TestedExtension;
import io.freefair.gradle.plugins.lombok.Delombok;
import io.freefair.gradle.plugins.lombok.LombokPlugin;
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
        lombokPlugin.getLombokExtension().getVersion().set("1.16.20");
        lombokPlugin.getLombokExtension().getConfig().put("lombok.addGeneratedAnnotation", "false");
        lombokPlugin.getLombokExtension().getConfig().put("lombok.addJavaxGeneratedAnnotation", "false");
        lombokPlugin.getLombokExtension().getConfig().put("lombok.anyConstructor.suppressConstructorProperties", "true");

        extension.getSourceSets().all(androidSourceSet -> {
            getProject().getConfigurations().getByName(androidSourceSet.getCompileOnlyConfigurationName())
                    .extendsFrom(lombokPlugin.getLombokConfiguration());
            getProject().getConfigurations().getByName(androidSourceSet.getAnnotationProcessorConfigurationName())
                    .extendsFrom(lombokPlugin.getLombokConfiguration());
        });

        getAndroidVariants().forEach(variant -> {
            String delombokName = "delombok" + capitalize((CharSequence) variant.getName());

            TaskProvider<Delombok> delombokProvider = getProject().getTasks().register(delombokName, Delombok.class, delombok -> {

                delombok.setDescription("Runs delombok on the " + delombokName + " variant");
            });

            TaskProvider<JavaCompile> compileJava = getJavaCompile(variant);
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
