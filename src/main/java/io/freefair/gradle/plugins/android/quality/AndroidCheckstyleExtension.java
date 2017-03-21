package io.freefair.gradle.plugins.android.quality;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.resources.TextResource;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.CheckstyleExtension
 */
@RequiredArgsConstructor
public class AndroidCheckstyleExtension extends AndroidCodeQualityExtension {

    private final Project project;
    private TextResource config;
    private Map<String, Object> configProperties = new LinkedHashMap<>();
    private int maxErrors;
    private int maxWarnings = 2147483647;
    private boolean showViolations = true;

    public File getConfigFile() {
        return this.getConfig().asFile();
    }

    @Incubating
    public TextResource getConfig() {
        return this.config;
    }

    public void setConfigFile(File configFile) {
        this.setConfig(this.project.getResources().getText().fromFile(configFile));
    }

    public void setConfig(TextResource config) {
        this.config = config;
    }

    public Map<String, Object> getConfigProperties() {
        return this.configProperties;
    }

    public void setConfigProperties(Map<String, Object> configProperties) {
        this.configProperties = configProperties;
    }

    public int getMaxErrors() {
        return this.maxErrors;
    }

    public void setMaxErrors(int maxErrors) {
        this.maxErrors = maxErrors;
    }

    public int getMaxWarnings() {
        return this.maxWarnings;
    }

    public void setMaxWarnings(int maxWarnings) {
        this.maxWarnings = maxWarnings;
    }

    public boolean isShowViolations() {
        return this.showViolations;
    }

    public void setShowViolations(boolean showViolations) {
        this.showViolations = showViolations;
    }
}
