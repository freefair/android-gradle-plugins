package io.freefair.gradle.plugins.android.ci;

import com.android.build.api.dsl.CommonExtension;
import io.freefair.gradle.plugins.android.AndroidProjectUtil;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class CiLintPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        if (CiUtil.isCi()) {
            CommonExtension<?, ?, ?, ?, ?, ?> extension = AndroidProjectUtil.getAndroidExtension(project);

            extension.getLint().setHtmlReport(false);
            extension.getLint().setXmlReport(false);

            extension.getLint().setTextReport(true);
        }
    }
}
