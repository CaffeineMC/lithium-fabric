plugins {
    id("java")
    id("idea")
    id("fabric-loom") version ("1.7.3")
    id("net.caffeinemc.mixin-config-plugin") version ("1.0-SNAPSHOT")
}

repositories {
    maven("https://maven.parchmentmc.org/")
}


val MINECRAFT_VERSION: String by rootProject.extra
val PARCHMENT_VERSION: String? by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = MINECRAFT_VERSION)
    mappings(loom.layered() {
        officialMojangMappings()
        if (PARCHMENT_VERSION != null) {
            parchment("org.parchmentmc.data:parchment-${MINECRAFT_VERSION}:${PARCHMENT_VERSION}@zip")
        }
    })

    modCompileOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    fun addDependentFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        modCompileOnly(module)
    }
    // example usage:
    //    addDependentFabricModule("fabric-block-view-api-v2")

    compileOnly("net.caffeinemc:mixin-config-plugin:1.0-SNAPSHOT")
}

sourceSets {
    val main = getByName("main")
    val api = create("api")

    api.apply {
        java {
            compileClasspath += main.compileClasspath
        }
    }

    main.apply {
        java {
            compileClasspath += api.output
            runtimeClasspath += api.output
        }
    }
}

loom {
    mixin {
        defaultRefmapName = "lithium.refmap.json"
    }

    accessWidenerPath = file("src/main/resources/lithium.accesswidener")

    mods {
        val main by creating { // to match the default mod generated for Forge
            sourceSet("api")
            sourceSet("main")
        }
    }
}

tasks {
    jar {
        from(rootDir.resolve("LICENSE.md"))

        val api = sourceSets.getByName("api")
        from(api.output.classesDirs)
        from(api.output.resourcesDir)
    }
}

// This trick hides common tasks in the IDEA list.
tasks.configureEach {
    group = null
}

tasks.named<net.caffeinemc.gradle.CreateMixinConfigTask>("createMixinConfig") {
    inputFiles.set(
            listOf(
                    tasks.named("compileJava", JavaCompile::class).get().destinationDirectory.get(),
                    project(":neoforge").tasks.named("compileJava", JavaCompile::class).get().destinationDirectory.get(),
                    project(":fabric").tasks.named("compileJava", JavaCompile::class).get().destinationDirectory.get()
            )
    )
    includeFiles.set(file("src/main/java/net/caffeinemc/mods/lithium"))
    outputDirectory.set(file("src/main/resources/assets/lithium/"))
    outputDirectoryForSummaryDocument = "."
    mixinParentPackages = listOf("net.caffeinemc.mods.lithium", "net.caffeinemc.mods.lithium.fabric", "net.caffeinemc.mods.lithium.neoforge")
    modShortName = "Lithium"

    dependsOn("compileJava")
    dependsOn(project(":fabric").tasks.named("compileJava", JavaCompile::class))
    dependsOn(project(":neoforge").tasks.named("compileJava", JavaCompile::class))
}

tasks.named<Jar>("jar") {
    dependsOn("createMixinConfig")
}

//TODO make run client and server tasks for fabric and neoforge depend on createMixinConfig / its output files