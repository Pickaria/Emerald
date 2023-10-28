plugins {
    id("java")
    id("maven-publish")
    id("xyz.jpenilla.run-paper") version "2.2.0"
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.10"
}

group = "fr.pickaria"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
    maven("https://repo.aikar.co/content/groups/aikar/") // ACF
}

val exposedVersion: String by project
dependencies {
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.h2database:h2:2.1.214")
    implementation("net.peanuuutz.tomlkt:tomlkt:0.3.7")

    testImplementation(kotlin("test"))
    testImplementation("com.github.seeseemelk:MockBukkit-v1.20:3.42.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("co.aikar:acf-core:0.5.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

kotlin {
    jvmToolchain(17)
}

tasks {
    test {
        useJUnitPlatform()
    }

    runServer {
        minecraftVersion("1.20.2")
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        destinationDirectory.set(file("run/plugins"))

        manifest {
            attributes["Main-Class"] = "fr.pickaria.emerald.MainKt"
        }

        from(configurations.runtimeClasspath.get().map { zipTree(it) })
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Pickaria/Emerald")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = "emerald"
            from(components["java"])
        }
    }
}
