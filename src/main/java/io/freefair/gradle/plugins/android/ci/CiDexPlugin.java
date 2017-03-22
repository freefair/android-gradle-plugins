package io.freefair.gradle.plugins.android.ci;

import com.android.build.gradle.TestedExtension;
import io.freefair.gradle.plugins.android.AndroidProjectPlugin;

public class CiDexPlugin extends AndroidProjectPlugin {

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);
        if(CiUtil.isCi()) {
            extension.getDexOptions().setPreDexLibraries(false);
        }
    }
}
