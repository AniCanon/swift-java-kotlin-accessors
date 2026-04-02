package dev.anicanon.swiftjava.kotlinaccessors.gradle;

import java.util.List;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.file.DirectoryProperty;

public abstract class SwiftJavaKotlinAccessorsExtension {
    public abstract DirectoryProperty getInputDir();
    public abstract DirectoryProperty getOutputDir();
    public abstract ListProperty<String> getPackagePrefixes();
    public abstract Property<String> getNullableAnnotationFqcn();

    public SwiftJavaKotlinAccessorsExtension(ObjectFactory objects) {
        getPackagePrefixes().convention(List.of());
        getNullableAnnotationFqcn().convention("");
    }
}
