import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.5.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.20"
    application
}
group = "projects"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    // ktor
    val ktor_version: String by project
    val logback_version: String by project
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")
    implementation("io.ktor:ktor-network:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-json:$ktor_version")
    implementation("io.ktor:ktor-client-serialization:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")

    implementation("com.zaxxer:HikariCP:5.0.0")

    // vk sdk
    val vk_sdk_version: String by project
    implementation("com.petersamokhin.vksdk:core:$vk_sdk_version")
    implementation("com.petersamokhin.vksdk:http-client-jvm-okhttp:$vk_sdk_version")
    implementation("com.petersamokhin.vksdk:http-client-common-ktor:$vk_sdk_version")

    // coroutines
    val coroutines_version: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$coroutines_version")

    // sql exposed
    val exposed_version: String by project
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("com.h2database:h2:1.4.197")

    // tests
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation(kotlin("test-junit5"))
}

tasks.withType<Test>().all {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

kotlin.sourceSets.all {
    languageSettings.apply {
        useExperimentalAnnotation("kotlin.time.ExperimentalTime")
    }
}

application {
    mainClassName = "ApplicationKt"
}
