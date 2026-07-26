package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CodeInspectionError
import com.example.data.model.EditorTab
import com.example.ide.syntax.AutoCompleter
import com.example.ide.syntax.CompletionCandidate
import com.example.ide.syntax.SyntaxHighlighter
import com.example.ui.theme.IJAccentBlue
import com.example.ui.theme.IJBackground
import com.example.ui.theme.IJBorder
import com.example.ui.theme.IJHeader
import com.example.ui.theme.IJRedError
import com.example.ui.theme.IJTextPrimary
import com.example.ui.theme.IJTextSecondary
import com.example.ui.theme.IJWarningOrange

@Composable
fun CodeEditorView(
    activeTab: EditorTab?,
    inspections: List<CodeInspectionError>,
    onCodeChange: (String) -> Unit,
    onApplyQuickFix: (CodeInspectionError) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeTab == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(IJBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No open files in editor\nSelect a file from Project Tree to start modding",
                color = IJTextSecondary,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    var fontSizeSp by remember { mutableFloatStateOf(13f) }
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        fontSizeSp = (fontSizeSp * zoomChange).coerceIn(10f, 24f)
    }

    val code = activeTab.content
    val extension = activeTab.fileNode.extension
    val lines = code.lines()
    val scrollState = rememberScrollState()

    var showAutocomplete by remember { mutableStateOf(false) }
    var autocompleteCandidates by remember { mutableStateOf<List<CompletionCandidate>>(emptyList()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IJBackground)
            .transformable(state = transformState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Line Numbers Gutter
            Column(
                modifier = Modifier
                    .background(IJHeader)
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.End
            ) {
                lines.indices.forEach { idx ->
                    val lineNum = idx + 1
                    val hasError = inspections.any { it.line == lineNum }

                    Text(
                        text = "$lineNum",
                        color = if (hasError) IJRedError else IJTextSecondary,
                        fontSize = fontSizeSp.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (hasError) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(IJBorder)
            )

            // Main Code Text Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                BasicTextField(
                    value = code,
                    onValueChange = { newText ->
                        onCodeChange(newText)

                        // Trigger Autocomplete
                        val candidates = AutoCompleter.getSuggestions(newText, newText.length)
                        autocompleteCandidates = candidates
                        showAutocomplete = candidates.isNotEmpty()
                    },
                    textStyle = TextStyle(
                        color = IJTextPrimary,
                        fontSize = fontSizeSp.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = (fontSizeSp * 1.4f).sp
                    ),
                    cursorBrush = SolidColor(IJAccentBlue),
                    visualTransformation = {
                        val highlighted = SyntaxHighlighter.highlight(it.text, extension)
                        androidx.compose.ui.text.input.TransformedText(
                            highlighted,
                            androidx.compose.ui.text.input.OffsetMapping.Identity
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Inspections Quick Fix Popup Badge
        val topError = inspections.firstOrNull()
        if (topError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = IJHeader),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Quick Fix",
                        tint = IJWarningOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = topError.message,
                        color = IJRedError,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    if (topError.quickFixActionName != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(IJAccentBlue)
                                .clickable { onApplyQuickFix(topError) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = topError.quickFixActionName,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Auto-complete suggestion box overlay
        if (showAutocomplete && autocompleteCandidates.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = IJHeader),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 48.dp, bottom = 24.dp)
                    .size(width = 280.dp, height = 180.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text(
                        text = "IntelliJ Code Completion",
                        color = IJTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LazyColumn {
                        items(autocompleteCandidates) { candidate ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCodeChange(code + candidate.completionText)
                                        showAutocomplete = false
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(IJAccentBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = candidate.type.name.take(1),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = candidate.displayText,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (candidate.detail.isNotEmpty()) {
                                        Text(
                                            text = candidate.detail,
                                            color = IJTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
