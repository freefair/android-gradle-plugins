package io.freefair.gradle.plugins.android.aspectj;

import com.android.build.api.transform.*;
import com.android.build.gradle.TestedExtension;
import io.freefair.gradle.plugins.aspectj.AspectJCompileOptions;
import io.freefair.gradle.plugins.aspectj.internal.AspectJCompileSpec;
import io.freefair.gradle.plugins.aspectj.internal.AspectJCompiler;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.compile.CompilationFailedException;
import org.gradle.process.internal.JavaExecHandleFactory;

import java.io.IOException;
import java.util.*;

public class AspectJTransform extends Transform {

    private final Project project;
    private final TestedExtension testedExtension;
    private final FileCollection aspectjClasspath;

    AspectJTransform(Project project, TestedExtension testedExtension, FileCollection aspectjClasspath) {
        this.project = project;
        this.testedExtension = testedExtension;
        this.aspectjClasspath = aspectjClasspath;
    }

    @Override
    public String getName() {
        return "aspectj";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        HashSet<QualifiedContent.ContentType> contentTypes = new HashSet<>();
        contentTypes.add(QualifiedContent.DefaultContentType.CLASSES);
        return contentTypes;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return EnumSet.of(QualifiedContent.Scope.PROJECT);
    }

    @Override
    public Set<? super QualifiedContent.Scope> getReferencedScopes() {
        return EnumSet.of(QualifiedContent.Scope.EXTERNAL_LIBRARIES, QualifiedContent.Scope.SUB_PROJECTS);
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public Map<String, Object> getParameterInputs() {
        HashMap<String, Object> inputs = new HashMap<>();
        inputs.put("sourceCompatibility", testedExtension.getCompileOptions().getSourceCompatibility());
        inputs.put("targetCompatibility", testedExtension.getCompileOptions().getTargetCompatibility());
        inputs.put("encoding", testedExtension.getCompileOptions().getEncoding());
        return inputs;
    }

    @Override
    public Collection<SecondaryFile> getSecondaryFiles() {
        ArrayList<SecondaryFile> secondaryFiles = new ArrayList<>();
        secondaryFiles.add(SecondaryFile.nonIncremental(aspectjClasspath));
        return secondaryFiles;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        AspectJCompileSpec spec = createSpec();

        transformInvocation.getInputs().forEach(input -> {
            input.getDirectoryInputs().forEach(directoryInput -> spec.getAspectJCompileOptions().getInpath().from(directoryInput.getFile()));
            input.getJarInputs().forEach(jarInput -> spec.getAspectJCompileOptions().getInpath().from(jarInput.getFile()));
        });

        spec.setTempDir(transformInvocation.getContext().getTemporaryDir());
        spec.setDestinationDir(transformInvocation.getOutputProvider().getContentLocation("ajcOutputDir", getInputTypes(), EnumSet.of(QualifiedContent.Scope.PROJECT), Format.DIRECTORY));

        transformInvocation.getReferencedInputs().forEach(transformInput -> {
            transformInput.getDirectoryInputs().forEach(directoryInput -> spec.getCompileClasspath().add(directoryInput.getFile()));
            transformInput.getJarInputs().forEach(jarInput -> spec.getCompileClasspath().add(jarInput.getFile()));
        });

        executeAjc(spec);
    }

    private AspectJCompileSpec createSpec() {
        AspectJCompileSpec spec = new AspectJCompileSpec();
        spec.setAspectJCompileOptions(project.getObjects().newInstance(AspectJCompileOptions.class));
        spec.setCompileClasspath(new ArrayList<>());
        spec.setWorkingDir(project.getProjectDir());
        spec.setAspectJClasspath(aspectjClasspath);

        JavaVersion sourceCompatibility = testedExtension.getCompileOptions().getSourceCompatibility();
        if (sourceCompatibility != null) {
            spec.setSourceCompatibility(sourceCompatibility.toString());
        }

        JavaVersion targetCompatibility = testedExtension.getCompileOptions().getTargetCompatibility();
        if (targetCompatibility != null) {
            spec.setTargetCompatibility(targetCompatibility.toString());
        }

        spec.getAspectJCompileOptions().getEncoding().set(testedExtension.getCompileOptions().getEncoding());

        return spec;
    }

    private void executeAjc(AspectJCompileSpec spec) throws TransformException {
        JavaExecHandleFactory javaExecHandleFactory = ((ProjectInternal) project).getServices().get(JavaExecHandleFactory.class);

        AspectJCompiler aspectJCompiler = new AspectJCompiler(javaExecHandleFactory);

        try {
            aspectJCompiler.execute(spec);
        } catch (CompilationFailedException e) {
            throw new TransformException(e);
        }
    }
}
