package io.freefair.gradle.plugin
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar

class AndroidMavenPlugin implements Plugin<Project>{
    @Override
    void apply(Project project) {

        DefaultDomainObjectSet<BaseVariant> variants = null;
        LibraryExtension libraryExtension = null;

        try {
            libraryExtension = project.android
            variants = libraryExtension.libraryVariants;
        } catch (Exception e) {
            project.logger.debug("No Library found", e)
        }

        try {
            AppExtension appExt = project.android
            variants = appExt.applicationVariants;
        } catch (Exception e){
            project.logger.debug("No Application found", e)
        }

        if (variants == null ){
            project.logger.error("No Android Variants found")
            return;
        }

        variants.all { variant ->

            Jar sourcesJarTask = project.task("sources${variant.name.capitalize()}Jar", type: Jar) {
                description = "Generate the source jar for the $variant.name variant"
                classifier = "sources"
                from variant.javaCompiler.source
            } as Jar

            Javadoc javadocTask = project.task("javadoc${variant.name.capitalize()}", type: Javadoc) { Javadoc javadoc ->
                description = "Generate Javadoc for the $variant.name variant"

                javadoc.source = variant.javaCompiler.source
                javadoc.classpath = variant.javaCompiler.classpath

                if(javadoc.getOptions() instanceof StandardJavadocDocletOptions){
                    StandardJavadocDocletOptions realOptions = getOptions()

                    realOptions.links "http://docs.oracle.com/javase/7/docs/api/"
                    realOptions.links "http://developer.android.com/reference/"
                }

                javadoc.setFailOnError false


            } as Javadoc

            Jar javadocJarTask = project.task("javadoc${variant.name.capitalize()}Jar", type: Jar, dependsOn: javadocTask) {
                description = "Generate the javadoc jar for the ${variant.name} variant"

                classifier = 'javadoc'
                from javadocTask.destinationDir
            } as Jar

            if(libraryExtension == null || (libraryExtension.publishNonDefault || libraryExtension.defaultPublishConfig.equals(variant.name))){
                project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, sourcesJarTask)
                project.artifacts.add(Dependency.ARCHIVES_CONFIGURATION, javadocJarTask)
            }

        }
    }
}
