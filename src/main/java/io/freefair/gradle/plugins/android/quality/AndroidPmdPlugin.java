package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.api.AndroidSourceSet;
import com.google.common.util.concurrent.Callables;
import org.gradle.api.Incubating;
import org.gradle.api.JavaVersion;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.plugins.quality.Pmd;
import org.gradle.api.plugins.quality.PmdPlugin;
import org.gradle.api.plugins.quality.TargetJdk;
import org.gradle.api.resources.TextResource;
import org.gradle.util.VersionNumber;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Copy of {@link org.gradle.api.plugins.quality.PmdPlugin} which
 * <ul>
 * <li>extends {@link AbstractAndroidCodeQualityPlugin} instead of {@link org.gradle.api.plugins.quality.internal.AbstractCodeQualityPlugin}</li>
 * <li>uses {@link AndroidSourceSet AndroidSourceSets} instead of {@link org.gradle.api.tasks.SourceSet JavaSourceSets}</li>
 * </ul>
 *
 * @see org.gradle.api.plugins.quality.PmdPlugin
 * @see AbstractAndroidCodeQualityPlugin
 */
@Incubating
public class AndroidPmdPlugin extends SourceSetBasedCodeQualityPlugin<Pmd> {

    private AndroidPmdExtension extension;

    @Override
    protected String getToolName() {
        return "PMD";
    }

    @Override
    protected Class<Pmd> getTaskType() {
        return Pmd.class;
    }

    @Override
    protected SourceSetBasedCodeQualityExtension createExtension() {
        extension = project.getExtensions().create("pmd", AndroidPmdExtension.class, project);
        extension.setToolVersion(PmdPlugin.DEFAULT_PMD_VERSION);
        extension.setRuleSets(new ArrayList<String>(Arrays.asList("category/java/errorprone.xml")));
        extension.setRuleSetFiles(project.getLayout().files());
        conventionMappingOf(extension).map("targetJdk", () ->
                getDefaultTargetJdk(getJavaPluginConvention().getSourceCompatibility()));
        return extension;
    }

    public TargetJdk getDefaultTargetJdk(JavaVersion javaVersion) {
        try {
            return TargetJdk.toVersion(javaVersion.toString());
        } catch (IllegalArgumentException ignored) {
            // TargetJDK does not include 1.1, 1.2 and 1.8;
            // Use same fallback as PMD
            return TargetJdk.VERSION_1_4;
        }
    }

    @Override
    protected void configureConfiguration(Configuration configuration) {
        configureDefaultDependencies(configuration);
    }

    @Override
    protected void configureTaskDefaults(Pmd task, String baseName) {
        Configuration configuration = project.getConfigurations().getAt(getConfigurationName());
        configureTaskConventionMapping(configuration, task);
        configureReportsConventionMapping(task, baseName);
    }

    private void configureDefaultDependencies(Configuration configuration) {
        configuration.defaultDependencies(dependencies -> {
            VersionNumber version = VersionNumber.parse(extension.getToolVersion());
            String dependency = calculateDefaultDependencyNotation(version);
            dependencies.add(project.getDependencies().create(dependency));
        });
    }

    private void configureTaskConventionMapping(Configuration configuration, Pmd task) {
        ConventionMapping taskMapping = task.getConventionMapping();
        taskMapping.map("pmdClasspath", Callables.returning(configuration));
        taskMapping.map("ruleSets", (Callable<List<String>>) () -> extension.getRuleSets());
        taskMapping.map("ruleSetConfig", (Callable<TextResource>) () -> extension.getRuleSetConfig());
        taskMapping.map("ruleSetFiles", (Callable<FileCollection>) () -> extension.getRuleSetFiles());
        taskMapping.map("ignoreFailures", (Callable<Boolean>) () -> extension.isIgnoreFailures());
        taskMapping.map("rulePriority", (Callable<Integer>) () -> extension.getRulePriority());
        taskMapping.map("consoleOutput", (Callable<Boolean>) () -> extension.isConsoleOutput());
        taskMapping.map("targetJdk", (Callable<TargetJdk>) () -> extension.getTargetJdk());

        task.getMaxFailures().convention(extension.getMaxFailures());
        task.getIncrementalAnalysis().convention(extension.getIncrementalAnalysis());
    }

    private void configureReportsConventionMapping(Pmd task, final String baseName) {
        task.getReports().all(report -> {
            report.getRequired().convention(true);
            report.getOutputLocation().convention(project.getLayout().getProjectDirectory().file(project.provider(() -> new File(extension.getReportsDir(), baseName + "." + report.getName()).getAbsolutePath())));
        });
    }

    private String calculateDefaultDependencyNotation(VersionNumber toolVersion) {
        if (toolVersion.compareTo(VersionNumber.version(5)) < 0) {
            return "pmd:pmd:" + extension.getToolVersion();
        } else if (toolVersion.compareTo(VersionNumber.parse("5.2.0")) < 0) {
            return "net.sourceforge.pmd:pmd:" + extension.getToolVersion();
        }
        return "net.sourceforge.pmd:pmd-java:" + extension.getToolVersion();
    }

    @Override
    protected void configureForSourceSet(final AndroidSourceSet sourceSet, final Pmd task) {
        task.setDescription("Run PMD analysis for " + sourceSet.getName() + " classes");
        task.setSource(getAllJava(sourceSet));
        ConventionMapping taskMapping = task.getConventionMapping();
        taskMapping.map("classpath", () -> getOutput(sourceSet).plus(getCompileClasspath(sourceSet)));
    }
}
