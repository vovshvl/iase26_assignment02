plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    application
}

group = "de.seuhd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
}

application {
    mainClass.set("de.seuhd.worldcup.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(25)
}

tasks.register("repeatTests") {
    group = "verification"
    description = "Runs the test suite 20 times to expose flaky tests"
    doLast {
        val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")
        val gradleCmd = if (isWindows) listOf("cmd", "/c", "gradlew.bat") else listOf("./gradlew")
        val runs = 20
        var passes = 0
        var fails = 0
        repeat(runs) { i ->
            val process = ProcessBuilder(gradleCmd + listOf("test", "--rerun-tasks"))
                .directory(projectDir)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readLines()
            val exitCode = process.waitFor()
            val label = if (exitCode == 0) { passes++; "PASS" } else { fails++; "FAIL" }
            val info = output.filter { "tests completed," in it }
            println("Run ${i + 1}: $label | $info")
            output.filter { " FAILED" in it && " > " in it }
                  .forEach { println("  FAILED: ${it.substringAfter(" > ").removeSuffix(" FAILED").trim()}") }
            println()
        }
        println("\nSummary: $passes/$runs passed, $fails/$runs failed")
    }
}
