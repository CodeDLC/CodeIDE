package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuildLog
import com.example.data.model.LogLevel
import com.example.ui.theme.IJBackground
import com.example.ui.theme.IJBorder
import com.example.ui.theme.IJGreenSuccess
import com.example.ui.theme.IJHeader
import com.example.ui.theme.IJRedError
import com.example.ui.theme.IJTextPrimary
import com.example.ui.theme.IJTextSecondary
import com.example.ui.theme.IJWarningOrange

@Composable
fun ConsoleOutputView(
    logs: List<BuildLog>,
    activeTabName: String?,
    onTaskClick: (String) -> Unit,
    onOpenErrorFile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(IJHeader)
    ) {
        // Bottom Tool Bar Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(IJBackground)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf("CONSOLE", "TERMINAL", "GIT LOG", "PROBLEMS")
            tabs.forEach { tab ->
                val isSelected = tab == (activeTabName ?: "CONSOLE")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) IJHeader else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else IJTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(IJBorder)
        )

        // Logs Output List
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Gradle Console Ready. Tap 'Build' or 'Cloud' to start compilation.",
                    color = IJTextSecondary,
                    fontSize = 11.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(logs) { log ->
                    val color = when (log.level) {
                        LogLevel.INFO -> IJTextPrimary
                        LogLevel.WARNING -> IJWarningOrange
                        LogLevel.ERROR -> IJRedError
                        LogLevel.SUCCESS -> IJGreenSuccess
                    }

                    val isClickable = log.filePath != null

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isClickable) {
                                log.filePath?.let { onOpenErrorFile(it) }
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = log.message,
                            color = color,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (log.level == LogLevel.ERROR) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
