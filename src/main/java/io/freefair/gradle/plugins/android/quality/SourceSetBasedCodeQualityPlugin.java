package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.TestedExtension;
import com.android.build.gradle.api.AndroidSourceSet;
import com.android.build.gradle.api.BaseVariant;
import com.google.common.collect.Iterables;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.internal.file.UnionFileTree;
import org.gradle.api.internal.jvm.ClassDirectoryBinaryNamingScheme;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSet;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Lars Grefer
 */
public abstract class SourceSetBasedCodeQualityPlugin<T extends Task> extends AbstractAndroidCodeQualityPlugin<T, SourceSetBasedCodeQualityExtension> {

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);
        configureSourceSetRule();
        configureCheckTaskDependents();
    }

    @Override
    protected String getExtensionElementsName() {
        return "sourceSets";
    }

    @Override
    protected Callable<Collection<?>> getExtensionElementsCallable() {
        return () -> getAndroidExtension().getSourceSets();
    }

    private void configureSourceSetRule() {
        configureForSourceSets(getAndroidExtension().getSourceSets());
    }

    private void configureForSourceSets(NamedDomainObjectContainer<AndroidSourceSet> sourceSets) {
        sourceSets.all(sourceSet -> {
            Task task = project.getTasks().create(getTaskName(sourceSet, getTaskBaseName(), null), getCastedTaskType());
            task.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
            configureForSourceSet(sourceSet, (T)task);
        });
    }

    private void configureCheckTaskDependents() {
        final String taskBaseName = getTaskBaseName();
        project.getTasks().getByName("check").dependsOn((Callable) () -> Iterables.transform(extension.getSourceSets(), sourceSet -> getTaskName(sourceSet, taskBaseName, null)));
    }

    protected abstract void configureForSourceSet(AndroidSourceSet sourceSet, T task);

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
        Stream<BaseVariant> testVariants = Stream.concat(getTestVariants().stream(), getUnitTestVariants().stream());
        Stream<BaseVariant> variants = Stream.concat(getAndroidVariants().stream(), testVariants);

        FileTreeInternal[] sourceTrees = variants
                .filter(variant -> variant.getJavaCompile() != null)
                .filter(variant -> variant.getSourceSets().contains(androidSourceSet))
                .map(variant -> variant.getJavaCompile().getDestinationDir())
                .map(outputDir -> (FileTreeInternal) getProject().fileTree(outputDir))
                .toArray(FileTreeInternal[]::new);
        return new UnionFileTree(sourceTrees);
    }
}
