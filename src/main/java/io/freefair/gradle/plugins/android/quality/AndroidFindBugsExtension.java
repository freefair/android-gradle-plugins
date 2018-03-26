package io.freefair.gradle.plugins.android.quality;

import lombok.EqualsAndHashCode;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.resources.TextResource;

import java.io.File;
import java.util.Collection;

/**
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.FindBugsExtension
 */
@EqualsAndHashCode(callSuper = true)
public class AndroidFindBugsExtension extends VariantBasedCodeQualityExtension {

    private final Project project;

    private String effort;
    private String reportLevel;
    private Collection<String> visitors;
    private Collection<String> omitVisitors;
    private TextResource includeFilterConfig;
    private TextResource excludeFilterConfig;
    private TextResource excludeBugsFilterConfig;
    private Collection<String> extraArgs;
    private Collection<String> jvmArgs;
    private boolean showProgress;

    public AndroidFindBugsExtension(Project project) {
        this.project = project;
    }

    /**
     * The analysis effort level.
     * The value specified should be one of {@code min}, {@code default}, or {@code max}.
     * Higher levels increase precision and find more bugs at the expense of running time and memory consumption.
     */
    public String getEffort() {
        return effort;
    }

    /**
     * The analysis effort level.
     * The value specified should be one of {@code min}, {@code default}, or {@code max}.
     * Higher levels increase precision and find more bugs at the expense of running time and memory consumption.
     */
    public void setEffort(String effort) {
        this.effort = effort;
    }

    /**
     * The priority threshold for reporting bugs.
     * If set to {@code low}, all bugs are reported.
     * If set to {@code medium} (the default), medium and high priority bugs are reported.
     * If set to {@code high}, only high priority bugs are reported.
     */
    public String getReportLevel() {
        return reportLevel;
    }

    /**
     * The priority threshold for reporting bugs.
     * If set to {@code low}, all bugs are reported.
     * If set to {@code medium} (the default), medium and high priority bugs are reported.
     * If set to {@code high}, only high priority bugs are reported.
     */
    public void setReportLevel(String reportLevel) {
        this.reportLevel = reportLevel;
    }

    /**
     * The bug detectors which should be run.
     * The bug detectors are specified by their class names, without any package qualification.
     * By default, all detectors which are not disabled by default are run.
     */
    public Collection<String> getVisitors() {
        return visitors;
    }

    /**
     * The bug detectors which should be run.
     * The bug detectors are specified by their class names, without any package qualification.
     * By default, all detectors which are not disabled by default are run.
     */
    public void setVisitors(Collection<String> visitors) {
        this.visitors = visitors;
    }

    /**
     * Similar to {@code visitors} except that it specifies bug detectors which should not be run.
     * By default, no visitors are omitted.
     */
    public Collection<String> getOmitVisitors() {
        return omitVisitors;
    }

    /**
     * Similar to {@code visitors} except that it specifies bug detectors which should not be run.
     * By default, no visitors are omitted.
     */
    public void setOmitVisitors(Collection<String> omitVisitors) {
        this.omitVisitors = omitVisitors;
    }

    /**
     * A filter specifying which bugs are reported. Replaces the {@code includeFilter} property.
     *
     * @since 2.2
     */
    @Incubating
    public TextResource getIncludeFilterConfig() {
        return includeFilterConfig;
    }

    /**
     * A filter specifying which bugs are reported. Replaces the {@code includeFilter} property.
     *
     * @since 2.2
     */
    @Incubating
    public void setIncludeFilterConfig(TextResource includeFilterConfig) {
        this.includeFilterConfig = includeFilterConfig;
    }

    /**
     * The filename of a filter specifying which bugs are reported.
     */
    public File getIncludeFilter() {
        TextResource includeFilterConfig = getIncludeFilterConfig();
        if (includeFilterConfig == null) {
            return null;
        }
        return includeFilterConfig.asFile();
    }

    /**
     * The filename of a filter specifying which bugs are reported.
     */
    public void setIncludeFilter(File filter) {
        setIncludeFilterConfig(project.getResources().getText().fromFile(filter));
    }

    /**
     * A filter specifying bugs to exclude from being reported. Replaces the {@code excludeFilter} property.
     *
     * @since 2.2
     */
    @Incubating
    public TextResource getExcludeFilterConfig() {
        return excludeFilterConfig;
    }

