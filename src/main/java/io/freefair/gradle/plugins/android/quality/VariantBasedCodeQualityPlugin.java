package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.TestedExtension;
import com.android.build.gradle.api.BaseVariant;
import com.google.common.collect.Iterables;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.jvm.ClassDirectoryBinaryNamingScheme;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * @author Lars Grefer
 */
public abstract class VariantBasedCodeQualityPlugin<T extends Task> extends AbstractAndroidCodeQualityPlugin<T, VariantBasedCodeQualityExtension> {

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);
        configureVariantRule();
        configureCheckTaskDependents();
    }

    @Override
    protected String getExtensionElementsName() {
        return "variants";
    }

    @Override
    protected Callable<Collection<?>> getExtensionElementsCallable() {
        return () -> {
            ArrayList<BaseVariant> objects = new ArrayList<>();
            objects.addAll(getAndroidVariants());
            objects.addAll(getTestVariants());
            objects.addAll(getUnitTestVariants());
            return objects;
        };
    }

    private void configureVariantRule() {
        configureForVariants(getAndroidVariants());
        configureForVariants(getTestVariants());
        configureForVariants(getUnitTestVariants());
    }

    private void configureForVariants(DomainObjectSet<? extends BaseVariant> variants) {
        variants.all(sourceSet -> {
            T task = project.getTasks().create(getTaskName(sourceSet, getTaskBaseName(), null), getCastedTaskType());
            task.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
            configureForVariant(sourceSet, task);
        });
    }

    private void configureCheckTaskDependents() {
        final String taskBaseName = getTaskBaseName();
        project.getTasks().getByName("check").dependsOn((Callable) () -> Iterables.transform(extension.getVariants(), variant -> getTaskName(variant, taskBaseName, null)));
    }

    protected abstract void configureForVariant(BaseVariant sourceSet, T task);

    /**
     * Returns the name of a task for this source set.
     *
     * @param verb The action, may be null.
     * @param target The target, may be null
     * @return The task name, generally of the form ${verb}${name}${noun}
     * @see SourceSet#getTaskName(String, String)
     */
    protected static String getTaskName(BaseVariant variant, String verb, String target) {
        return new ClassDirectoryBinaryNamingScheme(variant.getName()).getTaskName(verb, target);
    }

    /**
     * Returns the classpath used to compile this source.
     *
     * @return The classpath. Never returns null.
     * @see SourceSet#getCompileClasspath()
     */
    protected FileCollection getCompileClasspath(BaseVariant variant) {
        return getJavaCompile(variant).getClasspath();
    }

    /**
     * All Java source files for this source set. This includes, for example, source which is directly compiled, and
     * source which is indirectly compiled through joint compilation.
     *
     * @return the Java source. Never returns null.
     * @see SourceSet#getAllJava()
     */
    protected static FileTree getAllJava(BaseVariant androidSourceSet) {
        return getJavaCompile(androidSourceSet).getSource();
    }

    /**
     * @see SourceSet#getOutput()
     */
    protected FileTree getOutput(BaseVariant androidSourceSet) {
        return getProject().fileTree(getJavaCompile(androidSourceSet).getDestinationDir());
    }

    static JavaCompile getJavaCompile(BaseVariant variant) {
        Task javaCompiler = variant.getJavaCompiler();
        if (javaCompiler instanceof JavaCompile) {
            return (JavaCompile) javaCompiler;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
