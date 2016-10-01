package io.freefair.gradle.plugin.android.ci;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class CiPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPluginManager().apply(CiDexPlugin.class);
        project.getPluginManager().apply(CiLintPlugin.class);

    }
}
