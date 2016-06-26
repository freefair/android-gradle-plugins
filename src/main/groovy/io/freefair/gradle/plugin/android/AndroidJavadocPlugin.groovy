package io.freefair.gradle.plugin.android

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions

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

            Javadoc javadocTask = project.task("javadoc${variant.name.capitalize()}", type: Javadoc) { Javadoc javadoc ->
                javadoc.description = "Generate Javadoc for the $variant.name variant"
                javadoc.group = JavaBasePlugin.DOCUMENTATION_GROUP;

                javadoc.dependsOn variant.javaCompiler

                javadoc.source = variant.javaCompiler.source
                javadoc.classpath = variant.javaCompiler.classpath + project.files(androidExtension.getBootClasspath())

                javadoc.exclude '**/BuildConfig.java'
                javadoc.exclude '**/R.java'

                if (javadoc.getOptions() instanceof StandardJavadocDocletOptions) {
                    StandardJavadocDocletOptions realOptions = javadoc.options as StandardJavadocDocletOptions

                    realOptions.links "http://docs.oracle.com/javase/7/docs/api/"
                    realOptions.linksOffline "http://developer.android.com/reference/", "${androidExtension.sdkDirectory}/docs/reference"
                    realOptions.addStringOption('Xdoclint:none', '-quiet')
                }

                javadoc.setFailOnError false

                if (project.hasProperty("docsDir")) {
                    javadoc.destinationDir = new File(project.docsDir, "javadoc/${variant.dirName}")
                }

            } as Javadoc

            javadocTasks.put(variant.name, javadocTask);

            allJavadocTask.dependsOn javadocTask


        }
    }
}
