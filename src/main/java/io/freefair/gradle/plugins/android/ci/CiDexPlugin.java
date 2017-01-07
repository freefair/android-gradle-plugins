package io.freefair.gradle.plugins.android.ci;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.TestedExtension;
import io.freefair.gradle.plugins.android.AndroidProjectPlugin;
import org.gradle.api.Project;

public class CiDexPlugin extends AndroidProjectPlugin {

    @Override
    protected void withAndroid(BaseExtension extension) {
        super.withAndroid(extension);
        if(CiUtil.isCi()) {
            extension.getDexOptions().setPreDexLibraries(false);
        }
    }
}
