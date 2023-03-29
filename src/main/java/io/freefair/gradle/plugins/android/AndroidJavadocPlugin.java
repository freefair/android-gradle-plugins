package io.freefair.gradle.plugins.android;

import com.android.build.api.dsl.CommonExtension;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Variant;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

/**
 * @author Lars Grefer
 */
public class AndroidJavadocPlugin implements Plugin<Project> {

    private Map<String, TaskProvider<Javadoc>> javadocTasks = new HashMap<>();

    private TaskProvider<Task> allJavadocTask;

    @Override
    public void apply(Project project) {
        allJavadocTask = project.getTasks().register("javadoc", ajdTasks -> {
            ajdTasks.setDescription("Generate Javadoc for all variants");
            ajdTasks.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
        });

        AndroidComponentsExtension<?, ?, ?> androidComponents = AndroidProjectUtil.getAndroidComponentsExtension(project);

        androidComponents.onVariants(androidComponents.selector().all(), variant -> {
            TaskProvider<Javadoc> javadocTask = getJavadocTask(project, variant);

            allJavadocTask.configure(t -> t.dependsOn(javadocTask));
        });
    }

    public TaskProvider<Javadoc> getJavadocTask(Project project, Variant variant) {

        if (!javadocTasks.containsKey(variant.getName())) {

            TaskProvider<Javadoc> task = project.getTasks().register("javadoc" + capitalize(variant.getName()), Javadoc.class, javadoc -> {
                javadoc.setDescription("Generate Javadoc for the " + variant.getName() + " variant");
                javadoc.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);

                TaskProvider<JavaCompile> javaCompileTaskProvider = AndroidProjectUtil.getJavaCompileTaskProvider(project, variant);

                javadoc.dependsOn(javaCompileTaskProvider);

                javadoc.setSource(variant.getSources().getJava().getAll());

                javadoc.setClasspath(variant.getCompileClasspath());

                CommonExtension<?, ?, ?, ?, ?> android = AndroidProjectUtil.getAndroidExtension(project);

                javadoc.getOptions().setSource(android.getCompileOptions().getSourceCompatibility().getMajorVersion());

                //javadoc.exclude '**/BuildConfig.java'
                javadoc.exclude("**/R.java");

                javadoc.getOptions().encoding("UTF-8");
                javadoc.getOptions().setBootClasspath(new ArrayList<>(javaCompileTaskProvider.get().getOptions().getBootstrapClasspath().getFiles()));

                if (javadoc.getOptions() instanceof StandardJavadocDocletOptions) {
                    StandardJavadocDocletOptions realOptions = (StandardJavadocDocletOptions) javadoc.getOptions();

                    realOptions.docEncoding("UTF-8");
                    realOptions.charSet("UTF-8");

                    String majorVersion = JavaVersion.current().getMajorVersion();
                    if (Integer.parseInt(majorVersion) < 11) {
                        realOptions.links("https://docs.oracle.com/javase/" + majorVersion + "/docs/api/");
                    } else {
                        realOptions.links("https://docs.oracle.com/en/java/javase/" + majorVersion + "/docs/api/");
                    }
                    realOptions.links("https://developer.android.com/reference/");
                }

                javadoc.setFailOnError(false);

                File docsDir;
                if (project.hasProperty("docsDir")) {
                    docsDir = (File) project.property("docsDir");
                } else {
                    docsDir = new File(project.getBuildDir(), "docs");
                }
                File javadocDir = new File(docsDir, "javadoc");
                javadoc.setDestinationDir(new File(javadocDir, variant.getName()));
            });

            javadocTasks.put(variant.getName(), task);
        }

        return javadocTasks.get(variant.getName());
    }
}
