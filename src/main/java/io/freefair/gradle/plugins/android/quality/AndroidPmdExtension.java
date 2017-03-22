package io.freefair.gradle.plugins.android.quality;

import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.quality.Pmd;
import org.gradle.api.plugins.quality.TargetJdk;
import org.gradle.api.resources.TextResource;

import java.util.Arrays;
import java.util.List;

/**
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.PmdExtension
 */
public class AndroidPmdExtension extends SourceSetBasedCodeQualityExtension {

    private final Project project;
    private List<String> ruleSets;
    private TargetJdk targetJdk;
    private int rulePriority = 5;
    private TextResource ruleSetConfig;
    private FileCollection ruleSetFiles;
    private boolean consoleOutput;

    public AndroidPmdExtension(Project project) {
        this.project = project;
    }

    public List<String> getRuleSets() {
        return this.ruleSets;
    }

    public void setRuleSets(List<String> ruleSets) {
        this.ruleSets = ruleSets;
    }

    public void ruleSets(String... ruleSets) {
        this.ruleSets.addAll(Arrays.asList(ruleSets));
    }

    public TargetJdk getTargetJdk() {
        return this.targetJdk;
    }

    public void setTargetJdk(Object value) {
        this.targetJdk = TargetJdk.toVersion(value);
    }

    @Incubating
    public int getRulePriority() {
        return this.rulePriority;
    }

    @Incubating
    public void setRulePriority(int intValue) {
        Pmd.validate(intValue);
        this.rulePriority = intValue;
    }

    @Incubating
    public TextResource getRuleSetConfig() {
        return this.ruleSetConfig;
    }

    @Incubating
    public void setRuleSetConfig(TextResource ruleSetConfig) {
        this.ruleSetConfig = ruleSetConfig;
    }

    public FileCollection getRuleSetFiles() {
        return this.ruleSetFiles;
    }

    public void setRuleSetFiles(FileCollection ruleSetFiles) {
        this.ruleSetFiles = ruleSetFiles;
    }

    public void ruleSetFiles(Object... ruleSetFiles) {
        this.ruleSetFiles.add(this.project.files(ruleSetFiles));
    }

    @Incubating
    public boolean isConsoleOutput() {
        return this.consoleOutput;
    }

    @Incubating
    public void setConsoleOutput(boolean consoleOutput) {
        this.consoleOutput = consoleOutput;
    }
}
