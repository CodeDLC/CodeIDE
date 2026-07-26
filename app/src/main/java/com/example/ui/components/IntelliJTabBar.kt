package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EditorTab
import com.example.ui.theme.IJAccentBlue
import com.example.ui.theme.IJBackground
import com.example.ui.theme.IJBorder
import com.example.ui.theme.IJHeader

@Composable
fun IntelliJTabBar(
    openTabs: List<EditorTab>,
    activeTab: EditorTab?,
    onSelectTab: (EditorTab) -> Unit,
    onCloseTab: (EditorTab) -> Unit,
    modifier: Modifier = Modifier
) {
    if (openTabs.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(IJHeader)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        openTabs.forEach { tab ->
            val isActive = tab.fileNode.relativePath == activeTab?.fileNode?.relativePath

            Box(
                modifier = Modifier
                    .height(36.dp)
                    .background(if (isActive) IJBackground else IJHeader)
                    .clickable { onSelectTab(tab) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tab.fileNode.name,
                        color = if (isActive) Color.White else Color(0xFF868A91),
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
                    )

                    if (tab.isModified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "●",
                            color = IJAccentBlue,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close tab",
                        tint = if (isActive) Color(0xFFDFE1E5) else Color(0xFF868A91),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onCloseTab(tab) }
                    )
                }

                // Active Tab bottom highlight indicator
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(IJAccentBlue)
                            .align(Alignment.BottomCenter)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(IJBorder)
            )
        }
    }
}
