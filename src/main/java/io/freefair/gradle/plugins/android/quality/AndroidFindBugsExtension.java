package io.freefair.gradle.plugins.android.quality;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.resources.TextResource;

import java.io.File;
import java.util.Collection;

/**
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.FindBugsExtension
 */
@RequiredArgsConstructor
public class AndroidFindBugsExtension extends AndroidCodeQualityExtension {

    private final Project project;
    private String effort;
    private String reportLevel;
    private Collection<String> visitors;
    private Collection<String> omitVisitors;
    private TextResource includeFilterConfig;
    private TextResource excludeFilterConfig;
    private TextResource excludeBugsFilterConfig;
    private Collection<String> extraArgs;

    public String getEffort() {
        return this.effort;
    }

    public void setEffort(String effort) {
        this.effort = effort;
    }

    public String getReportLevel() {
        return this.reportLevel;
    }

    public void setReportLevel(String reportLevel) {
        this.reportLevel = reportLevel;
    }

    public Collection<String> getVisitors() {
        return this.visitors;
    }

    public void setVisitors(Collection<String> visitors) {
        this.visitors = visitors;
    }

    public Collection<String> getOmitVisitors() {
        return this.omitVisitors;
    }

    public void setOmitVisitors(Collection<String> omitVisitors) {
        this.omitVisitors = omitVisitors;
    }

    @Incubating
    public TextResource getIncludeFilterConfig() {
        return this.includeFilterConfig;
    }

    @Incubating
    public void setIncludeFilterConfig(TextResource includeFilterConfig) {
        this.includeFilterConfig = includeFilterConfig;
    }

    public File getIncludeFilter() {
        TextResource includeFilterConfig = this.getIncludeFilterConfig();
        return includeFilterConfig == null?null:includeFilterConfig.asFile();
    }

    public void setIncludeFilter(File filter) {
        this.setIncludeFilterConfig(this.project.getResources().getText().fromFile(filter));
    }

    @Incubating
    public TextResource getExcludeFilterConfig() {
        return this.excludeFilterConfig;
    }

    @Incubating
    public void setExcludeFilterConfig(TextResource excludeFilterConfig) {
        this.excludeFilterConfig = excludeFilterConfig;
    }

    public File getExcludeFilter() {
        TextResource excludeFilterConfig = this.getExcludeFilterConfig();
        return excludeFilterConfig == null?null:excludeFilterConfig.asFile();
    }

    public void setExcludeFilter(File filter) {
        this.setExcludeFilterConfig(this.project.getResources().getText().fromFile(filter));
    }

    @Incubating
    public TextResource getExcludeBugsFilterConfig() {
        return this.excludeBugsFilterConfig;
    }

    @Incubating
    public void setExcludeBugsFilterConfig(TextResource excludeBugsFilterConfig) {
        this.excludeBugsFilterConfig = excludeBugsFilterConfig;
    }

    public File getExcludeBugsFilter() {
        TextResource excludeBugsFilterConfig = this.getExcludeBugsFilterConfig();
        return excludeBugsFilterConfig == null?null:excludeBugsFilterConfig.asFile();
    }

    public void setExcludeBugsFilter(File filter) {
        this.setExcludeBugsFilterConfig(this.project.getResources().getText().fromFile(filter));
    }

    public Collection<String> getExtraArgs() {
        return this.extraArgs;
    }

    public void setExtraArgs(Collection<String> extraArgs) {
        this.extraArgs = extraArgs;
    }
}
