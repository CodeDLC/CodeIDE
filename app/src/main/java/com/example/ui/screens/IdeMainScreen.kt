package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.components.AiAssistantDrawer
import com.example.ui.components.CodeEditorView
import com.example.ui.components.ConsoleOutputView
import com.example.ui.components.IntelliJTabBar
import com.example.ui.components.IntelliJTopBar
import com.example.ui.components.ProjectTreeView
import com.example.ui.theme.IJBackground
import com.example.ui.viewmodel.IdeViewModel

@Composable
fun IdeMainScreen(
    viewModel: IdeViewModel,
    onOpenWelcomeScreen: () -> Unit
) {
    val project by viewModel.currentProject.collectAsState()
    val projectTree by viewModel.projectTree.collectAsState()
    val openTabs by viewModel.openTabs.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val inspections by viewModel.inspections.collectAsState()

    val isLeftSidebarOpen by viewModel.isLeftSidebarOpen.collectAsState()
    val isAiDrawerOpen by viewModel.isAiDrawerOpen.collectAsState()
    val buildLogs by viewModel.buildLogs.collectAsState()
    val activeBottomTool by viewModel.activeBottomTool.collectAsState()

    val aiMessages by viewModel.aiMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val aiSettings by viewModel.aiSettings.collectAsState()

    val searchResults by viewModel.searchResults.collectAsState()

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showDependencyDialog by remember { mutableStateOf(false) }
    var showAiSettingsDialog by remember { mutableStateOf(false) }
    var showGlobalSearchDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            IntelliJTopBar(
                projectName = project?.name ?: "",
                onToggleSidebar = { viewModel.toggleLeftSidebar() },
                onRunBuild = { viewModel.runGradleTask("build") },
                onCloudBuild = { viewModel.startCloudBuild() },
                onToggleAi = { viewModel.toggleAiDrawer() },
                onOpenSearch = { showGlobalSearchDialog = true },
                onOpenSettings = { showAiSettingsDialog = true }
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(IJBackground)
        ) {
            // Left Collapsible Tool Window: Project Tree
            if (isLeftSidebarOpen) {
                ProjectTreeView(
                    rootNode = projectTree,
                    activeFilePath = activeTab?.fileNode?.relativePath,
                    onOpenFile = { path -> viewModel.openFile(path) }
                )
            }

            // Central Canvas: Editor & Bottom Output Panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Opened Files Tabs Bar
                IntelliJTabBar(
                    openTabs = openTabs,
                    activeTab = activeTab,
                    onSelectTab = { tab -> viewModel.openFile(tab.fileNode.relativePath) },
                    onCloseTab = { tab -> viewModel.closeTab(tab) }
                )

                // Code Editor View
                CodeEditorView(
                    activeTab = activeTab,
                    inspections = inspections,
                    onCodeChange = { newCode -> viewModel.updateActiveCode(newCode) },
                    onApplyQuickFix = { fix -> viewModel.applyQuickFix(fix) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Console / Build Output View
                ConsoleOutputView(
                    logs = buildLogs,
                    activeTabName = activeBottomTool,
                    onTaskClick = { task -> viewModel.runGradleTask(task) },
                    onOpenErrorFile = { path -> viewModel.openFile(path) }
                )
            }

            // Right Collapsible Tool Window: OpenCode AI Drawer
            if (isAiDrawerOpen) {
                AiAssistantDrawer(
                    messages = aiMessages,
                    isThinking = isAiThinking,
                    onSendPrompt = { prompt -> viewModel.sendAiPrompt(prompt) },
                    onApplyCode = { code -> viewModel.updateActiveCode(code) },
                    onCloseDrawer = { viewModel.toggleAiDrawer() },
                    onOpenSettings = { showAiSettingsDialog = true }
                )
            }
        }
    }

    // Modals
    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onCreateProject = { name, modId, pkg, mcVer, kt ->
                viewModel.createNewProject(name, modId, pkg, mcVer, kt)
            }
        )
    }

    if (showDependencyDialog) {
        DependencyManagerDialog(
            dependencies = viewModel.dependencies.collectAsState().value,
            onDismiss = { showDependencyDialog = false },
            onToggleDependency = { dep -> }
        )
    }

    if (showAiSettingsDialog) {
        AiSettingsDialog(
            currentSettings = aiSettings,
            onDismiss = { showAiSettingsDialog = false },
            onSaveSettings = { newSettings -> viewModel.updateAiSettings(newSettings) }
        )
    }

    if (showGlobalSearchDialog) {
        GlobalSearchDialog(
            searchResults = searchResults,
            onSearchQuery = { q -> viewModel.performGlobalSearch(q) },
            onSelectMatch = { path, line -> viewModel.openFile(path) },
            onDismiss = { showGlobalSearchDialog = false }
        )
    }
}
