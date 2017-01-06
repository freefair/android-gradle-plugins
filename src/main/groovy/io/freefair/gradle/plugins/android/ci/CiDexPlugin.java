package io.freefair.gradle.plugins.android.ci;

import io.freefair.gradle.plugins.android.AndroidProjectPlugin;
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
