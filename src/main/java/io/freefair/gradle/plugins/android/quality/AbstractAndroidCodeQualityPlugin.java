package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.api.AndroidSourceSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Callables;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.ReportingBasePlugin;
import org.gradle.api.reporting.ReportingExtension;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Copy of {@link org.gradle.api.plugins.quality.internal.AbstractCodeQualityPlugin} which
 * uses {@link AndroidSourceSet AndroidSourceSets} instead of {@link org.gradle.api.tasks.SourceSet JavaSourceSets}
 *
 * @see org.gradle.api.plugins.quality.internal.AbstractCodeQualityPlugin
 */
public abstract class AbstractAndroidCodeQualityPlugin<T extends Task> extends AndroidCodeQualityHelper {

    protected static ConventionMapping conventionMappingOf(Object object) {
        return ((IConventionAware) object).getConventionMapping();
    }

    protected ProjectInternal project;
    protected AndroidCodeQualityExtension extension;

    @Override
    public final void apply(Project project) {
        super.apply(project);
        this.project = (ProjectInternal) project;

        beforeApply();
        project.getPluginManager().apply(ReportingBasePlugin.class);
        createConfigurations();
        extension = createExtension();
        configureExtensionRule();
        configureTaskRule();
        configureSourceSetRule();
        configureCheckTask();
    }

    protected abstract String getToolName();

    protected abstract Class<T> getTaskType();

    private Class<? extends Task> getCastedTaskType() {
        return getTaskType();
    }

    protected String getTaskBaseName() {
        return getToolName().toLowerCase();
    }

    protected String getConfigurationName() {
        return getToolName().toLowerCase();
    }

    protected String getReportName() {
        return getToolName().toLowerCase();
    }

    protected Class<? extends Plugin> getBasePlugin() {
        return JavaBasePlugin.class;
    }

    protected void beforeApply() {
    }

    protected void createConfigurations() {
        Configuration configuration = project.getConfigurations().create(getConfigurationName());
        configuration.setVisible(false);
        configuration.setTransitive(true);
        configuration.setDescription("The " + getToolName() + " libraries to be used for this project.");
        // Don't need these things, they're provided by the runtime
        configuration.exclude(excludeProperties("ant", "ant"));
        configuration.exclude(excludeProperties("org.apache.ant", "ant"));
        configuration.exclude(excludeProperties("org.apache.ant", "ant-launcher"));
        configuration.exclude(excludeProperties("org.slf4j", "slf4j-api"));
        configuration.exclude(excludeProperties("org.slf4j", "jcl-over-slf4j"));
        configuration.exclude(excludeProperties("org.slf4j", "log4j-over-slf4j"));
        configuration.exclude(excludeProperties("commons-logging", "commons-logging"));
        configuration.exclude(excludeProperties("log4j", "log4j"));
    }

    private Map<String, String> excludeProperties(String group, String module) {
        return ImmutableMap.<String, String>builder()
                .put("group", group)
                .put("module", module)
                .build();
    }

    protected abstract AndroidCodeQualityExtension createExtension();

    private void configureExtensionRule() {
        final ConventionMapping extensionMapping = conventionMappingOf(extension);
        extensionMapping.map("sourceSets", Callables.returning(new ArrayList()));
        extensionMapping.map("reportsDir", () -> project.getExtensions().getByType(ReportingExtension.class).file(getReportName()));
        withBasePlugin(plugin -> extensionMapping.map("sourceSets", () -> getAndroidExtension().getSourceSets()));
    }

    private void configureTaskRule() {
        project.getTasks().withType(getCastedTaskType(), (Action<Task>) task -> {
            String prunedName = task.getName().replaceFirst(getTaskBaseName(), "");
            if (prunedName.isEmpty()) {
                prunedName = task.getName();
            }
            prunedName = ("" + prunedName.charAt(0)).toLowerCase() + prunedName.substring(1);
            configureTaskDefaults((T) task, prunedName);
        });
    }

    protected abstract void configureTaskDefaults(T task, String baseName);

    private void configureSourceSetRule() {
        withBasePlugin(plugin -> configureForSourceSets(getAndroidExtension().getSourceSets()));
    }

    private void configureForSourceSets(NamedDomainObjectContainer<AndroidSourceSet> sourceSets) {
        sourceSets.all(sourceSet -> {
            Task task = project.getTasks().create(getTaskName(sourceSet, getTaskBaseName(), null), getCastedTaskType());
            configureForSourceSet(sourceSet, (T)task);
        });
    }

    protected abstract void configureForSourceSet(AndroidSourceSet sourceSet, T task);

    private void configureCheckTask() {
        withBasePlugin(plugin -> configureCheckTaskDependents());
    }

    private void configureCheckTaskDependents() {
        final String taskBaseName = getTaskBaseName();
        project.getTasks().getByName("check").dependsOn((Callable) () -> Iterables.transform(extension.getSourceSets(), sourceSet -> getTaskName(sourceSet, taskBaseName, null)));
    }

    protected void withBasePlugin(Action<Plugin> action) {
        project.getPlugins().withType(getBasePlugin(), action);
    }

    protected JavaPluginConvention getJavaPluginConvention() {
        return project.getConvention().getPlugin(JavaPluginConvention.class);
    }
}