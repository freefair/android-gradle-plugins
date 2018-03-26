package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.api.AndroidSourceSet;
import com.android.build.gradle.api.BaseVariant;
import com.google.common.util.concurrent.Callables;
import org.gradle.api.Incubating;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.plugins.quality.FindBugs;
import org.gradle.api.plugins.quality.FindBugsPlugin;
import org.gradle.api.resources.TextResource;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

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
        extension.setToolVersion(FindBugsPlugin.DEFAULT_FINDBUGS_VERSION);
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
        taskMapping.map("ignoreFailures", (Callable<Boolean>) () -> extension.isIgnoreFailures());
        taskMapping.map("effort", (Callable<String>) () -> extension.getEffort());
        taskMapping.map("reportLevel", (Callable<String>) () -> extension.getReportLevel());
        taskMapping.map("visitors", (Callable<Collection<String>>) () -> extension.getVisitors());
        taskMapping.map("omitVisitors", (Callable<Collection<String>>) () -> extension.getOmitVisitors());

        taskMapping.map("excludeFilterConfig", (Callable<TextResource>) () -> extension.getExcludeFilterConfig());
        taskMapping.map("includeFilterConfig", (Callable<TextResource>) () -> extension.getIncludeFilterConfig());
        taskMapping.map("excludeBugsFilterConfig", (Callable<TextResource>) () -> extension.getExcludeBugsFilterConfig());

        taskMapping.map("extraArgs", (Callable<Collection<String>>) () -> extension.getExtraArgs());
        taskMapping.map("jvmArgs", (Callable<Collection<String>>) () -> extension.getJvmArgs());
        taskMapping.map("showProgress", (Callable<Boolean>) () -> extension.isShowProgress());
    }

    private void configureReportsConventionMapping(FindBugs task, final String baseName) {
        task.getReports().all(report -> {
            ConventionMapping reportMapping = conventionMappingOf(report);
            reportMapping.map("enabled", (Callable<Boolean>) () -> report.getName().equals("xml"));
            reportMapping.map("destination", (Callable<File>) () -> new File(extension.getReportsDir(), baseName + "." + report.getName()));
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

            List<String> generatedClasses = new LinkedList<>();

            variant.getJavaCompile().getSource().visit(fileVisitDetails -> {
                if (!fileVisitDetails.isDirectory() && fileVisitDetails.getPath().endsWith(".java") && fileVisitDetails.getFile().getAbsolutePath().startsWith(project.getBuildDir().getAbsolutePath())) {
                    generatedClasses.add(fileVisitDetails.getPath().replace(".java", ""));
                }
            });

            return getOutput(variant)
                    .filter(file -> generatedClasses.parallelStream().noneMatch(generatedClass -> file.getAbsolutePath().endsWith(generatedClass + ".class") || file.getAbsolutePath().contains(generatedClass + "$")));
        });
        taskMapping.map("classpath", () -> getCompileClasspath(variant));
    }
}
