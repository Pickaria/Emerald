plugins {
    id("java")
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
    id("xyz.jpenilla.run-paper") version "2.2.3"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
}

group = "fr.pickaria"
version = "0.1.0-beta.4-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pickaria.fr/releases")
    maven("https://maven.pickaria.fr/snapshot")
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
    maven("https://repo.aikar.co/content/groups/aikar/") // ACF
    maven("https://jitpack.io") // Vault
}

val exposedVersion: String by project
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("fr.pickaria:bedrock:1.2.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.h2database:h2:2.2.224")
    implementation("net.peanuuutz.tomlkt:tomlkt:0.3.7")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.20:3.78.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("co.aikar:acf-core:0.5.1-SNAPSHOT")
    testImplementation("com.github.MilkBowl:VaultAPI:1.7")
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
