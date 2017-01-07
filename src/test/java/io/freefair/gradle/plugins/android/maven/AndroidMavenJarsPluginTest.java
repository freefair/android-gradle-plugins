package io.freefair.gradle.plugins.android.maven;

import io.freefair.gradle.plugins.android.AbstractGradlePluginTest;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.IOException;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.*;

public class AndroidMavenJarsPluginTest extends AbstractGradlePluginTest {

    @Test
    public void testApp() throws IOException {
        loadBuildFileFromClasspath("/maven-jars-app.gradle");
        setPackageName("io.freefair.android.test.mavenJarsApp");

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
        loadBuildFileFromClasspath("/maven-jars-library.gradle");
        setPackageName("io.freefair.android.test.mavenJarsLib");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("assemble", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertEquals(result.task(":assemble").getOutcome(), SUCCESS);
    }
}