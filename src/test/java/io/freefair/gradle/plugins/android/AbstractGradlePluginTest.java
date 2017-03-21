package io.freefair.gradle.plugins.android;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AbstractGradlePluginTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    protected File buildFile;
    private File srcMain;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
        srcMain = testProjectDir.newFolder("src", "main");

    }

    protected void setPackageName(String packageName) throws FileNotFoundException, UnsupportedEncodingException {
        File androidManifestXml = new File(srcMain, "AndroidManifest.xml");

        try (PrintWriter writer = new PrintWriter(androidManifestXml, "UTF-8")) {
            writer.printf("<manifest package=\"%s\"></manifest>", packageName);
            writer.println();
        }
    }

    protected void loadBuildFileFromClasspath(String name) throws IOException {
        InputStream resourceAsStream = AbstractGradlePluginTest.class.getResourceAsStream(name);
        Files.copy(resourceAsStream, buildFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
