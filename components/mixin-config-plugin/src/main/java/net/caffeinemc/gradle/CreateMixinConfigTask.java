package net.caffeinemc.gradle;

import kotlin.Pair;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.caffeinemc.gradle.GradleMixinConfigPlugin.LOGGER;

public abstract class CreateMixinConfigTask extends DefaultTask {

    @Option(option = "mixinParentPackage", description = "The parent of the mixin package. Mixins will be printed relative to the package.")
    public List<String> mixinParentPackages;
    @Option(option = "mixinPackagePrefix", description = "Name of the mixin package relative to the mixinParentPackage.")
    public String mixinPackage = "mixin";
    @Option(option = "modShortName", description = "Short name of the mod.")
    public String modShortName;
    @Option(option = "outputDirectoryForSummaryDocument", description = "Output directory for the summary markdown with all mixin rules and descriptions.")
    public String outputDirectoryForSummaryDocument;

    @InputFiles
    public abstract ListProperty<Directory> getInputFiles();

    @InputDirectory
    public abstract DirectoryProperty getIncludeFiles();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void run() {
        var outputDirectory = this.getOutputDirectory().get().getAsFile().toPath();
        HashMap<Path, Directory> inputFiles = new HashMap<>();

        for (Directory inputDir : getInputFiles().get()) {
            try {
                inputFiles.putAll(Files.walk(inputDir.getAsFile().toPath()).map(e -> new Pair<>(e, inputDir)).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
            } catch (IOException e) {
                throw new RuntimeException("Failed to walk input directory: " + inputDir, e);
            }
        }

        List<URL> urls = inputFiles.keySet().stream()
                .map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Failed to convert path to URL: " + path, e);
                    }
                })
                .collect(Collectors.toList());

        ClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), MixinConfigOption.class.getClassLoader());
        HashSet<String> mixinPackages = new HashSet<>();
        HashSet<String> mixinOptions = new HashSet<>();
        List<MixinRuleRepresentation> sortedMixinConfigOptions = inputFiles.entrySet().stream().filter(path -> path.getKey().toFile().isFile())
                .map((Map.Entry<Path, Directory> inputFile) -> {
                    boolean isPackageInfo = inputFile.getKey().endsWith("package-info.class");
                    Path inputPackagePath = inputFile.getValue().getAsFile().toPath().relativize(inputFile.getKey().getParent());
                    String inputPackageName = inputPackagePath.toString().replaceAll(Pattern.quote(inputPackagePath.getFileSystem().getSeparator()), ".");
                    String inputPackageClassName = inputPackageName + ".package-info";

                    boolean isInMixinPackage = false;
                    for (String mixinParentPackage : mixinParentPackages) {
                        if (inputPackageName.startsWith(mixinParentPackage + "." + mixinPackage + ".")) {
                            inputPackageName = inputPackageName.substring(mixinParentPackage.length() + 1);
                            isInMixinPackage = true;
                        }
                    }
                    if (!isInMixinPackage) {
                        return null;
                    }

                    if (!isPackageInfo) {
                        mixinPackages.add(inputPackageName);
                        return null;
                    }
                    try {
                        Package inputPackage = loader.loadClass(inputPackageClassName).getPackage();
                        MixinConfigOption[] inputPackageAnnotations = inputPackage.getAnnotationsByType(MixinConfigOption.class);
                        if (inputPackageAnnotations.length > 1) {
                            LOGGER.warn(inputPackagePath + " had multiple mixin config option annotations, only using first!");
                        }
                        if (inputPackageAnnotations.length > 0) {
                            MixinConfigOption option = inputPackageAnnotations[0];
                            mixinOptions.add(inputPackageName);
                            return new MixinRuleRepresentation(inputPackageName, option);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).filter(Objects::nonNull)
                .sorted(Comparator.comparing(MixinRuleRepresentation::path))
                .collect(Collectors.toList());

        mixinPackages.removeAll(mixinOptions);
        StringBuilder errorMessage = new StringBuilder();
        for (String packageName : mixinPackages) {
            errorMessage.append("Mixin Package ").append(packageName).append(" contains files without corresponding MixinConfigOption annotation in a package-info.java file!\n");
        }
        if (!errorMessage.isEmpty()) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }

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