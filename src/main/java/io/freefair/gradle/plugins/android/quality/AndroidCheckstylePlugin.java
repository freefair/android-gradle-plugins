package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.api.AndroidSourceSet;
import com.google.common.util.concurrent.Callables;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstylePlugin;

import java.io.File;

/**
 * Copy of {@link org.gradle.api.plugins.quality.CheckstylePlugin} which
 * <ul>
 * <li>extends {@link AbstractAndroidCodeQualityPlugin} instead of {@link org.gradle.api.plugins.quality.internal.AbstractCodeQualityPlugin}</li>
 * <li>uses {@link AndroidSourceSet AndroidSourceSets} instead of {@link org.gradle.api.tasks.SourceSet JavaSourceSets}</li>
 * </ul>
 *
 * @see org.gradle.api.plugins.quality.CheckstylePlugin
 * @see AbstractAndroidCodeQualityPlugin
 */
public class AndroidCheckstylePlugin extends SourceSetBasedCodeQualityPlugin<Checkstyle> {

    public static final String DEFAULT_CHECKSTYLE_VERSION = CheckstylePlugin.DEFAULT_CHECKSTYLE_VERSION;
    private AndroidCheckstyleExtension extension;

    @Override
    protected String getToolName() {
        return "Checkstyle";
    }

    @Override
    protected Class<Checkstyle> getTaskType() {
        return Checkstyle.class;
    }

    @Override
    protected SourceSetBasedCodeQualityExtension createExtension() {
        extension = project.getExtensions().create("checkstyle", AndroidCheckstyleExtension.class, project);
        extension.setToolVersion(DEFAULT_CHECKSTYLE_VERSION);
        extension.setConfig(project.getResources().getText().fromFile("config/checkstyle/checkstyle.xml"));
        return extension;
    }

    @Override
    protected void configureTaskDefaults(Checkstyle task, final String baseName) {
        Configuration configuration = project.getConfigurations().getAt("checkstyle");
        configureDefaultDependencies(configuration);
        configureTaskConventionMapping(configuration, task);
        configureReportsConventionMapping(task, baseName);
    }

    private void configureDefaultDependencies(Configuration configuration) {
        configuration.defaultDependencies(dependencies -> dependencies.add(project.getDependencies().create("com.puppycrawl.tools:checkstyle:" + extension.getToolVersion())));
    }

    private void configureTaskConventionMapping(Configuration configuration, Checkstyle task) {
        ConventionMapping taskMapping = task.getConventionMapping();
        taskMapping.map("checkstyleClasspath", Callables.returning(configuration));
        taskMapping.map("config", () -> extension.getConfig());
        taskMapping.map("configProperties", () -> extension.getConfigProperties());
        taskMapping.map("ignoreFailures", () -> extension.isIgnoreFailures());
        taskMapping.map("showViolations", () -> extension.isShowViolations());
    }

    private void configureReportsConventionMapping(Checkstyle task, final String baseName) {
        task.getReports().all(report -> {
            ConventionMapping reportMapping = conventionMappingOf(report);
            reportMapping.map("enabled", Callables.returning(true));
            reportMapping.map("destination", () -> new File(extension.getReportsDir(), baseName + "." + report.getName()));
        });
    }

    @Override
    protected void configureForSourceSet(final AndroidSourceSet sourceSet, Checkstyle task) {
        task.setDescription("Run Checkstyle analysis for " + sourceSet.getName() + " classes");
        task.setClasspath(getCompileClasspath(sourceSet));
        task.setSource(getAllJava(sourceSet));
    }
}

