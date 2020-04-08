plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
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

    implementation("ch.qos.logback:logback-core:1.2.3")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.github.microutils:kotlin-logging:1.7.6")

    // cli parsing
    implementation("com.github.ajalt:clikt:2.4.0")

    // config
    implementation("com.uchuhimo:konf:0.22.1")

    // api
    implementation("org.http4k:http4k-core:3.239.0")
    implementation("org.http4k:http4k-server-jetty:3.239.0")
    implementation("org.http4k:http4k-server-undertow:3.239.0")

    implementation("org.http4k:http4k-client-okhttp:3.239.0")

    // command execution management
    implementation("org.zeroturnaround:zt-exec:1.11")

    val jacksonVersion = "2.10.1"
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")

    testImplementation("io.kotlintest:kotlintest-core:3.4.2")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("io.kotlintest:kotlintest-assertions:3.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.0.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClassName = "chaperone.AppKt"
}
