import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
}

val asmVersion = "9.4"

fun asm(module: String? = null, version: String? = asmVersion) =
    "org.ow2.asm:asm${module?.let { "-$it" } ?: ""}${version?.let { ":$it" }}"

repositories {
    mavenCentral()
}

@Suppress("SpellCheckingInspection")
dependencies {
    implementation(platform(kotlin("bom")))
    implementation("org.jetbrains:annotations:24.0.0")

    implementation(kotlin("reflect"))
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    testImplementation(kotlin("test-junit5"))
    implementation(asm())
    implementation(asm("analysis"))
    implementation(asm("commons"))
    implementation(asm("tree"))
    implementation(asm("util"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
