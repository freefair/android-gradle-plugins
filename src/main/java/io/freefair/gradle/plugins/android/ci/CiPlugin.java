package io.freefair.gradle.plugins.android.ci;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class CiPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPluginManager().apply(CiLintPlugin.class);

    }
}
