plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.15.5"
}

val minecraftVersion = project.property("minecraft_version") as String
val yarnMappings = project.property("yarn_mappings") as String
val loaderVersion = project.property("loader_version") as String
val fabricApiVersion = project.property("fabric_api_version") as String
val javaVersion = project.property("java_version") as String
val modVersion = project.findProperty("mod_version") as String? ?: "0.1.0"
val clothConfigVersion = project.findProperty("cloth_config_version") as String? ?: "21.11.153"
val modMenuVersion = project.findProperty("modmenu_version") as String? ?: "17.0.0"

version = "${modVersion}+mc${minecraftVersion}"
group = "pl.inh.tidy"

base {
    archivesName.set("tidy")
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.nucleoid.xyz/")
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings("net.fabricmc:yarn:${yarnMappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${clothConfigVersion}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modCompileOnly("com.terraformersmc:modmenu:${modMenuVersion}") {
        exclude(group = "net.fabricmc", module = "fabric-loader")
        exclude(group = "net.fabricmc.fabric-api")
    }
    modLocalRuntime("com.terraformersmc:modmenu:${modMenuVersion}") {
        exclude(group = "net.fabricmc", module = "fabric-loader")
        exclude(group = "net.fabricmc.fabric-api")
    }
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
