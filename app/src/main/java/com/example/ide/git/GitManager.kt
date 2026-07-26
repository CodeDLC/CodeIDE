package com.example.ide.git

import com.example.data.model.GitCommit
import com.example.data.model.GitStatusItem

object GitManager {

    fun getGitStatus(
        projectFiles: Map<String, String>,
        originalFiles: Map<String, String>,
        currentBranch: String,
        commits: List<GitCommit>
    ): GitStatusItem {
        val modified = mutableListOf<String>()
        val untracked = mutableListOf<String>()

        projectFiles.forEach { (path, content) ->
            if (!originalFiles.containsKey(path)) {
                untracked.add(path)
            } else if (originalFiles[path] != content) {
                modified.add(path)
            }
        }

        return GitStatusItem(
            branch = currentBranch,
            modifiedFiles = modified,
            stagedFiles = emptyList(),
            untrackedFiles = untracked,
            recentCommits = commits
        )
    }

    fun computeDiff(oldText: String, newText: String): List<DiffLine> {
        val oldLines = oldText.lines()
        val newLines = newText.lines()
        val diffs = mutableListOf<DiffLine>()

        val maxLines = maxOf(oldLines.size, newLines.size)
        for (i in 0 until maxLines) {
            val oldLine = oldLines.getOrNull(i)
            val newLine = newLines.getOrNull(i)

            when {
                oldLine == newLine && oldLine != null -> {
                    diffs.add(DiffLine(i + 1, oldLine, DiffType.UNCHANGED))
                }
                oldLine != null && newLine != null -> {
                    diffs.add(DiffLine(i + 1, oldLine, DiffType.REMOVED))
                    diffs.add(DiffLine(i + 1, newLine, DiffType.ADDED))
                }
                oldLine != null -> {
                    diffs.add(DiffLine(i + 1, oldLine, DiffType.REMOVED))
                }
                newLine != null -> {
                    diffs.add(DiffLine(i + 1, newLine, DiffType.ADDED))
                }
            }
        }
        return diffs
    }
}

enum class DiffType {
    UNCHANGED, ADDED, REMOVED
}

data class DiffLine(
    val lineNumber: Int,
    val text: String,
    val type: DiffType
)
