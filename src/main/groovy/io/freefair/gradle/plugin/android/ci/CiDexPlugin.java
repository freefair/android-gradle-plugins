package io.freefair.gradle.plugin.android.ci;

import io.freefair.gradle.plugin.android.AndroidProjectPlugin;
import org.gradle.api.Project;

public class CiDexPlugin extends AndroidProjectPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);

        if(CiUtil.isCi()) {
            getAndroidExtension().getDexOptions().setPreDexLibraries(false);
        }
    }
}
