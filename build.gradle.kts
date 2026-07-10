plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

tasks {
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(25)) } }

    withType<Test> {
        useJUnitPlatform()
    }
}

application {
    mainClass.set("chaperone.AppKt")
}

dependencies {
    implementation(libs.kotlinx.coroutines.reactive)

    implementation(libs.logback.classic)
    implementation(libs.logstash.logback.encoder) {
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.api)

    // cli parsing
    implementation(libs.clikt)

    // config
    implementation(libs.bundles.hoplite)

    // cron scheduling
    implementation(libs.cron.utils)

    // api
    implementation(libs.bundles.http4k)

    // command execution management
    implementation(libs.zt.exec)

    api(libs.bundles.jackson)

    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.kotest.assertions.core)
}
