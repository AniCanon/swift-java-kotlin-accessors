package dev.anicanon.swiftjava.kotlinaccessors.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public final class SwiftJavaKotlinAccessorsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        SwiftJavaKotlinAccessorsExtension extension = project.getExtensions().create(
            "swiftJavaKotlinAccessors",
            SwiftJavaKotlinAccessorsExtension.class,
            project.getObjects()
        );

        project.getTasks().register(
            "generateSwiftJavaKotlinAccessors",
            GenerateSwiftJavaKotlinAccessorsTask.class,
            task -> {
                task.getInputDir().set(extension.getInputDir());
                task.getOutputDir().set(extension.getOutputDir());
                task.getPackagePrefixes().set(extension.getPackagePrefixes());
                task.getNullableAnnotationFqcn().set(extension.getNullableAnnotationFqcn());
                task.getGenerateKotlinFactories().set(extension.getGenerateKotlinFactories());
            }
        );
    }
}
