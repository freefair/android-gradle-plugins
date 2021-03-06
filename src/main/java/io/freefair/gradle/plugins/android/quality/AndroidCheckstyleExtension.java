package io.freefair.gradle.plugins.android.quality;

import lombok.EqualsAndHashCode;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.resources.TextResource;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Lars Grefer
 * @see org.gradle.api.plugins.quality.CheckstyleExtension
 */
@EqualsAndHashCode(callSuper = true)
public class AndroidCheckstyleExtension extends SourceSetBasedCodeQualityExtension {

    private final Project project;

    private TextResource config;
    private Map<String, Object> configProperties = new LinkedHashMap<String, Object>();
    private int maxErrors;
    private int maxWarnings = Integer.MAX_VALUE;
    private boolean showViolations = true;
    private DirectoryProperty configDir;

    public AndroidCheckstyleExtension(Project project) {
        this.project = project;
        configDir = project.getObjects().directoryProperty();
    }

    /**
     * The Checkstyle configuration file to use.
     */
    public File getConfigFile() {
        return getConfig().asFile();
    }

    /**
     * The Checkstyle configuration file to use.
     */
    public void setConfigFile(File configFile) {
        setConfig(project.getResources().getText().fromFile(configFile));
    }

    /**
     * The Checkstyle configuration to use. Replaces the {@code configFile} property.
     *
     * @since 2.2
     */
    public TextResource getConfig() {
        return config;
    }

    /**
     * The Checkstyle configuration to use. Replaces the {@code configFile} property.
     *
     * @since 2.2
     */
    public void setConfig(TextResource config) {
        this.config = config;
    }

    /**
     * The properties available for use in the configuration file. These are substituted into the configuration file.
     */
    public Map<String, Object> getConfigProperties() {
        return configProperties;
    }

    /**
     * The properties available for use in the configuration file. These are substituted into the configuration file.
     */
    public void setConfigProperties(Map<String, Object> configProperties) {
        this.configProperties = configProperties;
    }

    /**
     * Path to other Checkstyle configuration files. By default, this path is {@code $rootProject.projectDir/config/checkstyle}
     * <p>
     * This path will be exposed as the variable {@code config_loc} in Checkstyle's configuration files.
     * </p>
     *
     * @return path to other Checkstyle configuration files
     * @since 4.0
     */
    @Incubating
    public File getConfigDir() {
        return configDir.get().getAsFile();
    }

    /**
     * Path to other Checkstyle configuration files. By default, this path is {@code $rootProject.projectDir/config/checkstyle}
     * <p>
     * This path will be exposed as the variable {@code config_loc} in Checkstyle's configuration files.
     * </p>
     *
     * @since 4.0
     */
    @Incubating
    public void setConfigDir(File configDir) {
        this.configDir.set(configDir);
    }

    /**
     * Gets the configuration directory.
     *
     * @return The configuration directory
     * @since 4.7
     */
    @Incubating
    public DirectoryProperty getConfigDirectory() {
        return configDir;
    }

    /**
     * The maximum number of errors that are tolerated before breaking the build
     * or setting the failure property. Defaults to {@code 0}.
     * <p>
     * Example: maxErrors = 42
     *
     * @return the maximum number of errors allowed
     * @since 3.4
     */
    public int getMaxErrors() {
        return maxErrors;
    }

    /**
     * Set the maximum number of errors that are tolerated before breaking the build.
     *
     * @param maxErrors number of errors allowed
     * @since 3.4
     */
    public void setMaxErrors(int maxErrors) {
        this.maxErrors = maxErrors;
    }

    /**
     * The maximum number of warnings that are tolerated before breaking the build
     * or setting the failure property. Defaults to {@link Integer#MAX_VALUE}.
     * <p>
     * Example: maxWarnings = 1000
     *
     * @return the maximum number of warnings allowed
     * @since 3.4
     */
    public int getMaxWarnings() {
        return maxWarnings;
    }

    /**
     * Set the maximum number of warnings that are tolerated before breaking the build.
     *
     * @param maxWarnings number of warnings allowed
     * @since 3.4
     */
    public void setMaxWarnings(int maxWarnings) {
        this.maxWarnings = maxWarnings;
    }

    /**
     * Whether rule violations are to be displayed on the console. Defaults to {@code true}.
     * <p>
     * Example: showViolations = false
     */
    public boolean isShowViolations() {
        return showViolations;
    }

    /**
     * Whether rule violations are to be displayed on the console. Defaults to {@code true}.
     * <p>
     * Example: showViolations = false
     */
    public void setShowViolations(boolean showViolations) {
        this.showViolations = showViolations;
    }
}
