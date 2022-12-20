package io.freefair.gradle.plugins.android;

import com.android.build.api.dsl.CommonExtension;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Variant;
import lombok.experimental.UtilityClass;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * @author Lars Grefer
 */
@UtilityClass
public class AndroidProjectUtil {

    public CommonExtension<?, ?, ?, ?> getAndroidExtension(Project project) {
        return project.getExtensions().getByType(CommonExtension.class);
    }

    public AndroidComponentsExtension<?,?,?> getAndroidComponentsExtension(Project project) {
        return project.getExtensions().getByType(AndroidComponentsExtension.class);
    }

    public TaskProvider<JavaCompile> getJavaCompileTaskProvider(Project project, Variant variant) {
        String variantName = variant.getName();

        String compileTaskName = "compile" + StringGroovyMethods.capitalize(variantName) + "JavaWithJavac";

        return project.getTasks().named(compileTaskName, JavaCompile.class);
    }
}
