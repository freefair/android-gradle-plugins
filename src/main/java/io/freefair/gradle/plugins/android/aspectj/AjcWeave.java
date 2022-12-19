package io.freefair.gradle.plugins.android.aspectj;

import io.freefair.gradle.plugins.aspectj.AspectjCompile;
import io.freefair.gradle.plugins.aspectj.internal.AspectJCompileSpec;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;

public abstract class AjcWeave extends AspectjCompile {

    @InputFiles
    @SkipWhenEmpty
    public abstract ListProperty<RegularFile> getAllJars();

    @InputFiles
    @SkipWhenEmpty
    public abstract ListProperty<Directory> getAllDirs();

    @OutputFile
    public abstract RegularFileProperty getOutput();


    public AjcWeave() {
        getAjcOptions().getOutjar().set(getOutput());
    }

    @Override
    protected AspectJCompileSpec createSpec() {
        AspectJCompileSpec spec = super.createSpec();

        spec.setAdditionalInpath(getProject().files(getAllDirs(), getAllJars()));

        return spec;
    }
}
