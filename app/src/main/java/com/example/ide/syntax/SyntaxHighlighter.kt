package com.example.ide.syntax

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.example.ui.theme.CodeAnnotation
import com.example.ui.theme.CodeComment
import com.example.ui.theme.CodeFunction
import com.example.ui.theme.CodeKeyword
import com.example.ui.theme.CodeNumber
import com.example.ui.theme.CodeString
import com.example.ui.theme.CodeType
import com.example.ui.theme.IJTextPrimary
import java.util.regex.Pattern

object SyntaxHighlighter {

    private val JAVA_KEYWORDS = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
        "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
        "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
        "volatile", "while", "true", "false", "null", "record"
    )

    private val KOTLIN_KEYWORDS = setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in", "interface",
        "is", "null", "object", "package", "return", "super", "this", "throw", "true", "try", "typealias",
        "val", "var", "when", "while", "by", "catch", "constructor", "delegate", "dynamic", "field",
        "file", "finally", "get", "import", "init", "param", "property", "receiver", "set", "setparam",
        "where", "actual", "abstract", "annotation", "companion", "const", "crossinline", "data", "enum",
        "expect", "external", "final", "infix", "inline", "inner", "internal", "lateinit", "noinline",
        "open", "operator", "out", "override", "private", "protected", "public", "reified", "sealed",
        "suspend", "tailrec", "vararg"
    )

    private val GROOVY_KEYWORDS = setOf(
        "plugins", "dependencies", "repositories", "implementation", "modImplementation", "minecraft",
        "mappings", "processResources", "tasks", "java", "def", "in", "as", "class", "import", "package"
    )

    fun highlight(code: String, fileExtension: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            addStyle(SpanStyle(color = IJTextPrimary), 0, code.length)

            when (fileExtension.lowercase()) {
                "java" -> highlightJava(code)
                "kt", "kts" -> highlightKotlin(code)
                "json", "mcmeta" -> highlightJson(code)
                "gradle", "groovy" -> highlightGroovy(code)
                "xml" -> highlightXml(code)
                "yaml", "yml" -> highlightYaml(code)
                else -> highlightJava(code)
            }
        }
    }

    private fun AnnotatedString.Builder.highlightJava(code: String) {
        // Comments
        highlightRegex(code, Pattern.compile("//.*$|/\\*.*?\\*/", Pattern.DOTALL or Pattern.MULTILINE), CodeComment)
        // Strings
        highlightRegex(code, Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'"), CodeString)
        // Annotations
        highlightRegex(code, Pattern.compile("@[A-Za-z0-9_]+"), CodeAnnotation)
        // Numbers
        highlightRegex(code, Pattern.compile("\\b0x[0-9a-fA-F]+\\b|\\b\\d+(\\.\\d+)?([fFdDLL])?\\b"), CodeNumber)
        // Keywords & Types
        highlightWords(code, JAVA_KEYWORDS, CodeKeyword)
        // Common Fabric/Minecraft Types
        highlightRegex(code, Pattern.compile("\\b(ModInitializer|Identifier|ItemStack|Item|Block|PlayerEntity|World|TitleScreen|Mixin|Inject|At|CallbackInfo|Logger|LoggerFactory)\\b"), CodeType)
    }

    private fun AnnotatedString.Builder.highlightKotlin(code: String) {
        highlightRegex(code, Pattern.compile("//.*$|/\\*.*?\\*/", Pattern.DOTALL or Pattern.MULTILINE), CodeComment)
        highlightRegex(code, Pattern.compile("\"\"\"[\\s\\S]*?\"\"\"|\"([^\"\\\\]|\\\\.)*\""), CodeString)
        highlightRegex(code, Pattern.compile("@[A-Za-z0-9_]+"), CodeAnnotation)
        highlightRegex(code, Pattern.compile("\\b0x[0-9a-fA-F]+\\b|\\b\\d+(\\.\\d+)?([fFdDLL])?\\b"), CodeNumber)
        highlightWords(code, KOTLIN_KEYWORDS, CodeKeyword)
        highlightRegex(code, Pattern.compile("\\b(ModInitializer|Identifier|ItemStack|Item|Block|PlayerEntity|World|TitleScreen|Logger|LoggerFactory)\\b"), CodeType)
    }

    private fun AnnotatedString.Builder.highlightJson(code: String) {
        // JSON Keys
        highlightRegex(code, Pattern.compile("\"[^\"]+\"\\s*:"), CodeKeyword)
        // Strings
        highlightRegex(code, Pattern.compile(":\\s*\"[^\"]*\""), CodeString)
        // Numbers & Booleans
        highlightRegex(code, Pattern.compile("\\b(true|false|null|\\d+(\\.\\d+)?)\\b"), CodeNumber)
    }

    private fun AnnotatedString.Builder.highlightGroovy(code: String) {
        highlightRegex(code, Pattern.compile("//.*$|/\\*.*?\\*/", Pattern.DOTALL or Pattern.MULTILINE), CodeComment)
        highlightRegex(code, Pattern.compile("'[^']*'|\"[^\"]*\""), CodeString)
        highlightWords(code, GROOVY_KEYWORDS, CodeKeyword)
        highlightRegex(code, Pattern.compile("\\b\\d+(\\.\\d+)?\\b"), CodeNumber)
    }

    private fun AnnotatedString.Builder.highlightXml(code: String) {
        highlightRegex(code, Pattern.compile("<!--.*?-->", Pattern.DOTALL), CodeComment)
        highlightRegex(code, Pattern.compile("<[^>]+>"), CodeKeyword)
        highlightRegex(code, Pattern.compile("\"[^\"]*\""), CodeString)
    }

    private fun AnnotatedString.Builder.highlightYaml(code: String) {
        highlightRegex(code, Pattern.compile("#.*$"), CodeComment)
        highlightRegex(code, Pattern.compile("^[a-zA-Z0-9_-]+:"), CodeKeyword)
        highlightRegex(code, Pattern.compile("\"[^\"]*\"|'[^']*'"), CodeString)
    }

    private fun AnnotatedString.Builder.highlightRegex(code: String, pattern: Pattern, color: androidx.compose.ui.graphics.Color) {
        val matcher = pattern.matcher(code)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            if (start >= 0 && end <= code.length && start < end) {
                addStyle(SpanStyle(color = color), start, end)
            }
        }
    }

    private fun AnnotatedString.Builder.highlightWords(code: String, words: Set<String>, color: androidx.compose.ui.graphics.Color) {
        val pattern = Pattern.compile("\\b(" + words.joinToString("|") + ")\\b")
        highlightRegex(code, pattern, color)
    }
}
