
plugins {
    id("java")
    id("idea")
}

sourceSets {
    val main = getByName("main")
    val api = create("api")
    val gametest = create("gametest")

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

    gametest.apply {
        java.srcDir("src/gametest/java")
        resources.srcDir("src/gametest/resources")
    }
}

tasks.withType<JavaCompile>().configureEach {
    enabled = false
}


tasks.named<Copy>("processGametestResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Jar>("apiJar") {
    from(sourceSets["api"].output)
    archiveBaseName.set("lithium")
    archiveClassifier.set("api")
    destinationDirectory = rootDir.resolve("build").resolve("libs")
}

tasks.named<Jar>("jar") {
    from(sourceSets["api"].output.classesDirs)
    from(sourceSets["api"].output.resourcesDir)
}

tasks.named("build") {
    dependsOn("apiJar")
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