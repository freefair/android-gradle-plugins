package io.freefair.gradle.plugins.android.ci;

import com.android.build.gradle.BaseExtension;
import io.freefair.gradle.plugins.android.AndroidProjectPlugin;
import org.gradle.api.Project;

public class CiLintPlugin extends AndroidProjectPlugin {

    @Override
    protected void withAndroid(BaseExtension extension) {
        super.withAndroid(extension);
        if (CiUtil.isCi()) {
            extension.getLintOptions().setHtmlReport(false);
            extension.getLintOptions().setXmlReport(false);

            extension.getLintOptions().setTextReport(true);
            extension.getLintOptions().textOutput("stdout");
        }
    }
}
