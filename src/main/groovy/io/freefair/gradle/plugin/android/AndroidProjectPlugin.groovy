package io.freefair.gradle.plugin.android

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Nullable
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.DefaultDomainObjectSet

abstract class AndroidProjectPlugin implements Plugin<Project> {

    @Nullable
    protected LibraryExtension libraryExtension;
    @Nullable
    protected AppExtension appExtension;

    private boolean isLibrary

    @Override
    void apply(Project project) {

        if (project.android instanceof LibraryExtension) {
            isLibrary = true;
            libraryExtension = project.android;
        } else if (project.android instanceof AppExtension) {
            isLibrary = false;
            appExtension = project.android;
        }

    }

    protected TestedExtension getAndroidExtension() {
        if (isLibrary)
            return libraryExtension;
        else
            return appExtension;
    }

    protected DefaultDomainObjectSet<BaseVariant> getAndroidVariants() {
        if (isLibrary)
            return libraryExtension.libraryVariants;
        else
            return appExtension.applicationVariants;
    }

    protected boolean publishVariant(BaseVariant variant) {
        if (isLibrary) {
            return libraryExtension.publishNonDefault || libraryExtension.defaultPublishConfig.equals(variant.name)
        } else {
            return appExtension.publishNonDefault || appExtension.defaultPublishConfig.equals(variant.name)
        }
    }
}