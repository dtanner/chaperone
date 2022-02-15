plugins {
    kotlin("jvm") version "1.6.10"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

val targetJvm = 11
tasks {
    compileJava { options.release.set(targetJvm) }
    compileKotlin { kotlinOptions { jvmTarget = "$targetJvm" } }
    compileTestKotlin { kotlinOptions { jvmTarget = "$targetJvm" } }

    withType<Test> {
        useJUnitPlatform()
    }
}

application {
    mainClass.set("chaperone.AppKt")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.0")

    implementation("ch.qos.logback:logback-classic:1.2.10")
    implementation("net.logstash.logback:logstash-logback-encoder:7.0.1") {
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("org.slf4j:slf4j-api:1.7.35")

    // cli parsing
    implementation("com.github.ajalt.clikt:clikt:3.4.0")

    // config
    implementation("com.uchuhimo:konf-toml:1.1.2")

    // cron scheduling
    implementation("com.cronutils:cron-utils:9.1.5")

    // api
    val http4kVersion = "4.19.3.0"
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-server-undertow:$http4kVersion")
    implementation("org.http4k:http4k-client-okhttp:$http4kVersion")

    // command execution management
    implementation("org.zeroturnaround:zt-exec:1.11")

    val jacksonVersion = "2.13.1"
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("io.kotest:kotest-assertions-core:5.1.0")
}
