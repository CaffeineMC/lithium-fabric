plugins {
    id("java")
    id("idea")
    id("fabric-loom") version ("1.7.3")
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