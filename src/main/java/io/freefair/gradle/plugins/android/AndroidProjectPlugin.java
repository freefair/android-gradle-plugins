package io.freefair.gradle.plugins.android;

import com.android.build.gradle.*;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.TestVariant;
import com.android.build.gradle.api.UnitTestVariant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.jvm.ClassDirectoryBinaryNamingScheme;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static lombok.AccessLevel.PRIVATE;

/**
 * Abstract base class for all android related {@link Plugin plugins}.
 * Provides type-safe access to the {@link BaseExtension android}-extension
 * and the {@link BaseVariant variant} collection.
 */
@Getter
public abstract class AndroidProjectPlugin implements Plugin<Project> {

    @Nullable
    private TestedExtension androidExtension;

    @Nullable
    private ProjectType projectType;

    private Project project;

    @Getter(PRIVATE)
    private boolean withAndroidCalled = false;

    @Getter(PRIVATE)
    private boolean applyCalled = false;

    @Override
    public void apply(@Nonnull Project project) {
        this.project = project;
        applyCalled = true;

        project.getPlugins().withType(AppPlugin.class, appPlugin -> {
            androidExtension = project.getExtensions().getByType(AppExtension.class);
            projectType = ProjectType.APP;

            withAndroid(androidExtension);
            if (!isWithAndroidCalled()) {
                throw new RuntimeException("call super() in withAndroid()");
            }
        });

        project.getPlugins().withType(LibraryPlugin.class, libraryPlugin -> {
            androidExtension = project.getExtensions().getByType(LibraryExtension.class);
            projectType = ProjectType.LIBRARY;

            withAndroid(androidExtension);
            if (!isWithAndroidCalled()) {
                throw new RuntimeException("call super() in withAndroid()");
            }
        });

        project.getPlugins().withType(FeaturePlugin.class, atomPlugin -> {
            androidExtension = project.getExtensions().getByType(FeatureExtension.class);
            projectType = ProjectType.FEATURE;

            withAndroid(androidExtension);
            if (!isWithAndroidCalled()) {
                throw new RuntimeException("call super() in withAndroid()");
            }
        });

        project.afterEvaluate(project1 -> {
            if (androidExtension == null || projectType == null)
                project1.getLogger().warn("No android plugin found on project {}", project);
        });
    }

    protected void withAndroid(TestedExtension extension) {
        withAndroidCalled = true;
    }

    @SuppressWarnings("WeakerAccess")
    public TestedExtension getAndroidExtension() {
        if (androidExtension == null || projectType == null) {
            if (isApplyCalled()) {
                throw new IllegalStateException("No android plugin found");
            }
            else {
                throw new IllegalArgumentException("super.apply() not called");
            }
        }

        return androidExtension;
    }

    @SuppressWarnings("WeakerAccess")
    public DomainObjectSet<? extends BaseVariant> getAndroidVariants() {
        if (androidExtension == null || projectType == null) {
            throw new IllegalStateException("No android plugin found");
        }

        switch (projectType) {
            case APP:
                return ((AppExtension) androidExtension).getApplicationVariants();
            case LIBRARY:
                return ((LibraryExtension) androidExtension).getLibraryVariants();
            case FEATURE:
                return ((FeatureExtension) androidExtension).getFeatureVariants();
            default:
                throw new IllegalStateException("Unexpected project type: " + projectType);
        }
    }

    @Nonnull
    public DomainObjectSet<TestVariant> getTestVariants() {
        if (androidExtension == null || projectType == null) {
            throw new IllegalStateException("No android plugin found");
        }

        return androidExtension.getTestVariants();
    }

    @Nonnull
    public DomainObjectSet<UnitTestVariant> getUnitTestVariants() {
        if (androidExtension == null || projectType == null) {
            throw new IllegalStateException("No android plugin found");
        }

        return androidExtension.getUnitTestVariants();
    }

    protected boolean publishVariant(BaseVariant variant) {
        if (variant instanceof TestVariant || variant instanceof UnitTestVariant) {
            return false;
        }

        return getAndroidExtension().getDefaultPublishConfig().equals(variant.getName());
    }

    /**
     * Returns the name of a task for this source set.
     *
     * @param verb   The action, may be null.
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
    protected Provider<FileCollection> getCompileClasspath(BaseVariant variant) {
        return variant.getJavaCompileProvider().map(JavaCompile::getClasspath);
    }

    /**
     * All Java source files for this source set. This includes, for example, source which is directly compiled, and
     * source which is indirectly compiled through joint compilation.
     *
     * @return the Java source. Never returns null.
     * @see SourceSet#getAllJava()
     */
    protected static Provider<FileTree> getAllJava(BaseVariant androidSourceSet) {
        return androidSourceSet.getJavaCompileProvider().map(JavaCompile::getSource);
    }

    /**
     * @see SourceSet#getOutput()
     */
    protected Provider<FileTree> getOutput(BaseVariant androidSourceSet) {
        return androidSourceSet.getJavaCompileProvider()
                .map(AbstractCompile::getDestinationDir)
                .map(dir -> getProject().fileTree(dir));
    }

    @Getter
    @AllArgsConstructor
    public enum ProjectType {
        APP(AppPlugin.class, AppExtension.class),
        LIBRARY(LibraryPlugin.class, LibraryExtension.class),
        FEATURE(FeaturePlugin.class, FeatureExtension.class);

        private Class<? extends BasePlugin<?>> pluginClass;
        private Class<? extends TestedExtension> extensionClass;
    }
}
