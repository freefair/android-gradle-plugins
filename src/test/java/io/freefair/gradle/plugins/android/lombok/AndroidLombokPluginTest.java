package io.freefair.gradle.plugins.android.lombok;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AndroidLombokPluginTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    void testApp() {
        project.getPlugins().apply("com.android.application");
        project.getPlugins().apply(AndroidLombokPlugin.class);
    }

    @Test
    void testLib() {
        project.getPlugins().apply("com.android.library");
        project.getPlugins().apply(AndroidLombokPlugin.class);
    }

}
