package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.api.AndroidSourceSet;
import com.google.common.util.concurrent.Callables;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.Directory;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.provider.Provider;
import org.gradle.api.resources.TextResource;
import org.gradle.util.DeprecationLogger;

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
        extension.getConfigDirectory().set(determineConfigurationDirectory());
        extension.setConfig(project.getResources().getText().fromFile((Callable<File>) () -> new File(extension.getConfigDir(), "checkstyle.xml")));
        return extension;
    }

    private Provider<Directory> determineConfigurationDirectory() {
        return project.provider(() -> {
            if (usesSubprojectCheckstyleConfiguration()) {
                DeprecationLogger.nagUserWithDeprecatedIndirectUserCodeCause("Setting the Checkstyle configuration file under 'config/checkstyle' of a sub project", "Use the root project's 'config/checkstyle' directory instead.");
                return project.getLayout().getProjectDirectory().dir(CONFIG_DIR_NAME);
            }
            return project.getRootProject().getLayout().getProjectDirectory().dir(CONFIG_DIR_NAME);
        });
    }

    private boolean usesSubprojectCheckstyleConfiguration() {
        return !isRootProject() && project.file(CONFIG_DIR_NAME).isDirectory();
    }

    private boolean isRootProject() {
        return project.equals(project.getRootProject());
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
        configuration.defaultDependencies(dependencies -> dependencies.add(project.getDependencies().create("com.puppycrawl.tools:checkstyle:" + extension.getToolVersion())));
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

        task.setConfigDir(project.provider(() -> extension.getConfigDir()));
    }

    private void configureReportsConventionMapping(Checkstyle task, final String baseName) {
        task.getReports().all(report -> {
            ConventionMapping reportMapping = conventionMappingOf(report);
            reportMapping.map("enabled", Callables.returning(true));
            reportMapping.map("destination", (Callable<File>) () -> new File(extension.getReportsDir(), baseName + "." + report.getName()));
        });
    }

    @Override
    protected void configureForSourceSet(final AndroidSourceSet sourceSet, Checkstyle task) {
        task.setDescription("Run Checkstyle analysis for " + sourceSet.getName() + " classes");
        task.setClasspath(getOutput(sourceSet).plus(getCompileClasspath(sourceSet)));
        task.setSource(getAllJava(sourceSet));
    }
}

