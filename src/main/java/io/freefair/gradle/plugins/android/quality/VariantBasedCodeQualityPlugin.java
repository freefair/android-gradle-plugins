package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.TestedExtension;
import com.android.build.gradle.api.BaseVariant;
import com.google.common.collect.Iterables;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;

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
}
