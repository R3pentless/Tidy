plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.7.4"
}

val modVersion: String by project
val minecraftVersion: String by project
val yarnMappings: String by project
val loaderVersion: String by project
val fabricApiVersion: String by project
val javaVersion: String by project

version = "${modVersion}+mc${minecraftVersion}"
group = "pl.inh.tidy"

base {
    archivesName.set("tidy")
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings("net.fabricmc:yarn:${yarnMappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props = mapOf(
        "mod_version"       to modVersion,
        "minecraft_version" to minecraftVersion,
        "loader_version"    to loaderVersion,
        "fabric_api_version" to fabricApiVersion
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_tidy" }
    }
}
