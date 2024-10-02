plugins {
    id("java")
    id("idea")
    id("fabric-loom") version ("1.7.3")
    id("net.caffeinemc.mixin-config-plugin") version ("1.0-SNAPSHOT")
}

val MINECRAFT_VERSION: String by rootProject.extra
val PARCHMENT_VERSION: String? by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

base {
    archivesName.set("lithium-fabric")
}

repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    minecraft("com.mojang:minecraft:${MINECRAFT_VERSION}")
    mappings(loom.layered {
        officialMojangMappings()
        if (PARCHMENT_VERSION != null) {
            parchment("org.parchmentmc.data:parchment-${MINECRAFT_VERSION}:${PARCHMENT_VERSION}@zip")
        }
    })
    modImplementation("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")
    modImplementation("com.github.2No2Name:McTester:v0.4.2")

    fun addEmbeddedFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        modImplementation(module)
        include(module)
    }

    fun addCompileOnlyFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        modCompileOnly(module)
    }

    addCompileOnlyFabricModule("fabric-transfer-api-v1")


    implementation("com.google.code.findbugs:jsr305:3.0.1")

    implementation(project.project(":common").sourceSets.getByName("api").output)
    implementation(project.project(":common").sourceSets.getByName("main").output)

    compileOnly("net.caffeinemc:mixin-config-plugin:1.0-SNAPSHOT")
}

tasks.named("compileTestJava").configure {
    enabled = false
}

tasks.named("test").configure {
    enabled = false
}

//Mixin hotswap
afterEvaluate {
    loom.runs.configureEach {
        // https://fabricmc.net/wiki/tutorial:mixin_hotswaps
        vmArg("-javaagent:${ configurations.compileClasspath.get().find { it.name.contains("sponge-mixin") } }")
    }
}

loom {
    if (project(":common").file("src/main/resources/lithium.accesswidener").exists())
        accessWidenerPath.set(project(":common").file("src/main/resources/lithium.accesswidener"))

    @Suppress("UnstableApiUsage")
    mixin { defaultRefmapName.set("lithium-fabric.refmap.json") }

    runs {
        create("fabricClient") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run")
        }

        create("fabricServer") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("run")
        }

        create("testmodServer") {
            runDir("testserver");
            server()
            configName = "Fabric Testmod Server"
            ideConfigGenerated(project.rootProject == project)
            vmArg("-Dfabric.autoTest")
        }

        create("autoTestServer") {
            runDir("testserver");
            inherit(runs.named("testmodServer").get())
            server()
            configName = "Fabric Auto Testmod Server"
            ideConfigGenerated(project.rootProject == project)
            vmArg("-Dfabric.autoTest")
        }
    }
}

tasks {
    processResources {
        from(project.project(":common").sourceSets.main.get().resources)
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(zipTree(project.project(":common").tasks.jar.get().archiveFile))
    }

    remapJar.get().destinationDirectory = rootDir.resolve("build").resolve("libs")
}