package io.freefair.gradle.plugins.android.aspectj;

import com.android.build.api.artifact.ScopedArtifact;
import com.android.build.api.dsl.CommonExtension;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.ScopedArtifacts;
import com.android.build.gradle.BasePlugin;
import io.freefair.gradle.plugins.aspectj.AspectJBasePlugin;
import kotlin.Pair;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lars Grefer
 */
public class AndroidAspectJPostCompileWeavingPlugin implements Plugin<Project> {

    private Project project;

    private Configuration inpath;
    private Configuration aspectpath;

    private final Map<String, Configuration> buildTypeInpaths = new HashMap<>();
    private final Map<String, Configuration> buildTypeAspectpaths = new HashMap<>();

    private final Map<String, Configuration> flavorInpaths = new HashMap<>();
    private final Map<String, Configuration> flavorAspectpaths = new HashMap<>();

    private final Map<String, Configuration> variantInpaths = new HashMap<>();
    private final Map<String, Configuration> variantAspectpaths = new HashMap<>();


    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPlugins().apply(AspectJBasePlugin.class);

        inpath = project.getConfigurations().create("inpath");
        aspectpath = project.getConfigurations().create("aspect");

        project.getPlugins().withType(BasePlugin.class, this::withAndroidPlugin);
    }

    private void withAndroidPlugin(BasePlugin basePlugin) {

        CommonExtension<?, ?, ?, ?> android = project.getExtensions().getByType(CommonExtension.class);
        AndroidComponentsExtension<?, ?, ?> androidComponents = project.getExtensions().getByType(AndroidComponentsExtension.class);

        android.getBuildTypes().configureEach(buildType -> {
            String buildTypeName = buildType.getName();

            Configuration buildTypeInpath = project.getConfigurations().create(buildTypeName + "Inpath");
            buildTypeInpath.extendsFrom(inpath);
            buildTypeInpaths.put(buildTypeName, buildTypeInpath);

            Configuration buildTypeAspectpath = project.getConfigurations().create(buildTypeName + "Aspect");
            buildTypeAspectpath.extendsFrom(aspectpath);
            buildTypeAspectpaths.put(buildTypeName, buildTypeAspectpath);
        });

        android.getProductFlavors().configureEach(productFlavor -> {
            String flavorName = productFlavor.getName();

            Configuration flavorInpath = project.getConfigurations().create(flavorName + "Inpath");
            flavorInpath.extendsFrom(inpath);
            flavorInpaths.put(flavorName, flavorInpath);

            Configuration flavorAspectpath = project.getConfigurations().create(flavorName + "Aspect");
            flavorAspectpath.extendsFrom(aspectpath);
            flavorAspectpaths.put(flavorName, flavorAspectpath);
        });

        androidComponents.beforeVariants(androidComponents.selector().all(), variantBuilder -> {
            String variantName = variantBuilder.getName();

            Configuration variantInpath = project.getConfigurations().create(variantName + "Inpath");
            variantInpaths.put(variantName, variantInpath);
            variantInpath.extendsFrom(buildTypeInpaths.get(variantBuilder.getBuildType()));

            Configuration variantAspectpath = project.getConfigurations().create(variantName + "Aspect");
            variantAspectpaths.put(variantName, variantAspectpath);
            variantAspectpath.extendsFrom(buildTypeAspectpaths.get(variantBuilder.getBuildType()));

            for (Pair<String, String> productFlavor : variantBuilder.getProductFlavors()) {
                String flavorName = productFlavor.getSecond();

                variantInpath.extendsFrom(flavorInpaths.get(flavorName));
                variantAspectpath.extendsFrom(flavorAspectpaths.get(flavorName));
            }

        });

        androidComponents.onVariants(androidComponents.selector().all(), variant -> {

            TaskProvider<AjcWeave> register = project.getTasks().register(variant.getName() + "AjcWeave", AjcWeave.class, ajcWeave -> {
                ajcWeave.setSourceCompatibility(android.getCompileOptions().getSourceCompatibility().getMajorVersion());
                ajcWeave.setTargetCompatibility(android.getCompileOptions().getTargetCompatibility().getMajorVersion());
                ajcWeave.getOptions().setEncoding(android.getCompileOptions().getTargetCompatibility().getMajorVersion());

                ajcWeave.getAjcOptions().getInpath().from(variantInpaths.get(variant.getName()));
                ajcWeave.getAjcOptions().getAspectpath().from(variantAspectpaths.get(variant.getName()));
                ajcWeave.setClasspath(variant.getCompileClasspath());
            });

            variant.getArtifacts()
                    .forScope(ScopedArtifacts.Scope.PROJECT)
                    .use(register)
                    .toTransform(ScopedArtifact.CLASSES.INSTANCE,
                            AjcWeave::getAllJars,
                            AjcWeave::getAllDirs,
                            AjcWeave::getOutput);
        });
    }
}
