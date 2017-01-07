package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.api.AndroidSourceSet;
import io.freefair.gradle.plugins.android.AndroidProjectPlugin;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.internal.file.UnionFileTree;
import org.gradle.api.internal.jvm.ClassDirectoryBinaryNamingScheme;
import org.gradle.api.tasks.SourceSet;

abstract class AndroidCodeQualityHelper extends AndroidProjectPlugin {

    /**
     * Returns the name of a task for this source set.
     *
     * @param verb The action, may be null.
     * @param target The target, may be null
     * @return The task name, generally of the form ${verb}${name}${noun}
     * @see SourceSet#getTaskName(String, String)
     */
    protected static String getTaskName(AndroidSourceSet sourceSet, String verb, String target) {
        return new ClassDirectoryBinaryNamingScheme(sourceSet.getName()).getTaskName(verb, target);
    }

    /**
     * Returns the classpath used to compile this source.
     *
     * @return The classpath. Never returns null.
     * @see SourceSet#getCompileClasspath()
     */
    protected FileCollection getCompileClasspath(AndroidSourceSet androidSourceSet) {
        return getProject().getConfigurations().getByName(androidSourceSet.getCompileConfigurationName());
    }

    /**
     * All Java source files for this source set. This includes, for example, source which is directly compiled, and
     * source which is indirectly compiled through joint compilation.
     *
     * @return the Java source. Never returns null.
     * @see SourceSet#getAllJava()
     */
    protected static FileTree getAllJava(AndroidSourceSet androidSourceSet) {
        return androidSourceSet.getJava().getSourceFiles();
    }

    /**
     * @see SourceSet#getOutput()
     */
    protected FileCollection getOutput(AndroidSourceSet androidSourceSet) {
        FileTreeInternal[] sourceTrees = getAndroidVariants().stream()
                .filter(variant -> variant.getJavaCompile() != null)
                .filter(variant -> variant.getSourceSets().contains(androidSourceSet))
                .map(variant -> variant.getJavaCompile().getDestinationDir())
                .map(outputDir -> (FileTreeInternal) getProject().fileTree(outputDir))
                .toArray(FileTreeInternal[]::new);
        return new UnionFileTree(sourceTrees);
    }
}
