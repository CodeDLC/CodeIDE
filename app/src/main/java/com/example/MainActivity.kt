package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.ProjectItem
import com.example.ui.screens.IdeMainScreen
import com.example.ui.screens.NewProjectDialog
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.CodeIDETheme
import com.example.ui.theme.IJBackground
import com.example.ui.viewmodel.IdeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: IdeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CodeIDETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = IJBackground
                ) {
                    CodeIdeApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun CodeIdeApp(viewModel: IdeViewModel) {
    val currentProject by viewModel.currentProject.collectAsState()
    val recentProjects by viewModel.recentProjects.collectAsState()

    var showNewProjectModal by remember { mutableStateOf(false) }

    if (currentProject == null) {
        WelcomeScreen(
            recentProjects = recentProjects,
            onNewProjectClick = { showNewProjectModal = true },
            onOpenFolderClick = {
                // Generate default Fabric Mod project if user opens standard directory
                viewModel.createNewProject("FabricMod", "fabricmod", "com.example.mod", "1.20.4", false)
            },
            onImportArchiveClick = {
                viewModel.createNewProject("ImportedMod", "importedmod", "com.example.mod", "1.20.4", false)
            },
            onSelectProject = { entity ->
                viewModel.loadProject(
                    ProjectItem(
                        id = entity.id,
                        name = entity.name,
                        path = entity.path,
                        modType = com.example.data.model.ModType.valueOf(entity.modType),
                        mcVersion = entity.mcVersion,
                        modId = entity.modId,
                        packageName = entity.packageName,
                        lastOpened = entity.lastOpened
                    )
                )
            }
        )

        if (showNewProjectModal) {
            NewProjectDialog(
                onDismiss = { showNewProjectModal = false },
                onCreateProject = { name, modId, pkg, mcVer, kt ->
                    viewModel.createNewProject(name, modId, pkg, mcVer, kt)
                }
            )
        }
    } else {
        IdeMainScreen(
            viewModel = viewModel,
            onOpenWelcomeScreen = {
                // Allow user to return to welcome screen if needed
            }
        )
    }
}
