package io.freefair.gradle.plugin.android.ci;

import io.freefair.gradle.plugin.android.AndroidProjectPlugin;
import org.gradle.api.Project;

public class CiLintPlugin extends AndroidProjectPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);

        if (CiUtil.isCi()) {
            getAndroidExtension().getLintOptions().setHtmlReport(false);
            getAndroidExtension().getLintOptions().setXmlReport(false);

            getAndroidExtension().getLintOptions().setTextReport(true);
            getAndroidExtension().getLintOptions().textOutput("stdout");

        }
    }
}
