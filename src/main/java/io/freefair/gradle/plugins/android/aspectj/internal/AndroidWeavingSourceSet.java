package io.freefair.gradle.plugins.android.aspectj.internal;

import com.android.build.api.dsl.AndroidSourceSet;
import io.freefair.gradle.plugins.aspectj.WeavingSourceSet;
import lombok.Data;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.jvm.ClassDirectoryBinaryNamingScheme;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

/**
 * {@link WeavingSourceSet} implementation based on {@link AndroidSourceSet}.
 *
 * @author Lars Grefer
 */
@Data
public class AndroidWeavingSourceSet implements WeavingSourceSet, HasPublicType {

    private final String aspectConfigurationName;
    private FileCollection aspectPath;

    private final String inpathConfigurationName;
    private FileCollection inPath;

    public AndroidWeavingSourceSet(AndroidSourceSet androidSourceSet) {
        ClassDirectoryBinaryNamingScheme namingScheme = new ClassDirectoryBinaryNamingScheme(androidSourceSet.getName());

        aspectConfigurationName = namingScheme.getTaskName("", "aspect");
        inpathConfigurationName = namingScheme.getTaskName("", "inpath");
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(WeavingSourceSet.class);
    }
}