    /**
     * A filter specifying bugs to exclude from being reported. Replaces the {@code excludeFilter} property.
     *
     * @since 2.2
     */
    @Incubating
    public void setExcludeFilterConfig(TextResource excludeFilterConfig) {
        this.excludeFilterConfig = excludeFilterConfig;
    }

    /**
     * The filename of a filter specifying bugs to exclude from being reported.
     */
    public File getExcludeFilter() {
        TextResource excludeFilterConfig = getExcludeFilterConfig();
        if (excludeFilterConfig == null) {
            return null;
        }
        return excludeFilterConfig.asFile();
    }

    /**
     * The filename of a filter specifying bugs to exclude from being reported.
     */
    public void setExcludeFilter(File filter) {
        setExcludeFilterConfig(project.getResources().getText().fromFile(filter));
    }

    /**
     * A filter specifying baseline bugs to exclude from being reported.
     *
     * @since 2.4
     */
    @Incubating
    public TextResource getExcludeBugsFilterConfig() {
        return excludeBugsFilterConfig;
    }

    /**
     * A filter specifying baseline bugs to exclude from being reported.
     *
     * @since 2.4
     */
    @Incubating
    public void setExcludeBugsFilterConfig(TextResource excludeBugsFilterConfig) {
        this.excludeBugsFilterConfig = excludeBugsFilterConfig;
    }

    /**
     * The filename of a filter specifying baseline bugs to exclude from being reported.
     */
    public File getExcludeBugsFilter() {
        TextResource excludeBugsFilterConfig = getExcludeBugsFilterConfig();
        if (excludeBugsFilterConfig == null) {
            return null;
        }
        return excludeBugsFilterConfig.asFile();
    }

    /**
     * The filename of a filter specifying baseline bugs to exclude from being reported.
     */
    public void setExcludeBugsFilter(File filter) {
        setExcludeBugsFilterConfig(project.getResources().getText().fromFile(filter));
    }

    /**
     * Any additional arguments (not covered here more explicitly like {@code effort}) to be passed along to FindBugs.
     * <p>
     * Extra arguments are passed to FindBugs after the arguments Gradle understands (like {@code effort} but before the list of classes to analyze.
     * This should only be used for arguments that cannot be provided by Gradle directly.
     * Gradle does not try to interpret or validate the arguments before passing them to FindBugs.
     * <p>
     * See the <a href="https://github.com/findbugsproject/findbugs/blob/master/findbugs/src/java/edu/umd/cs/findbugs/TextUICommandLine.java">FindBugs
     * TextUICommandLine source</a> for available options.
     *
     * @since 2.6
     */
    public Collection<String> getExtraArgs() {
        return extraArgs;
    }

    /**
     * Any additional arguments (not covered here more explicitly like {@code effort}) to be passed along to FindBugs.
     * <p>
     * Extra arguments are passed to FindBugs after the arguments Gradle understands (like {@code effort} but before the list of classes to analyze.
     * This should only be used for arguments that cannot be provided by Gradle directly.
     * Gradle does not try to interpret or validate the arguments before passing them to FindBugs.
     * <p>
     * See the <a href="https://github.com/findbugsproject/findbugs/blob/master/findbugs/src/java/edu/umd/cs/findbugs/TextUICommandLine.java">FindBugs
     * TextUICommandLine source</a> for available options.
     *
     * @since 2.6
     */
    public void setExtraArgs(Collection<String> extraArgs) {
        this.extraArgs = extraArgs;
    }

    /**
     * Any additional arguments to be passed along to FindBugs JVM process.
     * <p>
     * Arguments can contain general JVM flags like {@code -Xdebug} and also FindBugs system properties like {@code -Dfindbugs.loadPropertiesFrom=...}
     *
     * @since 4.3
     */
    @Incubating
    public Collection<String> getJvmArgs() {
        return jvmArgs;
    }

    /**
     * Any additional arguments to be passed along to FindBugs JVM process.
     * <p>
     * Arguments can contain general JVM flags like {@code -Xdebug} and also FindBugs system properties like {@code -Dfindbugs.loadPropertiesFrom=...}
     *
     * @since 4.3
     */
    @Incubating
    public void setJvmArgs(Collection<String> jvmArgs) {
        this.jvmArgs = jvmArgs;
    }

    /**
     * Indicates whether analysis progress should be rendered on standard output. Defaults to false.
     *
     * @since 4.2
     */
    @Incubating
    public boolean isShowProgress() {
        return showProgress;
    }

    /**
     * Indicates whether analysis progress should be rendered on standard output.
     *
     * @since 4.2
     */
    @Incubating
    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }
}
