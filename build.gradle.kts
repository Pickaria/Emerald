plugins {
    id("java")
    id("maven-publish")
    id("xyz.jpenilla.run-paper") version "2.3.0"
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
}

group = "fr.pickaria"
version = "1.1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pickaria.fr/releases")
    maven("https://maven.pickaria.fr/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
    maven("https://jitpack.io") // Vault
}

val exposedVersion: String by project
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("fr.pickaria:bookshelf:1.1-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.21:3.130.0")
    testImplementation("com.github.MilkBowl:VaultAPI:1.7.1")
    testImplementation("fr.pickaria:bookshelf:1.1-SNAPSHOT")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain(21)
    compilerOptions.javaParameters = true
}

tasks {
    test {
        useJUnitPlatform()
    }

    runServer {
        minecraftVersion("1.21.1")
    }

    jar {
        archiveFileName.set("${project.name}-${project.version}.jar")
        destinationDirectory.set(file("run/plugins"))
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "name" to project.name,
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}

publishing {
    repositories {
        maven {
            name = "pickariaRepository"
            url = uri(
                "https://maven.pickaria.fr/${if (version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"}"
            )
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = project.name
            from(components["java"])
        }
    }
}
