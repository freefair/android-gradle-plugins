package io.freefair.gradle.plugins.android.maven;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AndroidMavenJarsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(AndroidSourcesJarPlugin.class);
        project.getPluginManager().apply(AndroidJavadocJarPlugin.class);
    }
}
