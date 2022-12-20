package io.freefair.gradle.plugins.android.lombok;

import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import io.freefair.gradle.plugins.lombok.tasks.LombokConfig;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;

public abstract class AndroidLombokConfig extends LombokConfig {

    @InputFiles
    @Optional
    @Nullable
    @PathSensitive(PathSensitivity.ABSOLUTE)
    @Override
    protected Set<File> getConfigFiles() {
        return null;
    }
}
