plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    application
}

repositories {
    jcenter()
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.3.2")

    val logbackVersion = "1.2.3"
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:6.3") {
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("io.github.microutils:kotlin-logging:1.7.8")
    implementation("org.slf4j:slf4j-api:1.7.30")

    // cli parsing
    implementation("com.github.ajalt:clikt:2.4.0")

    // config
    implementation("com.uchuhimo:konf:0.22.1") {
        exclude(module = "konf-git")
        exclude(module = "konf-hocon")
        exclude(module = "konf-xml")
        exclude(module = "konf-yaml")
    }

    // api
    val http4kVersion = "3.244.0"
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-server-undertow:$http4kVersion")
    implementation("org.http4k:http4k-client-okhttp:$http4kVersion")

    // command execution management
    implementation("org.zeroturnaround:zt-exec:1.11")

    val jacksonVersion = "2.10.1"
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")

    testImplementation("io.kotlintest:kotlintest-assertions:3.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.0.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClassName = "chaperone.AppKt"
}
