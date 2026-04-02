package dev.anicanon.swiftjava.kotlinaccessors.gradle;

import dev.anicanon.swiftjava.kotlinaccessors.core.RewriteOptions;
import dev.anicanon.swiftjava.kotlinaccessors.core.SwiftJavaKotlinAccessorsGenerator;
import java.io.IOException;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public abstract class GenerateSwiftJavaKotlinAccessorsTask extends DefaultTask {
    @InputDirectory
    public abstract DirectoryProperty getInputDir();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @Input
    public abstract ListProperty<String> getPackagePrefixes();

    @Optional
    @Input
    public abstract Property<String> getNullableAnnotationFqcn();

    @Optional
    @Input
    public abstract Property<Boolean> getGenerateKotlinFactories();

    @TaskAction
    public void generate() throws IOException {
        RewriteOptions options = new RewriteOptions(
            getPackagePrefixes().getOrElse(java.util.List.of()),
            getNullableAnnotationFqcn().getOrElse(""),
            getGenerateKotlinFactories().getOrElse(false)
        );
        new SwiftJavaKotlinAccessorsGenerator().generate(
            getInputDir().get().getAsFile().toPath(),
            getOutputDir().get().getAsFile().toPath(),
            options
        );
    }
}
