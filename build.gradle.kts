plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.41"
    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    jcenter()
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    // kotlinOptions.allWarningsAsErrors = true
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.3.2")

    implementation("ch.qos.logback:logback-core:1.2.3")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.github.microutils:kotlin-logging:1.7.6")
    implementation("io.micrometer:micrometer-registry-influx:latest.release")

    // cli parsing
    implementation("com.github.ajalt:clikt:2.4.0")

    // config
    implementation("com.uchuhimo:konf:0.22.1")

    testImplementation("io.kotlintest:kotlintest-core:3.4.2")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("io.kotlintest:kotlintest-assertions:3.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.0.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    // Define the main class for the application.
    mainClassName = "chaperone.AppKt"
}
