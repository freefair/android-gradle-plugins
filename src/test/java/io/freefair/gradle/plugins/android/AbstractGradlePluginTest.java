package io.freefair.gradle.plugins.android;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AbstractGradlePluginTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    protected File buildFile;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
    }

    protected void loadBuildFileFromClasspath(String name) throws IOException {
        InputStream resourceAsStream = getClass().getResourceAsStream(name);
        Files.copy(resourceAsStream, buildFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
