package net.caffeinemc.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.caffeinemc.gradle.GradleMixinConfigPlugin.LOGGER;


public abstract class CreateMixinConfigTask extends DefaultTask {

    @Option(option = "mixinParentPackage", description = "The parent of the mixin package. Mixins will be printed relative to the package.")
    String mixinParentPackage;
    @Option(option = "modShortName", description = "Short name of the mod.")
    String modShortName;
    @Option(option = "outputDirectoryForSummaryDocument", description = "Output directory for the summary markdown with all mixin rules and descriptions.")
    String outputDirectoryForSummaryDocument;

    @InputDirectory
    public abstract DirectoryProperty getInputFiles();

    @InputDirectory
    public abstract DirectoryProperty getIncludeFiles();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void run() {
        var inputSourceSet = this.getInputFiles().get().getAsFile().toPath();
        var outputDirectory = this.getOutputDirectory().get().getAsFile().toPath();

        Stream<Path> inputFileStream;

        try {
            inputFileStream = Files.walk(inputSourceSet);
        } catch (IOException e) {
            throw new RuntimeException("Failed to walk input directory", e);
        }

        URL url = null;
        try {
            url = inputSourceSet.toUri().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        ClassLoader loader = new URLClassLoader(new URL[]{url}, MixinConfigOption.class.getClassLoader());

        List<MixinRuleRepresentation> sortedMixinConfigOptions = inputFileStream
                .filter(inputFile -> inputFile.endsWith("package-info.class"))
                .filter(inputFile -> inputFile.startsWith(inputSourceSet))
                .map(inputFile -> {
                    Path inputPackagePath = inputSourceSet.relativize(inputFile.getParent());
                    String inputPackageName = inputPackagePath.toString().replaceAll(inputPackagePath.getFileSystem().getSeparator(), ".");
                    String inputPackageClassName = inputPackageName + ".package-info";
                    try {
                        Package inputPackage = loader.loadClass(inputPackageClassName).getPackage();
                        MixinConfigOption[] inputPackageAnnotations = inputPackage.getAnnotationsByType(MixinConfigOption.class);
                        if (inputPackageAnnotations.length > 1) {
                            LOGGER.warn(inputPackagePath + " had multiple mixin config option annotations, only using first!");
                        }
                        if (inputPackageAnnotations.length > 0) {
                            MixinConfigOption option = inputPackageAnnotations[0];
                            if (inputPackageName.startsWith(mixinParentPackage + ".")) {
                                inputPackageName = inputPackageName.substring(mixinParentPackage.length() + 1);
                            }
                            return new MixinRuleRepresentation(inputPackageName, option);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).filter(Objects::nonNull)
                .sorted(Comparator.comparing(MixinRuleRepresentation::path))
                .collect(Collectors.toList());

        try {
            DefaultConfigCreator.writeDefaultConfig(this.modShortName, outputDirectory.resolve(this.modShortName.toLowerCase() + "-mixin-config-default.properties").toFile(), sortedMixinConfigOptions);
            DefaultConfigCreator.writeMixinDependencies(this.modShortName, outputDirectory.resolve(this.modShortName.toLowerCase() + "-mixin-config-dependencies.properties").toFile(), sortedMixinConfigOptions);
            DefaultConfigCreator.writeMixinConfigSummaryMarkdown(this.modShortName, Path.of(this.outputDirectoryForSummaryDocument).resolve(this.modShortName.toLowerCase() + "-mixin-config.md").toFile(), sortedMixinConfigOptions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    record MixinRuleRepresentation(String path, MixinConfigOption config) {

    }
}