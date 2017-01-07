package io.freefair.gradle.plugins.android.ci;

import io.freefair.gradle.plugins.android.AbstractGradlePluginTest;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.IOException;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.*;

/**
 * Created by larsgrefer on 07.01.17.
 */
public class CiPluginTest extends AbstractGradlePluginTest {

    @Test
    public void testApp() throws IOException {
        loadBuildFileFromClasspath("/ci-app.gradle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("build", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":build").getOutcome(), SUCCESS);
    }

    @Test
    public void testLibrary() throws IOException {
        loadBuildFileFromClasspath("/ci-library.gradle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("build", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":build").getOutcome(), SUCCESS);
    }
}