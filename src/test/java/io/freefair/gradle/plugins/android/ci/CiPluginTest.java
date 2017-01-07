package io.freefair.gradle.plugins.android.ci;

import io.freefair.gradle.plugins.android.AbstractGradlePluginTest;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.IOException;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.*;

public class CiPluginTest extends AbstractGradlePluginTest {

    @Test
    public void testApp() throws IOException {
        loadBuildFileFromClasspath("/ci-app.gradle");
        setPackageName("io.freefair.android.test.ciApp");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("assemble", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":assemble").getOutcome(), SUCCESS);
    }

    @Test
    public void testLibrary() throws IOException {
        loadBuildFileFromClasspath("/ci-library.gradle");
        setPackageName("io.freefair.android.test.ciLib");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("assemble", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":assemble").getOutcome(), SUCCESS);
    }
}