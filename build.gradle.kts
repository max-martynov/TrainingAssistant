import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "6.6.1"
    id("com.dorongold.task-tree") version "1.5"
}

group = "ru.emkn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val junitVersion = "5.6.2"

    implementation(kotlin("stdlib"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation(kotlin("test"))
}

tasks.withType(KotlinCompile::class.java) {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.shadowJar {
    archiveBaseName.set("runnable")
    archiveClassifier.set("")
    mergeServiceFiles()

    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}

val runJar by tasks.creating(Exec::class) {
    dependsOn(tasks.shadowJar)
    val argvString = project.findProperty("argv") as String? ?: ""
    val jarFile = tasks.shadowJar.get().outputs.files.singleFile
    val evalArgs = listOf("java", "-jar", jarFile.absolutePath) + argvString.split(" ")
    commandLine(*evalArgs.toTypedArray())

    val allFilesInGit = mutableListOf<File>()
    File(".git").walkTopDown().forEach {
        allFilesInGit.add(it)
    }
    println(allFilesInGit.size)
}