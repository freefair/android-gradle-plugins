package io.freefair.gradle.plugins.android.aspectj;

import com.android.build.gradle.TestedExtension;
import io.freefair.gradle.plugins.android.AndroidProjectPlugin;
import io.freefair.gradle.plugins.android.aspectj.internal.AndroidWeavingSourceSet;
import io.freefair.gradle.plugins.aspectj.AjcAction;
import io.freefair.gradle.plugins.aspectj.AspectJBasePlugin;
import org.gradle.api.Incubating;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.compile.JavaCompile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements AspectJ Post-Compile Weaving by adding an {@link AjcAction} to the {@link JavaCompile} tasks.
 *
 * @author Lars Grefer
 * @see io.freefair.gradle.plugins.aspectj.AspectJPostCompileWeavingPlugin
 */
@Incubating
public class AndroidAspectJPostCompileWeavingPlugin extends AndroidProjectPlugin {

    private Map<String, Configuration> aspectpaths = new HashMap<>();
    private Map<String, Configuration> inpaths = new HashMap<>();

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);

        getProject().getLogger().warn("The android aspectj support is still incubating and subject to change.");

        AspectJBasePlugin basePlugin = getProject().getPlugins().apply(AspectJBasePlugin.class);

        extension.getSourceSets().all(sourceSet -> {

            AndroidWeavingSourceSet weavingSourceSet = new AndroidWeavingSourceSet(sourceSet);
            new DslObject(sourceSet).getConvention().add("aspectj", weavingSourceSet);

            Configuration aspectpath = getProject().getConfigurations().create(weavingSourceSet.getAspectConfigurationName());
            weavingSourceSet.setAspectPath(aspectpath);
            aspectpaths.put(sourceSet.getName(), aspectpath);

            Configuration inpath = getProject().getConfigurations().create(weavingSourceSet.getInpathConfigurationName());
            weavingSourceSet.setInPath(inpath);
            inpaths.put(sourceSet.getName(), inpath);

            getProject().getConfigurations().getByName(sourceSet.getImplementationConfigurationName()).extendsFrom(aspectpath);
            getProject().getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(inpath);
        });

        getAndroidVariants().all(variant -> variant.getJavaCompileProvider().configure(javaCompile -> {

            AjcAction action = getProject().getObjects().newInstance(AjcAction.class);

            action.getOptions().getInpath().from(javaCompile.getDestinationDir());
            action.getOptions().getBootclasspath().from(getAndroidExtension().getBootClasspath());
            action.getOptions().getBootclasspath().from(javaCompile.getOptions().getBootstrapClasspath());
            action.getOptions().getExtdirs().from(javaCompile.getOptions().getExtensionDirs());
            action.getClasspath().from(basePlugin.getAspectjConfiguration());

            variant.getSourceSets().forEach(sourceProvider -> {
                String sourceSetName = sourceProvider.getName();

                action.getOptions().getInpath().from(inpaths.get(sourceSetName));
                action.getOptions().getAspectpath().from(aspectpaths.get(sourceSetName));
            });

            try {
                Method addToTask = AjcAction.class.getDeclaredMethod("addToTask", Task.class);
                addToTask.setAccessible(true);
                addToTask.invoke(action, javaCompile);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
