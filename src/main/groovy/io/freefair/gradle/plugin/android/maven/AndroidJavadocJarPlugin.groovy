package io.freefair.gradle.plugin.android.maven

import io.freefair.gradle.plugin.android.AndroidJavadocPlugin
import io.freefair.gradle.plugin.android.AndroidProjectPlugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar

class AndroidJavadocJarPlugin extends AndroidProjectPlugin {

    @Override
    void apply(Project project) {
        super.apply(project)

        AndroidJavadocPlugin ajp = project.getPluginManager().apply(AndroidJavadocPlugin);

        Task allJavadocJarTask = project.task("javadocJar") { Task ajdjTask ->
            ajdjTask.description = "Generate the javadoc jar for all variants"
            ajdjTask.group = "jar"
        }

        androidVariants.all { variant ->

            Javadoc javadocTask = ajp.javadocTasks.get(variant.name);

            Jar javadocJarTask = project.task("javadoc${variant.name.capitalize()}Jar", type: Jar, dependsOn: javadocTask) { Jar jar ->
                jar.description = "Generate the javadoc jar for the ${variant.name} variant"
                jar.group = "jar"

                jar.appendix = variant.name
                jar.classifier = 'javadoc'
                jar.from javadocTask.destinationDir
            } as Jar

            allJavadocJarTask.dependsOn javadocJarTask

            if (publishVariant(variant)) {
                project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, javadocJarTask)
            }
        }
    }
}
