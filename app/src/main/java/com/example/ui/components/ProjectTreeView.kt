package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileNode
import com.example.ui.theme.IJAccentBlue
import com.example.ui.theme.IJHeader
import com.example.ui.theme.IJTextPrimary
import com.example.ui.theme.IJTextSecondary

@Composable
fun ProjectTreeView(
    rootNode: FileNode?,
    activeFilePath: String?,
    onOpenFile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (rootNode == null) {
        Box(
            modifier = modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(IJHeader)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Project Loaded",
                color = IJTextSecondary,
                fontSize = 12.sp
            )
        }
        return
    }

    Column(
        modifier = modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(IJHeader)
            .padding(vertical = 8.dp)
    ) {
        // Header
        Text(
            text = "PROJECT VIEW",
            color = IJTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(rootNode.children) { child ->
                FileTreeNodeItem(
                    node = child,
                    activeFilePath = activeFilePath,
                    onOpenFile = onOpenFile,
                    depth = 0
                )
            }
        }
    }
}

@Composable
private fun FileTreeNodeItem(
    node: FileNode,
    activeFilePath: String?,
    onOpenFile: (String) -> Unit,
    depth: Int
) {
    var isExpanded by remember { mutableStateOf(depth < 2) }
    val isActive = activeFilePath == node.relativePath

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(if (isActive) IJAccentBlue else Color.Transparent)
                .clickable {
                    if (node.isDirectory) {
                        isExpanded = !isExpanded
                    } else {
                        onOpenFile(node.relativePath)
                    }
                }
                .padding(
                    start = (depth * 14 + 10).dp,
                    end = 8.dp,
                    top = 4.dp,
                    bottom = 4.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (node.isDirectory) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = "Expand",
                    tint = IJTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Folder",
                    tint = Color(0xFFE2A03F),
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(20.dp))
                // File Type Badge Icon
                val badgeColor = when (node.extension) {
                    "java" -> Color(0xFFF14C4C)
                    "kt" -> Color(0xFF8E44AD)
                    "json" -> Color(0xFF3574F0)
                    "gradle" -> Color(0xFF499C54)
                    else -> IJTextSecondary
                }

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = node.extension.take(1).uppercase().ifEmpty { "F" },
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = node.name,
                color = if (isActive) Color.White else IJTextPrimary,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (node.isDirectory && isExpanded) {
            node.children.forEach { child ->
                FileTreeNodeItem(
                    node = child,
                    activeFilePath = activeFilePath,
                    onOpenFile = onOpenFile,
                    depth = depth + 1
                )
            }
        }
    }
}
