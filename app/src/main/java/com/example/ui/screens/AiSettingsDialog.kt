package com.example.ui.screens

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AiProvider
import com.example.data.model.AiSettings
import com.example.ui.theme.IJAccentBlue
import com.example.ui.theme.IJBackground
import com.example.ui.theme.IJBorder
import com.example.ui.theme.IJHeader
import com.example.ui.theme.IJTextPrimary
import com.example.ui.theme.IJTextSecondary

@Composable
fun AiSettingsDialog(
    currentSettings: AiSettings,
    onDismiss: () -> Unit,
    onSaveSettings: (AiSettings) -> Unit
) {
    var provider by remember { mutableStateOf(currentSettings.provider) }
    var apiKey by remember { mutableStateOf(currentSettings.apiKey) }
    var modelName by remember { mutableStateOf(currentSettings.modelName) }
    var customEndpoint by remember { mutableStateOf(currentSettings.customEndpoint) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = IJHeader),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(380.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "OpenCode AI Settings",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "API keys are encrypted locally via Android Keystore",
                    color = IJTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "AI Provider:",
                    color = IJTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Column {
                    AiProvider.values().forEach { prov ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { provider = prov }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = (provider == prov),
                                onClick = { provider = prov },
                                colors = RadioButtonDefaults.colors(selectedColor = IJAccentBlue)
                            )
                            Text(text = prov.name, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key", fontSize = 11.sp) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("Model Name (e.g. gemini-3.5-flash)", fontSize = 11.sp) },
                    colors = textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (provider == AiProvider.LOCAL_ENDPOINT || provider == AiProvider.OPEN_ROUTER) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customEndpoint,
                        onValueChange = { customEndpoint = it },
                        label = { Text("Custom Endpoint URL", fontSize = 11.sp) },
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
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
                            onSaveSettings(
                                AiSettings(
                                    provider = provider,
                                    apiKey = apiKey,
                                    modelName = modelName,
                                    customEndpoint = customEndpoint
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IJAccentBlue)
                    ) {
                        Text("Save Settings", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
