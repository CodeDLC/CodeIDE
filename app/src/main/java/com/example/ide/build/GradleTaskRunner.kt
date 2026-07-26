package com.example.ide.build

import com.example.data.model.BuildLog
import com.example.data.model.DependencyItem
import com.example.data.model.LogLevel
import com.example.ide.syntax.CodeInspector
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object GradleTaskRunner {

    val COMMON_TASKS = listOf(
        "build" to "Assembles and tests this project",
        "assembleDebug" to "Assembles debug APK / JAR",
        "runClient" to "Launches Minecraft Fabric Client",
        "genSources" to "Generates Minecraft & Fabric Loom source code mappings",
        "remapJar" to "Remaps intermediary mappings to named Fabric mod JAR"
    )

    fun runTask(taskName: String, projectFiles: Map<String, String>): Flow<BuildLog> = flow {
        emit(BuildLog(message = "Executing Gradle task: ./gradlew $taskName", level = LogLevel.INFO))
        delay(300)

        emit(BuildLog(message = "Starting Gradle Daemon (Fabric Loom v1.5)...", level = LogLevel.INFO))
        delay(400)

        // Validate all Java/Kotlin files in project first
        var hasErrors = false
        projectFiles.forEach { (path, content) ->
            if (path.endsWith(".java") || path.endsWith(".kt")) {
                val ext = if (path.endsWith(".java")) "java" else "kt"
                val errors = CodeInspector.inspectCode(content, ext)
                errors.forEach { err ->
                    hasErrors = true
                    val fileName = path.substringAfterLast("/")
                    emit(
                        BuildLog(
                            message = "$fileName:${err.line}:${err.column}: error: ${err.message}",
                            level = LogLevel.ERROR,
                            filePath = path,
                            lineNumber = err.line,
                            columnNumber = err.column
                        )
                    )
                }
            }
        }

        if (hasErrors) {
            emit(BuildLog(message = "BUILD FAILED in 2s with compilation errors. Tap red log lines to jump to error in editor.", level = LogLevel.ERROR))
            return@flow
        }

        emit(BuildLog(message = "> Task :compileJava", level = LogLevel.INFO))
        delay(500)

        emit(BuildLog(message = "> Task :processResources", level = LogLevel.INFO))
        delay(300)

        emit(BuildLog(message = "> Task :classes", level = LogLevel.INFO))
        delay(300)

        if (taskName == "remapJar" || taskName == "build") {
            emit(BuildLog(message = "> Task :jar", level = LogLevel.INFO))
            delay(300)
            emit(BuildLog(message = "> Task :remapJar", level = LogLevel.INFO))
            delay(400)
            emit(BuildLog(message = "Output artifact generated: build/libs/mod-1.0.0.jar", level = LogLevel.SUCCESS))
        }

        emit(BuildLog(message = "BUILD SUCCESSFUL in 3s", level = LogLevel.SUCCESS))
    }

    fun parseDependenciesFromGradle(buildGradleContent: String): List<DependencyItem> {
        val list = mutableListOf<DependencyItem>()

        // Default Fabric Core dependencies
        list.add(DependencyItem("Fabric API", "net.fabricmc.fabric-api", "fabric-api", "0.92.0+1.20.4", "Fabric API", true))
        list.add(DependencyItem("Yarn Mappings", "net.fabricmc", "yarn", "1.20.4+build.3:v2", "Yarn Mappings", true))
        list.add(DependencyItem("Fabric Loader", "net.fabricmc", "fabric-loader", "0.15.6", "Fabric Loader", true))

        if (buildGradleContent.contains("cloth-config")) {
            list.add(DependencyItem("Cloth Config API", "me.shedaniel", "cloth-config-fabric", "13.0.121", "Library", true))
        } else {
            list.add(DependencyItem("Cloth Config API", "me.shedaniel", "cloth-config-fabric", "13.0.121", "Library", false))
        }

        if (buildGradleContent.contains("modmenu")) {
            list.add(DependencyItem("Mod Menu", "com.terraformersmc", "modmenu", "9.0.0", "Mod", true))
        } else {
            list.add(DependencyItem("Mod Menu", "com.terraformersmc", "modmenu", "9.0.0", "Mod", false))
        }

        if (buildGradleContent.contains("rei")) {
            list.add(DependencyItem("Roughly Enough Items (REI)", "me.shedaniel", "RoughlyEnoughItems-fabric", "14.0.688", "Mod", true))
        } else {
            list.add(DependencyItem("RoughlyEnoughItems (REI)", "me.shedaniel", "RoughlyEnoughItems-fabric", "14.0.688", "Mod", false))
        }

        return list
    }
}
