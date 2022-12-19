package io.freefair.gradle.plugins.android.lombok;

import com.android.build.api.variant.Variant;
import io.freefair.gradle.plugins.lombok.internal.CleanLombokConfig;
import io.freefair.gradle.plugins.lombok.tasks.LombokConfig;
import lombok.experimental.UtilityClass;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

@UtilityClass
public class AndroidConfigUtil {

    public TaskProvider<LombokConfig> getLombokConfigTask(Project project, Variant variant) {

        String taskName = "generate" + StringGroovyMethods.capitalize(variant.getName()) + "EffectiveLombokConfig";

        return project.getTasks().register(taskName, LombokConfig.class, lombokConfigTask -> {
            lombokConfigTask.setGroup("lombok");
            lombokConfigTask.setDescription("Generate effective Lombok configuration for source-set '" + variant.getName() + "'.");
            lombokConfigTask.getPaths().from(variant.getSources().getJava().getAll());
            lombokConfigTask.getOutputFile().set(project.getLayout().getBuildDirectory().file("lombok/effective-config/lombok-" + variant.getName() + ".config"));
            lombokConfigTask.doLast("cleanLombokConfig", new CleanLombokConfig());
        });
    }
}
