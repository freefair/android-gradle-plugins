package io.freefair.gradle.plugins.android;

import com.android.build.gradle.*;
import com.android.build.gradle.api.BaseVariant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Nullable;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static lombok.AccessLevel.PRIVATE;

/**
 * Abstract base class for all android related {@link Plugin plugins}.
 * Provides type-safe access to the {@link BaseExtension android}-extension
 * and the {@link BaseVariant variant} collection.
 */
@Getter
public abstract class AndroidProjectPlugin implements Plugin<Project> {

    @Nullable
    private LibraryExtension libraryExtension;

    @Nullable
    private AppExtension appExtension;

    @Nullable
    private TestExtension testExtension;

    @Nullable
    private ProjectType projectType;

    private Project project;

    @Getter(PRIVATE)
    private boolean withAndroidCalled = false;

    @Override
    public void apply(Project project) {
        this.project = project;

        project.getPlugins().withType(AppPlugin.class, appPlugin -> {
            appExtension = project.getExtensions().getByType(AppExtension.class);
            projectType = ProjectType.APP;

            withAndroid(appExtension);
            if (!isWithAndroidCalled()) {
                throw new RuntimeException("call super() in withAndroid()");
            }
        });

        project.getPlugins().withType(LibraryPlugin.class, libraryPlugin -> {
            libraryExtension = project.getExtensions().getByType(LibraryExtension.class);
            projectType = ProjectType.LIBRARY;

            withAndroid(libraryExtension);
            if (!isWithAndroidCalled()) {
                throw new RuntimeException("call super() in withAndroid()");
            }
        });

        project.getPlugins().withType(TestPlugin.class, testPlugin -> {
            testExtension = project.getExtensions().getByType(TestExtension.class);
            projectType = ProjectType.TEST;

            withAndroid(testExtension);
            if (!isWithAndroidCalled()) {
                throw new RuntimeException("call super() in withAndroid()");
            }
        });

        project.afterEvaluate(project1 -> {
            if (projectType == null)
                project1.getLogger().warn("No android plugin found on project {}", project);
        });
    }

    protected void withAndroid(BaseExtension extension) {
        withAndroidCalled = true;
    }

    @SuppressWarnings("WeakerAccess")
    public BaseExtension getAndroidExtension() {
        if (projectType == null) {
            throw new IllegalStateException("No android plugin found");
        }

        switch (projectType) {
            case APP:
                return appExtension;
            case LIBRARY:
                return libraryExtension;
            case TEST:
                return testExtension;
            default:
                throw new IllegalStateException("Unexpected project type: " + projectType);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public DomainObjectSet<? extends BaseVariant> getAndroidVariants() {
        if (projectType == null) {
            throw new IllegalStateException("No android plugin found");
        }

        switch (projectType) {
            case APP:
                return appExtension.getApplicationVariants();
            case LIBRARY:
                return libraryExtension.getLibraryVariants();
            case TEST:
                return testExtension.getApplicationVariants();
            default:
                throw new IllegalStateException("Unexpected project type: " + projectType);
        }
    }

    protected boolean publishVariant(BaseVariant variant) {
        return getAndroidExtension().getPublishNonDefault() || getAndroidExtension().getDefaultPublishConfig().equals(variant.getName());
    }

    @Getter
    @AllArgsConstructor
    public enum ProjectType {
        APP(AppPlugin.class, AppExtension.class),
        LIBRARY(LibraryPlugin.class, LibraryExtension.class),
        TEST(TestPlugin.class, TestExtension.class);

        private Class<? extends BasePlugin> pluginClass;
        private Class<? extends BaseExtension> extensionClass;

    }
}
