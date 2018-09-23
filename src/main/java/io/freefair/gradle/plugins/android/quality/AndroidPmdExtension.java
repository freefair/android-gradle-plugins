package io.freefair.gradle.plugins.android.quality;

import lombok.EqualsAndHashCode;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.quality.Pmd;
import org.gradle.api.plugins.quality.TargetJdk;
import org.gradle.api.resources.TextResource;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.PmdExtension
 */
@EqualsAndHashCode(callSuper = true)
public class AndroidPmdExtension extends SourceSetBasedCodeQualityExtension {

    private final Project project;

    private List<String> ruleSets;
    private TargetJdk targetJdk;
    private int rulePriority = 5;
    private TextResource ruleSetConfig;
    private ConfigurableFileCollection ruleSetFiles;
    private boolean consoleOutput;

    public AndroidPmdExtension(Project project) {
        this.project = project;
    }

    /**
     * The built-in rule sets to be used. See the <a href="http://pmd.sourceforge.net/rules/index.html">official list</a> of built-in rule sets.
     *
     * Example: ruleSets = ["basic", "braces"]
     */
    public List<String> getRuleSets() {
        return ruleSets;
    }

    /**
     * The built-in rule sets to be used. See the <a href="http://pmd.sourceforge.net/rules/index.html">official list</a> of built-in rule sets.
     *
     * Example: ruleSets = ["basic", "braces"]
     */
    public void setRuleSets(List<String> ruleSets) {
        this.ruleSets = ruleSets;
    }

    /**
     * Convenience method for adding rule sets.
     *
     * Example: ruleSets "basic", "braces"
     *
     * @param ruleSets the rule sets to be added
     */
    public void ruleSets(String... ruleSets) {
        this.ruleSets.addAll(Arrays.asList(ruleSets));
    }

    /**
     * The target jdk to use with pmd, 1.3, 1.4, 1.5, 1.6, 1.7 or jsp
     */
    public TargetJdk getTargetJdk() {
        return targetJdk;
    }

    /**
     * Sets the target jdk used with pmd.
     *
     * @param targetJdk The target jdk
     * @since 4.0
     */
    public void setTargetJdk(TargetJdk targetJdk) {
        this.targetJdk = targetJdk;
    }

    /**
     * Sets the target jdk used with pmd.
     *
     * @param value The value for the target jdk as defined by {@link TargetJdk#toVersion(Object)}
     */
    public void setTargetJdk(Object value) {
        targetJdk = TargetJdk.toVersion(value);
    }

    /**
     * The rule priority threshold; violations for rules with a lower priority will not be reported. Default value is 5, which means that all violations will be reported.
     *
     * This is equivalent to PMD's Ant task minimumPriority property.
     *
     * See the official documentation for the <a href="http://pmd.sourceforge.net/rule-guidelines.html">list of priorities</a>.
     *
     * Example: rulePriority = 3
     */
    @Incubating
    public int getRulePriority() {
        return rulePriority;
    }

    /**
     * Sets the rule priority threshold.
     */
    @Incubating
    public void setRulePriority(int intValue) {
        Pmd.validate(intValue);
        rulePriority = intValue;
    }

    /**
     * The custom rule set to be used (if any). Replaces {@code ruleSetFiles}, except that it does not currently support multiple rule sets.
     *
     * See the <a href="http://pmd.sourceforge.net/howtomakearuleset.html">official documentation</a> for how to author a rule set.
     *
     * Example: ruleSetConfig = resources.text.fromFile("config/pmd/myRuleSet.xml")
     *
     * @since 2.2
     */
    @Incubating
    @Nullable
    public TextResource getRuleSetConfig() {
        return ruleSetConfig;
    }

    /**
     * The custom rule set to be used (if any). Replaces {@code ruleSetFiles}, except that it does not currently support multiple rule sets.
     *
     * See the <a href="http://pmd.sourceforge.net/howtomakearuleset.html">official documentation</a> for how to author a rule set.
     *
     * Example: ruleSetConfig = resources.text.fromFile("config/pmd/myRuleSet.xml")
     *
     * @since 2.2
     */
    @Incubating
    public void setRuleSetConfig(@Nullable TextResource ruleSetConfig) {
        this.ruleSetConfig = ruleSetConfig;
    }

    /**
     * The custom rule set files to be used. See the <a href="http://pmd.sourceforge.net/howtomakearuleset.html">official documentation</a> for how to author a rule set file.
     *
     * Example: ruleSetFiles = files("config/pmd/myRuleSet.xml")
     */
    public FileCollection getRuleSetFiles() {
        return ruleSetFiles;
    }

    /**
     * The custom rule set files to be used. See the <a href="http://pmd.sourceforge.net/howtomakearuleset.html">official documentation</a> for how to author a rule set file.
     *
     * Example: ruleSetFiles = files("config/pmd/myRuleSet.xml")
     */
    public void setRuleSetFiles(FileCollection ruleSetFiles) {
        this.ruleSetFiles = project.getLayout().configurableFiles(ruleSetFiles);
    }

    /**
     * Convenience method for adding rule set files.
     *
     * Example: ruleSetFiles "config/pmd/myRuleSet.xml"
     *
     * @param ruleSetFiles the rule set files to be added
     */
    public void ruleSetFiles(Object... ruleSetFiles) {
        this.ruleSetFiles.from(ruleSetFiles);
    }

    /**
     * Whether or not to write PMD results to {@code System.out}.
     */
    @Incubating
    public boolean isConsoleOutput() {
        return consoleOutput;
    }

    /**
     * Whether or not to write PMD results to {@code System.out}.
     */
    @Incubating
    public void setConsoleOutput(boolean consoleOutput) {
        this.consoleOutput = consoleOutput;
    }
}
