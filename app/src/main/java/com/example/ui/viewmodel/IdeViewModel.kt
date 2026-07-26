package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.db.LocalHistoryEntity
import com.example.data.db.ProjectEntity
import com.example.data.model.AiProvider
import com.example.data.model.AiSettings
import com.example.data.model.BuildLog
import com.example.data.model.CodeInspectionError
import com.example.data.model.DependencyItem
import com.example.data.model.EditorTab
import com.example.data.model.FileNode
import com.example.data.model.GitCommit
import com.example.data.model.GitStatusItem
import com.example.data.model.LogLevel
import com.example.data.model.ModType
import com.example.data.model.ProjectItem
import com.example.ide.ai.OpenCodeAiEngine
import com.example.ide.build.CloudBuildManager
import com.example.ide.build.CloudBuildStatus
import com.example.ide.build.GradleTaskRunner
import com.example.ide.git.DiffLine
import com.example.ide.git.GitManager
import com.example.ide.refactor.RefactoringEngine
import com.example.ide.refactor.StructureItem
import com.example.ide.refactor.SymbolUsage
import com.example.ide.syntax.AutoCompleter
import com.example.ide.syntax.CodeInspector
import com.example.ide.templates.FabricTemplateGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class AiMessage(
    val id: Long = System.currentTimeMillis(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val suggestedCode: String? = null
)

class IdeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(application, AppDatabase::class.java, "codeide_db").build()
    private val dao = db.projectDao()

    val recentProjects: StateFlow<List<ProjectEntity>> = dao.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Workspace State
    private val _currentProject = MutableStateFlow<ProjectItem?>(null)
    val currentProject: StateFlow<ProjectItem?> = _currentProject.asStateFlow()

    private val _vfsFiles = MutableStateFlow<MutableMap<String, String>>(mutableMapOf())
    val vfsFiles: StateFlow<Map<String, String>> = _vfsFiles.asStateFlow()

    private val _originalVfsFiles = MutableStateFlow<Map<String, String>>(mapOf())

    private val _projectTree = MutableStateFlow<FileNode?>(null)
    val projectTree: StateFlow<FileNode?> = _projectTree.asStateFlow()

    private val _openTabs = MutableStateFlow<List<EditorTab>>(emptyList())
    val openTabs: StateFlow<List<EditorTab>> = _openTabs.asStateFlow()

    private val _activeTab = MutableStateFlow<EditorTab?>(null)
    val activeTab: StateFlow<EditorTab?> = _activeTab.asStateFlow()

    private val _inspections = MutableStateFlow<List<CodeInspectionError>>(emptyList())
    val inspections: StateFlow<List<CodeInspectionError>> = _inspections.asStateFlow()

    private val _structureItems = MutableStateFlow<List<StructureItem>>(emptyList())
    val structureItems: StateFlow<List<StructureItem>> = _structureItems.asStateFlow()

    // Tool Windows State
    private val _activeBottomTool = MutableStateFlow<String?>("CONSOLE") // CONSOLE, TERMINAL, GIT, PROBLEMS
    val activeBottomTool: StateFlow<String?> = _activeBottomTool.asStateFlow()

    private val _isLeftSidebarOpen = MutableStateFlow(true)
    val isLeftSidebarOpen: StateFlow<Boolean> = _isLeftSidebarOpen.asStateFlow()

    private val _isAiDrawerOpen = MutableStateFlow(false)
    val isAiDrawerOpen: StateFlow<Boolean> = _isAiDrawerOpen.asStateFlow()

    // Console & Cloud Build
    private val _buildLogs = MutableStateFlow<List<BuildLog>>(emptyList())
    val buildLogs: StateFlow<List<BuildLog>> = _buildLogs.asStateFlow()

    private val _cloudBuildStatus = MutableStateFlow(CloudBuildStatus.IDLE)
    val cloudBuildStatus: StateFlow<CloudBuildStatus> = _cloudBuildStatus.asStateFlow()

    private val _githubToken = MutableStateFlow("")
    val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    // Git
    private val _gitStatus = MutableStateFlow(GitStatusItem())
    val gitStatus: StateFlow<GitStatusItem> = _gitStatus.asStateFlow()

    private val _activeDiff = MutableStateFlow<List<DiffLine>?>(null)
    val activeDiff: StateFlow<List<DiffLine>?> = _activeDiff.asStateFlow()

    // Dependencies
    private val _dependencies = MutableStateFlow<List<DependencyItem>>(emptyList())
    val dependencies: StateFlow<List<DependencyItem>> = _dependencies.asStateFlow()

    // AI Assistant
    private val _aiMessages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage(sender = "AI", text = "Welcome to OpenCode AI! I am your Fabric Mod development assistant. How can I help you today?")
        )
    )
    val aiMessages: StateFlow<List<AiMessage>> = _aiMessages.asStateFlow()

    private val _aiSettings = MutableStateFlow(AiSettings())
    val aiSettings: StateFlow<AiSettings> = _aiSettings.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Global Search / Double Shift
    private val _searchResults = MutableStateFlow<List<SymbolUsage>>(emptyList())
    val searchResults: StateFlow<List<SymbolUsage>> = _searchResults.asStateFlow()

    fun updateGithubToken(token: String) {
        _githubToken.value = token
    }

    fun updateAiSettings(settings: AiSettings) {
        _aiSettings.value = settings
    }

    fun setBottomTool(toolName: String?) {
        _activeBottomTool.value = toolName
    }

    fun toggleLeftSidebar() {
        _isLeftSidebarOpen.value = !_isLeftSidebarOpen.value
    }

    fun toggleAiDrawer() {
        _isAiDrawerOpen.value = !_isAiDrawerOpen.value
    }

    // --- PROJECT CREATION & OPENING ---

    fun createNewProject(
        name: String,
        modId: String,
        packageName: String,
        mcVersion: String,
        useKotlin: Boolean
    ) {
        viewModelScope.launch {
            val appDir = getApplication<Application>().filesDir
            val projectsDir = File(appDir, "projects")
            projectsDir.mkdirs()

            FabricTemplateGenerator.createFabricModProject(
                baseDir = projectsDir,
                modName = name,
                modId = modId,
                packageName = packageName,
                mcVersion = mcVersion,
                useKotlin = useKotlin
            )

            val rootFile = File(projectsDir, modId.lowercase())
            val projectItem = ProjectItem(
                name = name,
                path = rootFile.absolutePath,
                modType = ModType.FABRIC,
                mcVersion = mcVersion,
                modId = modId,
                packageName = packageName
            )

            val id = dao.insertProject(ProjectEntity.fromProjectItem(projectItem))
            loadProject(projectItem.copy(id = id))
        }
    }

    fun loadProject(projectItem: ProjectItem) {
        viewModelScope.launch {
            _currentProject.value = projectItem
            dao.updateProject(ProjectEntity.fromProjectItem(projectItem.copy(lastOpened = System.currentTimeMillis())))

            val projectDir = File(projectItem.path)
            if (!projectDir.exists()) projectDir.mkdirs()

            val vfs = mutableMapOf<String, String>()
            readDirectoryRecursive(projectDir, projectDir.absolutePath, vfs)

            _vfsFiles.value = vfs
            _originalVfsFiles.value = vfs.toMap()

            val tree = buildFileTree(projectDir, projectDir.absolutePath)
            _projectTree.value = tree

            // Parse dependencies
            val buildGradleContent = vfs["build.gradle"] ?: ""
            _dependencies.value = GradleTaskRunner.parseDependenciesFromGradle(buildGradleContent)

            // Open main entry point file automatically
            val mainClass = vfs.keys.firstOrNull { it.endsWith("Mod.java") || it.endsWith("Mod.kt") }
                ?: vfs.keys.firstOrNull { it.endsWith("fabric.mod.json") }
                ?: vfs.keys.firstOrNull()

            if (mainClass != null) {
                openFile(mainClass)
            }

            refreshGitStatus()
        }
    }

    private fun readDirectoryRecursive(dir: File, rootPath: String, outMap: MutableMap<String, String>) {
        dir.listFiles()?.forEach { file ->
            val relPath = file.absolutePath.substringAfter(rootPath).removePrefix("/")
            if (file.isDirectory) {
                if (!file.name.startsWith(".") && file.name != "build") {
                    readDirectoryRecursive(file, rootPath, outMap)
                }
            } else {
                val content = try { file.readText() } catch (e: Exception) { "" }
                outMap[relPath] = content
            }
        }
    }

    private fun buildFileTree(dir: File, rootPath: String): FileNode {
        val relPath = dir.absolutePath.substringAfter(rootPath).removePrefix("/")
        val children = dir.listFiles()
            ?.filter { !it.name.startsWith(".") && it.name != "build" }
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?.map {
                if (it.isDirectory) buildFileTree(it, rootPath)
                else FileNode(it.name, it.absolutePath.substringAfter(rootPath).removePrefix("/"), false, sizeBytes = it.length())
            } ?: emptyList()

        return FileNode(dir.name, relPath, true, children)
    }

    // --- EDITOR & CODE MANAGEMENT ---

    fun openFile(relativePath: String) {
        val content = _vfsFiles.value[relativePath] ?: ""
        val fileName = relativePath.substringAfterLast("/")
        val node = FileNode(fileName, relativePath, false)

        val existingTab = _openTabs.value.find { it.fileNode.relativePath == relativePath }
        if (existingTab != null) {
            _activeTab.value = existingTab
        } else {
            val newTab = EditorTab(node, content, originalContent = content)
            _openTabs.value = _openTabs.value + newTab
            _activeTab.value = newTab
        }

        reInspectActiveFile()
    }

    fun updateActiveCode(newContent: String) {
        val current = _activeTab.value ?: return
        val updatedTab = current.copy(content = newContent, isModified = newContent != current.originalContent)

        _activeTab.value = updatedTab
        _openTabs.value = _openTabs.value.map { if (it.fileNode.relativePath == current.fileNode.relativePath) updatedTab else it }
        _vfsFiles.value[current.fileNode.relativePath] = newContent

        reInspectActiveFile()
    }

    fun saveActiveFile() {
        val tab = _activeTab.value ?: return
        val project = _currentProject.value ?: return
        val file = File(project.path, tab.fileNode.relativePath)
        try {
            file.parentFile?.mkdirs()
            file.writeText(tab.content)

            // Save to Local History DB
            viewModelScope.launch {
                dao.insertHistory(
                    LocalHistoryEntity(
                        filePath = tab.fileNode.relativePath,
                        timestamp = System.currentTimeMillis(),
                        content = tab.content,
                        changeDescription = "File Saved"
                    )
                )
            }

            val savedTab = tab.copy(originalContent = tab.content, isModified = false)
            _activeTab.value = savedTab
            _openTabs.value = _openTabs.value.map { if (it.fileNode.relativePath == tab.fileNode.relativePath) savedTab else it }

            refreshGitStatus()
        } catch (e: Exception) {
            addLog("Failed to save file: ${e.message}", LogLevel.ERROR)
        }
    }

    fun closeTab(tab: EditorTab) {
        val newTabs = _openTabs.value.filter { it.fileNode.relativePath != tab.fileNode.relativePath }
        _openTabs.value = newTabs
        if (_activeTab.value?.fileNode?.relativePath == tab.fileNode.relativePath) {
            _activeTab.value = newTabs.lastOrNull()
        }
    }

    private fun reInspectActiveFile() {
        val tab = _activeTab.value ?: return
        val ext = tab.fileNode.extension
        val errors = CodeInspector.inspectCode(tab.content, ext)
        _inspections.value = errors
        _structureItems.value = RefactoringEngine.parseClassStructure(tab.content)
    }

    fun applyQuickFix(fix: CodeInspectionError) {
        if (fix.quickFixReplacement != null) {
            updateActiveCode(fix.quickFixReplacement)
        }
    }

    // --- REFACTORING & GLOBAL SEARCH ---

    fun performGlobalSearch(query: String) {
        _searchResults.value = RefactoringEngine.findUsages(query, _vfsFiles.value)
    }

    fun renameSymbolInProject(oldSymbol: String, newSymbol: String) {
        val mutableVfs = _vfsFiles.value.toMutableMap()
        val count = RefactoringEngine.renameSymbol(oldSymbol, newSymbol, mutableVfs)
        _vfsFiles.value = mutableVfs

        // Update active tab if changed
        val tab = _activeTab.value
        if (tab != null && mutableVfs.containsKey(tab.fileNode.relativePath)) {
            val newCode = mutableVfs[tab.fileNode.relativePath] ?: ""
            updateActiveCode(newCode)
        }
        addLog("Refactoring Complete: Renamed '$oldSymbol' -> '$newSymbol' in $count files.", LogLevel.SUCCESS)
    }

    // --- BUILD & GRADLE TASKS ---

    fun runGradleTask(taskName: String) {
        _activeBottomTool.value = "CONSOLE"
        viewModelScope.launch {
            GradleTaskRunner.runTask(taskName, _vfsFiles.value).collect { log ->
                addLog(log)
            }
        }
    }

    fun startCloudBuild() {
        _activeBottomTool.value = "CONSOLE"
        val proj = _currentProject.value ?: return

        viewModelScope.launch {
            CloudBuildManager.startCloudBuild(
                githubToken = _githubToken.value,
                repoOwner = "developer",
                repoName = proj.modId,
                files = _vfsFiles.value
            ).collect { progress ->
                _cloudBuildStatus.value = progress.status
                progress.logs.forEach { addLog(it) }
            }
        }
    }

    private fun addLog(log: BuildLog) {
        _buildLogs.value = _buildLogs.value + log
    }

    private fun addLog(message: String, level: LogLevel) {
        addLog(BuildLog(message = message, level = level))
    }

    // --- GIT MANAGEMENT ---

    private fun refreshGitStatus() {
        _gitStatus.value = GitManager.getGitStatus(
            projectFiles = _vfsFiles.value,
            originalFiles = _originalVfsFiles.value,
            currentBranch = "main",
            commits = listOf(
                GitCommit("a1b2c3d", "Initial Fabric mod commit from CodeIDE", "CodeIDE", System.currentTimeMillis() - 86400000)
            )
        )
    }

    fun commitGitChanges(message: String) {
        _originalVfsFiles.value = _vfsFiles.value.toMap()
        refreshGitStatus()
        addLog("Git Commit Created: $message", LogLevel.SUCCESS)
    }

    fun showFileDiff(relativePath: String) {
        val oldText = _originalVfsFiles.value[relativePath] ?: ""
        val newText = _vfsFiles.value[relativePath] ?: ""
        _activeDiff.value = GitManager.computeDiff(oldText, newText)
    }

    fun clearDiff() {
        _activeDiff.value = null
    }

    // --- OPENCODE AI ASSISTANT ---

    fun sendAiPrompt(userPrompt: String) {
        if (userPrompt.isBlank()) return

        val userMsg = AiMessage(sender = "USER", text = userPrompt)
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiThinking.value = true

        val activeCode = _activeTab.value?.content ?: ""
        val activeFile = _activeTab.value?.fileNode?.relativePath ?: ""

        viewModelScope.launch {
            val response = OpenCodeAiEngine.processUserPrompt(
                prompt = userPrompt,
                selectedCode = activeCode,
                currentFileName = activeFile,
                buildLogs = _buildLogs.value,
                aiSettings = _aiSettings.value
            )

            val codeSnippet = if (response.contains("```java") || response.contains("```kt")) {
                response.substringAfter("```").substringAfter("\n").substringBefore("```").trim()
            } else null

            _aiMessages.value = _aiMessages.value + AiMessage(sender = "AI", text = response, suggestedCode = codeSnippet)
            _isAiThinking.value = false
        }
    }
}
