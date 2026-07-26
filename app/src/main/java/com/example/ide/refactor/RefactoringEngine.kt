package com.example.ide.refactor

import androidx.compose.runtime.Immutable

@Immutable
data class SymbolUsage(
    val filePath: String,
    val lineNumber: Int,
    val lineText: String,
    val matchStart: Int,
    val matchEnd: Int
)

enum class StructureKind {
    CLASS, INTERFACE, ENUM, METHOD, FIELD, ANNOTATION
}

@Immutable
data class StructureItem(
    val name: String,
    val kind: StructureKind,
    val lineNumber: Int,
    val detail: String = ""
)

object RefactoringEngine {

    fun findUsages(symbol: String, files: Map<String, String>): List<SymbolUsage> {
        val usages = mutableListOf<SymbolUsage>()
        if (symbol.isBlank()) return usages

        val regex = Regex("\\b${Regex.escape(symbol)}\\b")

        files.forEach { (filePath, content) ->
            content.lines().forEachIndexed { lineIdx, lineText ->
                regex.findAll(lineText).forEach { match ->
                    usages.add(
                        SymbolUsage(
                            filePath = filePath,
                            lineNumber = lineIdx + 1,
                            lineText = lineText.trim(),
                            matchStart = match.range.first,
                            matchEnd = match.range.last + 1
                        )
                    )
                }
            }
        }
        return usages
    }

    fun renameSymbol(oldSymbol: String, newSymbol: String, files: MutableMap<String, String>): Int {
        if (oldSymbol.isBlank() || newSymbol.isBlank()) return 0
        var modifiedFilesCount = 0
        val regex = Regex("\\b${Regex.escape(oldSymbol)}\\b")

        val fileKeys = files.keys.toList()
        fileKeys.forEach { path ->
            val content = files[path] ?: ""
            if (regex.containsMatchIn(content)) {
                val updated = regex.replace(content, newSymbol)
                files[path] = updated
                modifiedFilesCount++
            }
        }
        return modifiedFilesCount
    }

    fun parseClassStructure(content: String): List<StructureItem> {
        val items = mutableListOf<StructureItem>()
        if (content.isBlank()) return items

        content.lines().forEachIndexed { index, line ->
            val trimmed = line.trim()
            val lineNum = index + 1

            when {
                trimmed.startsWith("public class ") || trimmed.startsWith("class ") -> {
                    val className = trimmed.substringAfter("class ").substringBefore(" ").substringBefore("{").trim()
                    items.add(StructureItem(className, StructureKind.CLASS, lineNum))
                }
                trimmed.startsWith("public interface ") || trimmed.startsWith("interface ") -> {
                    val ifaceName = trimmed.substringAfter("interface ").substringBefore(" ").substringBefore("{").trim()
                    items.add(StructureItem(ifaceName, StructureKind.INTERFACE, lineNum))
                }
                trimmed.startsWith("public enum ") || trimmed.startsWith("enum ") -> {
                    val enumName = trimmed.substringAfter("enum ").substringBefore(" ").substringBefore("{").trim()
                    items.add(StructureItem(enumName, StructureKind.ENUM, lineNum))
                }
                (trimmed.contains("void ") || trimmed.contains("fun ") || trimmed.contains("public ") || trimmed.contains("private ")) &&
                trimmed.contains("(") && trimmed.contains(")") && !trimmed.startsWith("//") -> {
                    val methodName = trimmed.substringBefore("(").substringAfterLast(" ").trim()
                    if (methodName.isNotEmpty()) {
                        items.add(StructureItem(methodName, StructureKind.METHOD, lineNum, trimmed))
                    }
                }
                (trimmed.startsWith("public static ") || trimmed.startsWith("private ") || trimmed.startsWith("val ") || trimmed.startsWith("var ")) &&
                trimmed.contains("=") && !trimmed.contains("(") -> {
                    val fieldName = trimmed.substringBefore("=").trim().substringAfterLast(" ")
                    if (fieldName.isNotEmpty()) {
                        items.add(StructureItem(fieldName, StructureKind.FIELD, lineNum, trimmed))
                    }
                }
            }
        }

        return items
    }
}
