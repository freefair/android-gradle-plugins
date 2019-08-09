package io.freefair.gradle.plugins.android.maven;

import com.android.build.gradle.TestedExtension;
import com.android.build.gradle.api.*;
import com.google.common.collect.ImmutableSet;
import io.freefair.gradle.plugins.android.AndroidProjectPlugin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Action;
import org.gradle.api.Incubating;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationVariant;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.ConfigurationVariantDetails;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.internal.artifacts.ArtifactAttributes;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class AndroidComponentPlugin extends AndroidProjectPlugin {

    private final SoftwareComponentFactory softwareComponentFactory;

    @Getter
    private Map<String, SoftwareComponent> variantComponents = new HashMap<>();

    @Inject
    public AndroidComponentPlugin(SoftwareComponentFactory softwareComponentFactory) {
        this.softwareComponentFactory = softwareComponentFactory;
    }

    @Override
    protected void withAndroid(TestedExtension extension) {
        super.withAndroid(extension);

        getAndroidVariants().all(variant -> {
            AdhocComponentWithVariants component;

            if (variant instanceof LibraryVariant) {
                component = softwareComponentFactory.adhoc(variant.getName() + "AndroidLibrary");
            }
            else if (variant instanceof ApplicationVariant) {
                component = softwareComponentFactory.adhoc(variant.getName() + "AndroidApplication");
            }
            else if (variant instanceof FeatureVariant) {
                component = softwareComponentFactory.adhoc(variant.getName() + "AndroidFeature");
            }
            else {
                throw new IllegalStateException();
            }

            Configuration apiElements = getProject().getConfigurations().getByName(variant.getName() + "ApiElements");
            Configuration runtimeElements = getProject().getConfigurations().getByName(variant.getName() + "RuntimeElements");

            component.addVariantsFromConfiguration(apiElements, new AndroidConfigurationVariantMapping("compile", false));
            component.addVariantsFromConfiguration(runtimeElements, new AndroidConfigurationVariantMapping("runtime", false));

            if (variant instanceof LibraryVariant) {
                PublishArtifact aarArtifact = new LazyPublishArtifact(((LibraryVariant) variant).getPackageLibraryProvider());

                apiElements.getOutgoing().artifact(aarArtifact);
                apiElements.getOutgoing().getAttributes().attribute(ArtifactAttributes.ARTIFACT_FORMAT, "aar");
                runtimeElements.getOutgoing().artifact(aarArtifact);
                runtimeElements.getOutgoing().getAttributes().attribute(ArtifactAttributes.ARTIFACT_FORMAT, "aar");
            }
            else if (variant instanceof ApkVariant) {
                PublishArtifact aarArtifact = new LazyPublishArtifact(((ApkVariant) variant).getPackageApplicationProvider());

                apiElements.getOutgoing().artifact(aarArtifact);
                apiElements.getOutgoing().getAttributes().attribute(ArtifactAttributes.ARTIFACT_FORMAT, "aar");
                runtimeElements.getOutgoing().artifact(aarArtifact);
                runtimeElements.getOutgoing().getAttributes().attribute(ArtifactAttributes.ARTIFACT_FORMAT, "aar");
            }

            getProject().getComponents().add(component);
            variantComponents.put(variant.getName(), component);
        });
    }

    /**
     * @see JavaConfigurationVariantMapping
     */
    public static class AndroidConfigurationVariantMapping implements Action<ConfigurationVariantDetails> {

        private final String scope;
        private final boolean optional;

        public AndroidConfigurationVariantMapping(String scope, boolean optional) {
            this.scope = scope;
            this.optional = optional;
        }

        @Override
        public void execute(ConfigurationVariantDetails details) {
            ConfigurationVariant variant = details.getConfigurationVariant();
            if (ArtifactTypeSpec.INSTANCE.isSatisfiedBy(variant)) {
                details.mapToMavenScope(scope);
                if (optional) {
                    details.mapToOptional();
                }
            }
            else {
                details.skip();
            }
        }

        private static class ArtifactTypeSpec implements Spec<ConfigurationVariant> {
            private static final ArtifactTypeSpec INSTANCE = new ArtifactTypeSpec();

            @Override
            public boolean isSatisfiedBy(ConfigurationVariant element) {
                for (PublishArtifact artifact : element.getArtifacts()) {
                    log.warn(artifact.getType());
                    if (UNPUBLISHABLE_VARIANT_ARTIFACTS.contains(artifact.getType())) {
                        return false;
                    }
                }
                return true;
            }
        }

        @Incubating
        public static final Set<String> UNPUBLISHABLE_VARIANT_ARTIFACTS = ImmutableSet.of(
                ArtifactTypeDefinition.JVM_CLASS_DIRECTORY,
                ArtifactTypeDefinition.JVM_RESOURCES_DIRECTORY,
                ArtifactTypeDefinition.DIRECTORY_TYPE,
                "android-aidl",
                "android-renderscript",
                "android-assets",
                "android-consumer-proguard-rules",
                "android-jni",
                "android-lint",
                "android-public-res",
                "android-res",
                "android-classes",
                "android-java-res",
                "android-symbol-with-package-name",
                "android-symbol"
        );
    }
}
