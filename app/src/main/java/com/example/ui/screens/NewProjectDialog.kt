package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.IJAccentBlue
import com.example.ui.theme.IJBackground
import com.example.ui.theme.IJBorder
import com.example.ui.theme.IJHeader
import com.example.ui.theme.IJTextPrimary
import com.example.ui.theme.IJTextSecondary

@Composable
fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreateProject: (name: String, modId: String, packageName: String, mcVersion: String, useKotlin: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("MyFabricMod") }
    var modId by remember { mutableStateOf("myfabricmod") }
    var packageName by remember { mutableStateOf("com.example.mod") }
    var mcVersion by remember { mutableStateOf("1.20.4") }
    var useKotlin by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = IJHeader),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(380.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "New Fabric Mod Project",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Generates complete Fabric Loom workspace with Mixins",
                    color = IJTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        modId = it.lowercase().filter { char -> char.isLetterOrDigit() }
                    },
                    label = { Text("Mod Name", fontSize = 11.sp) },
                    colors = textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = modId,
                    onValueChange = { modId = it.lowercase() },
                    label = { Text("Mod ID", fontSize = 11.sp) },
                    colors = textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name", fontSize = 11.sp) },
                    colors = textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Minecraft Target Version:",
                    color = IJTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf("1.20.4", "1.20.1", "1.21").forEach { ver ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { mcVersion = ver }
                                .padding(end = 12.dp)
                        ) {
                            RadioButton(
                                selected = (mcVersion == ver),
                                onClick = { mcVersion = ver },
                                colors = RadioButtonDefaults.colors(selectedColor = IJAccentBlue)
                            )
                            Text(text = ver, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !useKotlin,
                        onClick = { useKotlin = false },
                        colors = RadioButtonDefaults.colors(selectedColor = IJAccentBlue)
                    )
                    Text(text = "Java 17", color = Color.White, fontSize = 11.sp)

                    Spacer(modifier = Modifier.width(12.dp))

                    RadioButton(
                        selected = useKotlin,
                        onClick = { useKotlin = true },
                        colors = RadioButtonDefaults.colors(selectedColor = IJAccentBlue)
                    )
                    Text(text = "Kotlin", color = Color.White, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = IJBackground)
                    ) {
                        Text("Cancel", color = IJTextSecondary, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank() && modId.isNotBlank()) {
                                onCreateProject(name, modId, packageName, mcVersion, useKotlin)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IJAccentBlue)
                    ) {
                        Text("Create Project", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = IJBackground,
    unfocusedContainerColor = IJBackground,
    focusedBorderColor = IJAccentBlue,
    unfocusedBorderColor = IJBorder,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
