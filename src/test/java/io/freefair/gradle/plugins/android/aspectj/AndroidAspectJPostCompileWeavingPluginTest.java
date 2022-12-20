package io.freefair.gradle.plugins.android.aspectj;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AndroidAspectJPostCompileWeavingPluginTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    void testApp() {
        project.getPlugins().apply("com.android.application");
        project.getPlugins().apply(AndroidAspectJPostCompileWeavingPlugin.class);
    }

    @Test
    void testLib() {
        project.getPlugins().apply("com.android.library");
        project.getPlugins().apply(AndroidAspectJPostCompileWeavingPlugin.class);
    }

}
