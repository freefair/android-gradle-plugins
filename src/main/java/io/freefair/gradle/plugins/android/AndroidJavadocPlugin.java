package io.freefair.gradle.plugins.android;

import com.android.build.gradle.TestedExtension;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.pipeline.TransformTask;
import com.android.build.gradle.internal.transforms.JackTransform;
import com.android.builder.core.JackProcessOptions;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.internal.jvm.Jvm;
import org.gradle.util.GUtil;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;

public class AndroidJavadocPlugin extends AndroidProjectPlugin {

    private Map<String, Javadoc> javadocTasks = new HashMap<>();

    private Task allJavadocTask;

    @Override
    public void apply(Project project) {
        super.apply(project);

        allJavadocTask = project.getTasks().create("javadoc", ajdTasks -> {
            ajdTasks.setDescription("Generate Javadoc for all variants");
            ajdTasks.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
        });

    }

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);
        getAndroidVariants().all(variant -> {
            Javadoc javadocTask = getJavadocTask(getProject(), variant);

            allJavadocTask.dependsOn(javadocTask);
        });
    }

    public Javadoc getJavadocTask(Project project, BaseVariant variant) {

        if (!javadocTasks.containsKey(variant.getName())) {

            Javadoc task = project.getTasks().create("javadoc" + capitalize((CharSequence) variant.getName()), Javadoc.class, javadoc -> {
                javadoc.setDescription("Generate Javadoc for the " + variant.getName() + " variant");
                javadoc.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);

                javadoc.dependsOn(variant.getJavaCompiler());

                if (variant.getJavaCompiler() instanceof JavaCompile) {
                    JavaCompile javacTask = (JavaCompile) variant.getJavaCompiler();

                    javadoc.setSource(javacTask.getSource());

                    javadoc.setClasspath(project.files(javacTask.getDestinationDir()).plus(javacTask.getClasspath()));
                    javadoc.getOptions().setSource(javacTask.getSourceCompatibility());
                }

                if (variant.getJavaCompiler() instanceof TransformTask) {
                    TransformTask jackTask = (TransformTask) variant.getJavaCompiler();
                    JackTransform jackTransform = (JackTransform) jackTask.getTransform();

                    Method getSourceFiles = null;
                    try {
                        getSourceFiles = JackTransform.class.getDeclaredMethod("getSourceFiles");
                        getSourceFiles.setAccessible(true);
                        Collection<File> sourceFiles = (Collection<File>) getSourceFiles.invoke(jackTransform);
                        javadoc.setSource(sourceFiles);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        project.getLogger().error(e.getLocalizedMessage(), e);
                    }

                    try {
                        Field optionsField = JackTransform.class.getDeclaredField("options");
                        optionsField.setAccessible(true);
                        JackProcessOptions options = (JackProcessOptions) optionsField.get(jackTransform);

                        javadoc.setClasspath(project.files(options.getClasspaths().toArray()));

                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        project.getLogger().error(e.getLocalizedMessage(), e);
                    }
                    javadoc.getOptions().setSource((String) jackTransform.getParameterInputs().get("sourceCompatibility"));
                }


                //javadoc.exclude '**/BuildConfig.java'
                javadoc.exclude("**/R.java");

                javadoc.getOptions().encoding("UTF-8");
                javadoc.getOptions().setBootClasspath(getAndroidExtension().getBootClasspath());

                if (javadoc.getOptions() instanceof StandardJavadocDocletOptions) {
                    StandardJavadocDocletOptions realOptions = (StandardJavadocDocletOptions) javadoc.getOptions();

                    realOptions.docEncoding("UTF-8");
                    realOptions.charSet("UTF-8");

                    Serializable javaVersion = GUtil.elvis(Jvm.current().getJavaVersion().getMajorVersion(), '8');
                    realOptions.links("http://docs.oracle.com/javase/" + javaVersion + "/docs/api/");
                    realOptions.linksOffline("http://developer.android.com/reference/", getAndroidExtension().getSdkDirectory() + "/docs/reference");
                }

                javadoc.setFailOnError(false);

                File docsDir;
                if (project.hasProperty("docsDir")) {
                    docsDir = (File) project.property("docsDir");
                } else {
                    docsDir = new File(project.getBuildDir(), "docs");
                }
                File javadocDir = new File(docsDir, "javadoc");
                javadoc.setDestinationDir(new File(javadocDir, variant.getDirName()));
            });

            javadocTasks.put(variant.getName(), task);
        }

        return javadocTasks.get(variant.getName());
    }
}
