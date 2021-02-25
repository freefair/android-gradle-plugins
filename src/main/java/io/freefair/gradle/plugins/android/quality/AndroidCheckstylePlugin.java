package io.freefair.gradle.plugins.android.quality;

import com.android.build.api.dsl.AndroidSourceSet;
import com.google.common.util.concurrent.Callables;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.resources.TextResource;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.gradle.api.plugins.quality.CheckstylePlugin.DEFAULT_CHECKSTYLE_VERSION;

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

    private static final String CONFIG_DIR_NAME = "config/checkstyle";
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
        extension.getConfigDirectory().convention(project.getRootProject().getLayout().getProjectDirectory().dir(CONFIG_DIR_NAME));
        extension.setConfig(project.getResources().getText().fromFile(extension.getConfigDirectory().file("checkstyle.xml")));
        return extension;
    }

    @Override
    protected void configureConfiguration(Configuration configuration) {
        configureDefaultDependencies(configuration);
    }

    @Override
    protected void configureTaskDefaults(Checkstyle task, final String baseName) {
        Configuration configuration = project.getConfigurations().getAt(getConfigurationName());
        configureTaskConventionMapping(configuration, task);
        configureReportsConventionMapping(task, baseName);
    }

    private void configureDefaultDependencies(Configuration configuration) {
        configuration.defaultDependencies(new Action<DependencySet>() {
            @Override
            public void execute(DependencySet dependencies) {
                dependencies.add(project.getDependencies().create("com.puppycrawl.tools:checkstyle:" + extension.getToolVersion()));
            }
        });
    }

    private void configureTaskConventionMapping(Configuration configuration, Checkstyle task) {
        ConventionMapping taskMapping = task.getConventionMapping();
        taskMapping.map("checkstyleClasspath", Callables.returning(configuration));
        taskMapping.map("config", (Callable<TextResource>) () -> extension.getConfig());
        taskMapping.map("configProperties", (Callable<Map<String, Object>>) () -> extension.getConfigProperties());
        taskMapping.map("ignoreFailures", (Callable<Boolean>) () -> extension.isIgnoreFailures());
        taskMapping.map("showViolations", (Callable<Boolean>) () -> extension.isShowViolations());
        taskMapping.map("maxErrors", (Callable<Integer>) () -> extension.getMaxErrors());
        taskMapping.map("maxWarnings", (Callable<Integer>) () -> extension.getMaxWarnings());

        task.getConfigDirectory().convention(extension.getConfigDirectory());
    }

    private void configureReportsConventionMapping(Checkstyle task, final String baseName) {
        task.getReports().all(new Action<SingleFileReport>() {
            @Override
            public void execute(final SingleFileReport report) {
                report.getRequired().convention(true);
                report.getOutputLocation().convention(project.getLayout().getProjectDirectory().file(project.provider(() -> new File(extension.getReportsDir(), baseName + "." + report.getName()).getAbsolutePath())));
            }
        });
    }

    @Override
    protected void configureForSourceSet(final AndroidSourceSet sourceSet, Checkstyle task) {
        task.setDescription("Run Checkstyle analysis for " + sourceSet.getName() + " classes");
        task.setClasspath(getOutput(sourceSet).plus(getCompileClasspath(sourceSet)));
        task.setSource(getAllJava(sourceSet));
    }
}

