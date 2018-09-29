package io.freefair.gradle.plugins.android;

import com.android.build.gradle.TestedExtension;
import io.freefair.gradle.plugins.lombok.Delombok;
import io.freefair.gradle.plugins.lombok.LombokPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

/**
 * @author Lars Grefer
 */
public class AndroidLombokPlugin extends AndroidProjectPlugin {

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);

        LombokPlugin lombokPlugin = getProject().getPlugins().apply(LombokPlugin.class);
        lombokPlugin.getLombokExtension().setVersion("1.16.20");
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
            String delombokName = "delombok" + capitalize((CharSequence)variant.getName());

            Delombok delombok = getProject().getTasks().create(delombokName, Delombok.class);
            delombok.setDescription("Runs delombok on the " + delombokName + " variant");

            JavaCompile compileJava = getJavaCompile(variant);
            compileJava.dependsOn(lombokPlugin.getGenerateLombokConfig());
            compileJava.getOptions().getCompilerArgs().add("-Xlint:-processing");
            getProject().afterEvaluate(p -> {
                delombok.getEncoding().set(compileJava.getOptions().getEncoding());
                delombok.getClasspath().from(getCompileClasspath(variant));
                compileJava.getInputs().file(lombokPlugin.getGenerateLombokConfig().getOutputFile());

                variant.getSourceSets().forEach(sourceProvider -> {
                    delombok.getInput().from(sourceProvider.getJavaDirectories());
                });
            });

            getProject().getPlugins().withType(AndroidJavadocPlugin.class, androidJavadocPlugin -> {
                Javadoc javadocTask = androidJavadocPlugin.getJavadocTask(getProject(), variant);

                javadocTask.setSource(delombok);
            });
        });
    }
}
