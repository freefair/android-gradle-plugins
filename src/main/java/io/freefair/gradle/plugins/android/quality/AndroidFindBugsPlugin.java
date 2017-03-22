package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.api.AndroidSourceSet;
import com.android.build.gradle.api.BaseVariant;
import com.google.common.util.concurrent.Callables;
import org.gradle.api.Incubating;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.plugins.quality.FindBugs;
import org.gradle.api.plugins.quality.FindBugsPlugin;

import java.io.File;

/**
 * Copy of {@link org.gradle.api.plugins.quality.FindBugsPlugin} which
 * <ul>
 * <li>extends {@link AbstractAndroidCodeQualityPlugin} instead of {@link org.gradle.api.plugins.quality.internal.AbstractCodeQualityPlugin}</li>
 * <li>uses {@link AndroidSourceSet AndroidSourceSets} instead of {@link org.gradle.api.tasks.SourceSet JavaSourceSets}</li>
 * </ul>
 *
 * @see org.gradle.api.plugins.quality.FindBugsPlugin
 * @see AbstractAndroidCodeQualityPlugin
 */
@Incubating
public class AndroidFindBugsPlugin extends VariantBasedCodeQualityPlugin<FindBugs> {

    public static final String DEFAULT_FINDBUGS_VERSION = FindBugsPlugin.DEFAULT_FINDBUGS_VERSION;
    private AndroidFindBugsExtension extension;

    @Override
    protected String getToolName() {
        return "FindBugs";
    }

    @Override
    protected Class<FindBugs> getTaskType() {
        return FindBugs.class;
    }

    @Override
    protected void beforeApply() {
        configureFindBugsConfigurations();
    }

    private void configureFindBugsConfigurations() {
        Configuration configuration = project.getConfigurations().create("findbugsPlugins");
        configuration.setVisible(false);
        configuration.setTransitive(true);
        configuration.setDescription("The FindBugs plugins to be used for this project.");
    }

    @Override
    protected VariantBasedCodeQualityExtension createExtension() {
        extension = project.getExtensions().create("findbugs", AndroidFindBugsExtension.class, project);
        extension.setToolVersion(DEFAULT_FINDBUGS_VERSION);
        return extension;
    }

    @Override
    protected void configureTaskDefaults(FindBugs task, String baseName) {
        task.setPluginClasspath(project.getConfigurations().getAt("findbugsPlugins"));
        Configuration configuration = project.getConfigurations().getAt("findbugs");
        configureDefaultDependencies(configuration);
        configureTaskConventionMapping(configuration, task);
        configureReportsConventionMapping(task, baseName);
    }

    private void configureDefaultDependencies(Configuration configuration) {
        configuration.defaultDependencies(dependencies -> dependencies.add(project.getDependencies().create("com.google.code.findbugs:findbugs:" + extension.getToolVersion())));
    }

    private void configureTaskConventionMapping(Configuration configuration, FindBugs task) {
        ConventionMapping taskMapping = task.getConventionMapping();
        taskMapping.map("findbugsClasspath", Callables.returning(configuration));
        taskMapping.map("ignoreFailures", () -> extension.isIgnoreFailures());
        taskMapping.map("effort", () -> extension.getEffort());
        taskMapping.map("reportLevel", () -> extension.getReportLevel());
        taskMapping.map("visitors", () -> extension.getVisitors());
        taskMapping.map("omitVisitors", () -> extension.getOmitVisitors());

        taskMapping.map("excludeFilterConfig", () -> extension.getExcludeFilterConfig());
        taskMapping.map("includeFilterConfig", () -> extension.getIncludeFilterConfig());
        taskMapping.map("excludeBugsFilterConfig", () -> extension.getExcludeBugsFilterConfig());

        taskMapping.map("extraArgs", () -> extension.getExtraArgs());
    }

    private void configureReportsConventionMapping(FindBugs task, final String baseName) {
        task.getReports().all(report -> {
            ConventionMapping reportMapping = conventionMappingOf(report);
            reportMapping.map("enabled", () -> report.getName().equals("xml"));
            reportMapping.map("destination", () -> new File(extension.getReportsDir(), baseName + "." + report.getName()));
        });
    }

    @Override
    protected void configureForVariant(final BaseVariant variant, FindBugs task) {
        task.setDescription("Run FindBugs analysis for " + variant.getName() + " classes");
        task.setSource(getAllJava(variant));
        task.dependsOn(variant.getJavaCompile());
        ConventionMapping taskMapping = task.getConventionMapping();
        taskMapping.map("classes", () -> {
            // the simple "classes = sourceSet.output" may lead to non-existing resources directory
            // being passed to FindBugs Ant task, resulting in an error
            return getOutput(variant)
                    .filter(f -> !f.getName().matches("R(\\$.*)?\\.class"))
                    .filter(f -> !f.getName().equals("BuildConfig.class"));
        });
        taskMapping.map("classpath", () -> getCompileClasspath(variant));
    }
}
