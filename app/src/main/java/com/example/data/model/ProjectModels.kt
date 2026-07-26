package com.example.data.model

import androidx.compose.runtime.Immutable

enum class ModType {
    FABRIC, FORGE, QUILT
}

@Immutable
data class ProjectItem(
    val id: Long = 0,
    val name: String,
    val path: String,
    val modType: ModType = ModType.FABRIC,
    val mcVersion: String = "1.20.4",
    val modId: String = "examplemod",
    val packageName: String = "com.example.mod",
    val lastOpened: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val githubRepo: String = ""
)

@Immutable
data class FileNode(
    val name: String,
    val relativePath: String,
    val isDirectory: Boolean,
    val children: List<FileNode> = emptyList(),
    val sizeBytes: Long = 0
) {
    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "")
}

@Immutable
data class EditorTab(
    val fileNode: FileNode,
    val content: String,
    val originalContent: String = content,
    val isModified: Boolean = false,
    val cursorOffset: Int = 0,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0
)

enum class LogLevel {
    INFO, WARNING, ERROR, SUCCESS
}

@Immutable
data class BuildLog(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val message: String,
    val filePath: String? = null,
    val lineNumber: Int? = null,
    val columnNumber: Int? = null
)

@Immutable
data class DependencyItem(
    val name: String,
    val groupId: String,
    val artifactId: String,
    val version: String,
    val category: String, // "Fabric API", "Yarn Mappings", "Fabric Loader", "Mod"
    val isInstalled: Boolean = false
)

@Immutable
data class GitStatusItem(
    val branch: String = "main",
    val modifiedFiles: List<String> = emptyList(),
    val stagedFiles: List<String> = emptyList(),
    val untrackedFiles: List<String> = emptyList(),
    val recentCommits: List<GitCommit> = emptyList()
)

@Immutable
data class GitCommit(
    val hash: String,
    val message: String,
    val author: String,
    val timestamp: Long
)

@Immutable
data class LocalHistorySnapshot(
    val id: Long = 0,
    val filePath: String,
    val timestamp: Long,
    val content: String,
    val changeDescription: String
)

enum class AiProvider {
    GEMINI, OPENAI, ANTHROPIC, OPEN_ROUTER, LOCAL_ENDPOINT
}

@Immutable
data class AiSettings(
    val provider: AiProvider = AiProvider.GEMINI,
    val apiKey: String = "",
    val modelName: String = "gemini-1.5-flash",
    val customEndpoint: String = "http://localhost:11434/v1"
)

@Immutable
data class CodeInspectionError(
    val line: Int,
    val column: Int,
    val message: String,
    val severity: InspectionSeverity,
    val quickFixActionName: String? = null,
    val quickFixReplacement: String? = null
)

enum class InspectionSeverity {
    ERROR, WARNING, INFO, HINT
}
