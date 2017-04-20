package io.freefair.gradle.plugins.android.quality;

import io.freefair.gradle.plugins.android.AbstractGradlePluginTest;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.IOException;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

/**
 * @author Lars Grefer
 */
public class AndroidCodeQualityPluginsTest extends AbstractGradlePluginTest {

    @Test
    public void testAllCodeQualityPlugins() throws IOException {
        loadBuildFileFromClasspath("/quality-all.gradle");
        setPackageName("io.freefair.android.test.allCodeQualityTest");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("tasks", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":tasks").getOutcome(), SUCCESS);
    }
}