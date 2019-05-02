package io.freefair.gradle.plugins.android.quality;

import com.android.build.gradle.AppPlugin;
import io.freefair.gradle.plugins.android.AbstractGradlePluginTest;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Lars Grefer
 */
public class AndroidCodeQualityPluginsTest extends AbstractGradlePluginTest {

    @Test
    public void testAllCodeQualityPlugins() throws IOException {

        Project project = ProjectBuilder.builder()
                .withProjectDir(testProjectDir.getRoot())
                .build();

        setPackageName("io.freefair.android.test.allCodeQualityTest");

        project.getPlugins().apply(AppPlugin.class);
        project.getPlugins().apply(AndroidPmdPlugin.class);
        project.getPlugins().apply(AndroidCheckstylePlugin.class);
        project.getPlugins().apply(AndroidFindBugsPlugin.class);
    }
}
