package com.example.ide.syntax

import com.example.data.model.CodeInspectionError
import com.example.data.model.InspectionSeverity

object CodeInspector {

    fun inspectCode(code: String, fileExtension: String): List<CodeInspectionError> {
        val errors = mutableListOf<CodeInspectionError>()
        if (code.isBlank()) return errors

        val lines = code.lines()

        // 1. Bracket & Brace Matching Check
        var openBraces = 0
        var openParens = 0
        lines.forEachIndexed { index, line ->
            openBraces += line.count { it == '{' } - line.count { it == '}' }
            openParens += line.count { it == '(' } - line.count { it == ')' }

            // 2. Java Semicolon Check
            if (fileExtension.equals("java", ignoreCase = true)) {
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() &&
                    !trimmed.startsWith("//") &&
                    !trimmed.startsWith("/*") &&
                    !trimmed.startsWith("*") &&
                    !trimmed.startsWith("@") &&
                    !trimmed.endsWith("{") &&
                    !trimmed.endsWith("}") &&
                    !trimmed.endsWith(";") &&
                    !trimmed.endsWith(":") &&
                    !trimmed.startsWith("package") == false &&
                    !trimmed.startsWith("import") == false
                ) {
                    if (trimmed.startsWith("package ") || trimmed.startsWith("import ") ||
                        trimmed.contains(" = ") || trimmed.contains("return ") || trimmed.contains("System.out.")
                    ) {
                        errors.add(
                            CodeInspectionError(
                                line = index + 1,
                                column = line.length,
                                message = "Missing semicolon ';'",
                                severity = InspectionSeverity.ERROR,
                                quickFixActionName = "Add ';'",
                                quickFixReplacement = "$line;"
                            )
                        )
                    }
                }
            }

            // 3. Fabric Mixin Annotation Inspection
            if (line.contains("@Inject") && !line.contains("at = @At")) {
                errors.add(
                    CodeInspectionError(
                        line = index + 1,
                        column = 1,
                        message = "Mixin @Inject annotation missing mandatory 'at = @At(...) ' target",
                        severity = InspectionSeverity.ERROR,
                        quickFixActionName = "Add 'at = @At(\"HEAD\")'",
                        quickFixReplacement = line.replace("@Inject", "@Inject(at = @At(\"HEAD\"))")
                    )
                )
            }
        }

        // Unmatched Brace Warning
        if (openBraces != 0) {
            errors.add(
                CodeInspectionError(
                    line = lines.size,
                    column = 1,
                    message = "Unmatched curly braces: $openBraces unclosed '{'",
                    severity = InspectionSeverity.ERROR,
                    quickFixActionName = "Close all braces",
                    quickFixReplacement = code + "\n" + "}".repeat(maxOf(0, openBraces))
                )
            )
        }

        // 4. Missing Fabric Import Check
        if (code.contains("ModInitializer") && !code.contains("import net.fabricmc.api.ModInitializer;")) {
            errors.add(
                CodeInspectionError(
                    line = 1,
                    column = 1,
                    message = "Unresolved class 'ModInitializer'. Import net.fabricmc.api.ModInitializer?",
                    severity = InspectionSeverity.WARNING,
                    quickFixActionName = "Import net.fabricmc.api.ModInitializer",
                    quickFixReplacement = "import net.fabricmc.api.ModInitializer;\n$code"
                )
            )
        }

        return errors
    }
}
