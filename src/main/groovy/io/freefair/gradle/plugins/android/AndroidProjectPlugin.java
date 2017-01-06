package io.freefair.gradle.plugins.android;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.TestedExtension;
import com.android.build.gradle.api.BaseVariant;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Nullable;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

@Getter
public abstract class AndroidProjectPlugin implements Plugin<Project> {

    @Nullable
    private LibraryExtension libraryExtension;
    @Nullable
    private AppExtension appExtension;

    private boolean isLibrary;

    @Override
    public void apply(Project project) {

        Object androidExtension = project.getExtensions().getByName("android");

        if (androidExtension instanceof LibraryExtension) {
            isLibrary = true;
            libraryExtension = (LibraryExtension) androidExtension;
        } else if (androidExtension instanceof AppExtension) {
            isLibrary = false;
            appExtension = (AppExtension) androidExtension;
        }

    }

    protected TestedExtension getAndroidExtension() {
        if (isLibrary)
            return libraryExtension;
        else
            return appExtension;
    }

    protected DomainObjectSet<? extends BaseVariant> getAndroidVariants() {
        if (isLibrary)
            return libraryExtension.getLibraryVariants();
        else
            return appExtension.getApplicationVariants();
    }

    protected boolean publishVariant(BaseVariant variant) {
        if (isLibrary) {
            return libraryExtension.getPublishNonDefault() || libraryExtension.getDefaultPublishConfig().equals(variant.getName());
        } else {
            return appExtension.getPublishNonDefault() || appExtension.getDefaultPublishConfig().equals(variant.getName());
        }
    }
}
