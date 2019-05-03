package io.freefair.gradle.plugins.android.ci;

import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.LibraryPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

public class CiDexPluginTest {

    private Project project;

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void apply_app() {
        project.getPlugins().apply(AppPlugin.class);
        project.getPlugins().apply(CiDexPlugin.class);
    }

    @Test
    public void apply_lib() {
        project.getPlugins().apply(LibraryPlugin.class);
        project.getPlugins().apply(CiDexPlugin.class);
    }

    @Test
    public void apply_alone() {
        project.getPlugins().apply(CiDexPlugin.class);
    }
}