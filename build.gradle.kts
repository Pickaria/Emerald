plugins {
    id("java")
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
    id("xyz.jpenilla.run-paper") version "2.2.3"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
}

group = "fr.pickaria"
version = "0.2.0-beta.2-SNAPSHOT"

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
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("fr.pickaria:bookshelf:1.0-SNAPSHOT")

    testImplementation(kotlin("test"))
    testImplementation("com.github.MilkBowl:VaultAPI:1.7.1")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.20:3.78.0")
    testImplementation("fr.pickaria:bookshelf:1.0-SNAPSHOT")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain(17)
}

tasks {
    test {
        useJUnitPlatform()
    }

    runServer {
        minecraftVersion("1.20.4")
    }

    jar {
        archiveFileName.set("${project.name}.jar")
        destinationDirectory.set(file("run/plugins"))

        manifest {
            attributes["Main-Class"] = "fr.pickaria.emerald.MainKt"
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
