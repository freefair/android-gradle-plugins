package io.freefair.gradle.plugins.android.lombok;

import com.android.build.api.variant.Variant;
import io.freefair.gradle.plugins.lombok.internal.CleanLombokConfig;
import io.freefair.gradle.plugins.lombok.tasks.LombokConfig;
import lombok.experimental.UtilityClass;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

/**
 * @author Lars Grefer
 */
@UtilityClass
public class AndroidConfigUtil {

    public TaskProvider<AndroidLombokConfig> getLombokConfigTask(Project project, Variant variant) {

        String taskName = "generate" + StringGroovyMethods.capitalize(variant.getName()) + "EffectiveLombokConfig";

        return project.getTasks().register(taskName, AndroidLombokConfig.class, lombokConfigTask -> {
            //lombokConfigTask.dependsOn(project.getTasks().named("generate" + StringGroovyMethods.capitalize(variant.getName()) + "BuildConfig"));
            lombokConfigTask.setGroup("lombok");
            lombokConfigTask.setDescription("Generate effective Lombok configuration for variant '" + variant.getName() + "'.");
            lombokConfigTask.getPaths().from(variant.getSources().getJava().getAll());
            lombokConfigTask.getOutputFile().set(project.getLayout().getBuildDirectory().file("lombok/effective-config/lombok-" + variant.getName() + ".config"));
            lombokConfigTask.doLast("cleanLombokConfig", new CleanLombokConfig());
        });
    }
}
