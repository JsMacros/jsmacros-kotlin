import java.net.URI

plugins {
    kotlin("jvm") version "1.7.10"
}

base {
    archivesName.set(project.properties["archives_base_name"] as String)
}

group = project.properties["maven_group"] as String
version = project.properties["version"] as String

repositories {
    mavenCentral()
    maven {
        url = URI("https://jitpack.io")
        metadataSources {
            artifact()
        }
    }
}

val jsmacrosExtensionInclude by configurations.creating

dependencies {
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha7")
    implementation("com.google.code.gson:gson:2.9.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.0-alpha7")
    implementation("javassist:javassist:3.12.1.GA")
    implementation("com.github.jsmacros.jsmacros:jsmacros-1.19:${project.properties["jsmacros_version"]}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    implementation("org.jetbrains:annotations:23.0.0")

    implementation("org.jetbrains.kotlin:kotlin-scripting-common")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host")
    jsmacrosExtensionInclude("org.jetbrains.kotlin:kotlin-scripting-common")
    jsmacrosExtensionInclude("org.jetbrains.kotlin:kotlin-scripting-jvm")
    jsmacrosExtensionInclude("org.jetbrains.kotlin:kotlin-scripting-jvm-host")
}

tasks.processResources {
    filesMatching("jsmacros.ext.kotlin.json") {
        expand(mapOf(
            "dependencies" to jsmacrosExtensionInclude.files.joinToString(" ") { it.name }
        ))
    }
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "17"
}

tasks.jar {
    from("LICENSE")
    from(jsmacrosExtensionInclude.files) {
        include("*")
        into("META-INF/jsmacrosdeps")
    }
}

tasks.test {
    useJUnitPlatform()
}
