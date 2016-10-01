package io.freefair.gradle.plugin.android

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.internal.jvm.Jvm

class AndroidJavadocPlugin extends AndroidProjectPlugin {

    Map<String, Javadoc> javadocTasks;

    Task allJavadocTask;

    @Override
    void apply(Project project) {
        super.apply(project)

        javadocTasks = new HashMap<>();

        allJavadocTask = project.task("javadoc") { Task ajdTasks ->
            ajdTasks.description = "Generate Javadoc for all variants"
            ajdTasks.group = JavaBasePlugin.DOCUMENTATION_GROUP
        }

        androidVariants.all { variant ->
            Javadoc javadocTask = getJavadocTask(project, variant)

            allJavadocTask.dependsOn javadocTask
        }
    }

    Javadoc getJavadocTask(Project project, BaseVariant variant) {

        if (!javadocTasks.containsKey(variant.name)) {

            Javadoc task = project.task("javadoc${variant.name.capitalize()}", type: Javadoc) { Javadoc javadoc ->
                javadoc.description = "Generate Javadoc for the $variant.name variant"
                javadoc.group = JavaBasePlugin.DOCUMENTATION_GROUP;

                javadoc.dependsOn variant.javaCompiler

                javadoc.source = variant.javaCompiler.source
                javadoc.classpath = project.files(variant.javaCompiler.destinationDir) + variant.javaCompiler.classpath

                //javadoc.exclude '**/BuildConfig.java'
                javadoc.exclude '**/R.java'

                javadoc.options.encoding "UTF-8"
                javadoc.options.bootClasspath = androidExtension.getBootClasspath() + Jvm.current().runtimeJar;
                javadoc.options.source = variant.javaCompiler.sourceCompatibility

                if (javadoc.getOptions() instanceof StandardJavadocDocletOptions) {
                    StandardJavadocDocletOptions realOptions = javadoc.options as StandardJavadocDocletOptions

                    realOptions.docEncoding "UTF-8"
                    realOptions.charSet "UTF-8"

                    realOptions.links "http://docs.oracle.com/javase/${Jvm.current().javaVersion.majorVersion ?: '8'}/docs/api/"
                    realOptions.linksOffline "http://developer.android.com/reference/", "${androidExtension.sdkDirectory}/docs/reference"
                }

                javadoc.setFailOnError false

                if (project.hasProperty("docsDir")) {
                    javadoc.destinationDir = project.file("$project.docsDir/javadoc/${variant.dirName}")
                } else {
                    javadoc.destinationDir = project.file("$project.buildDir/docs/javadoc/${variant.dirName}")
                }
            } as Javadoc

            javadocTasks.put(variant.name, task);
        }

        return javadocTasks.get(variant.name);
    }
}
