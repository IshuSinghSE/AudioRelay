package com.devindeed.aurelay.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devindeed.aurelay.engine.AudioEngine

@Composable
fun SettingsScreen(
    audioEngine: AudioEngine,
    modifier: Modifier = Modifier
) {
    // State
    var autoStartService by remember { mutableStateOf(false) }
    var audioOutputMode by remember { mutableStateOf("Audio") } // "Receive", "Audio"
    var appTheme by remember { mutableStateOf("App") } // "App", "Default"
    var dynamicColors by remember { mutableStateOf(true) }
    var showVolumeSlider by remember { mutableStateOf(true) }
    var showVisualizer by remember { mutableStateOf(true) }
    var audioVerification by remember { mutableStateOf(true) }
    var audioConnections by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        val isWide = maxWidth >= 900.dp
        
        if (isWide) {
            SettingsGrid(
                autoStartService = autoStartService, onAutoStartChange = { autoStartService = it },
                audioOutputMode = audioOutputMode, onAudioOutputChange = { audioOutputMode = it },
                appTheme = appTheme, onAppThemeChange = { appTheme = it },
                dynamicColors = dynamicColors, onDynamicColorsChange = { dynamicColors = it },
                showVolumeSlider = showVolumeSlider, onShowVolumeSliderChange = { showVolumeSlider = it },
                showVisualizer = showVisualizer, onShowVisualizerChange = { showVisualizer = it },
                audioVerification = audioVerification, onAudioVerificationChange = { audioVerification = it },
                audioConnections = audioConnections, onAudioConnectionsChange = { audioConnections = it }
            )
        } else {
            SettingsList(
                autoStartService = autoStartService, onAutoStartChange = { autoStartService = it },
                audioOutputMode = audioOutputMode, onAudioOutputChange = { audioOutputMode = it },
                appTheme = appTheme, onAppThemeChange = { appTheme = it },
                dynamicColors = dynamicColors, onDynamicColorsChange = { dynamicColors = it },
                showVolumeSlider = showVolumeSlider, onShowVolumeSliderChange = { showVolumeSlider = it },
                showVisualizer = showVisualizer, onShowVisualizerChange = { showVisualizer = it },
                audioVerification = audioVerification, onAudioVerificationChange = { audioVerification = it },
                audioConnections = audioConnections, onAudioConnectionsChange = { audioConnections = it }
            )
        }
    }
}

@Composable
fun SettingsGrid(
    autoStartService: Boolean, onAutoStartChange: (Boolean) -> Unit,
    audioOutputMode: String, onAudioOutputChange: (String) -> Unit,
    appTheme: String, onAppThemeChange: (String) -> Unit,
    dynamicColors: Boolean, onDynamicColorsChange: (Boolean) -> Unit,
    showVolumeSlider: Boolean, onShowVolumeSliderChange: (Boolean) -> Unit,
    showVisualizer: Boolean, onShowVisualizerChange: (Boolean) -> Unit,
    audioVerification: Boolean, onAudioVerificationChange: (Boolean) -> Unit,
    audioConnections: Boolean, onAudioConnectionsChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Column 1
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                SettingsCard {
                    SettingToggle(
                        title = "Auto-start Service",
                        description = "Automatically start receiver service when app launches",
                        checked = autoStartService,
                        onCheckedChange = onAutoStartChange
                    )
                }
                
                SettingsCard {
                    SettingSegmented(
                        title = "Audio Output",
                        description = "Ensure enable audio output",
                        options = listOf("Receive", "Audio"),
                        selectedOption = audioOutputMode,
                        onOptionSelected = onAudioOutputChange
                    )
                }
                
                SettingsCard {
                    SettingToggle(
                        title = "Audio Verification",
                        description = "Update certificate verification settings",
                        checked = audioVerification,
                        onCheckedChange = onAudioVerificationChange
                    )
                }
                
                SettingsCard {
                    SettingToggle(
                        title = "Audio Connections",
                        description = "Allow audio connections from external devices",
                        checked = audioConnections,
                        onCheckedChange = onAudioConnectionsChange
                    )
                }
            }

            // Column 2
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                SettingsCard {
                    SettingSegmented(
                        title = "App Theme",
                        description = "Set the app theme",
                        options = listOf("App", "Default"),
                        selectedOption = appTheme,
                        onOptionSelected = onAppThemeChange
                    )
                }
                
                SettingsCard {
                    SettingToggle(
                        title = "Dynamic Colors",
                        description = "Enable dynamic colors based on wallpaper",
                        checked = dynamicColors,
                        onCheckedChange = onDynamicColorsChange
                    )
                }
                
                SettingsCard {
                    SettingToggle(
                        title = "Show Volume Slider",
                        description = "Show volume slider on main screen",
                        checked = showVolumeSlider,
                        onCheckedChange = onShowVolumeSliderChange
                    )
                }
                
                SettingsCard {
                    SettingToggle(
                        title = "Show Audio Visualizer",
                        description = "Show audio visualizer animation",
                        checked = showVisualizer,
                        onCheckedChange = onShowVisualizerChange
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsList(
    autoStartService: Boolean, onAutoStartChange: (Boolean) -> Unit,
    audioOutputMode: String, onAudioOutputChange: (String) -> Unit,
    appTheme: String, onAppThemeChange: (String) -> Unit,
    dynamicColors: Boolean, onDynamicColorsChange: (Boolean) -> Unit,
    showVolumeSlider: Boolean, onShowVolumeSliderChange: (Boolean) -> Unit,
    showVisualizer: Boolean, onShowVisualizerChange: (Boolean) -> Unit,
    audioVerification: Boolean, onAudioVerificationChange: (Boolean) -> Unit,
    audioConnections: Boolean, onAudioConnectionsChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SettingsCard {
            SettingToggle(
                title = "Auto-start Service",
                description = "Automatically start receiver service",
                checked = autoStartService,
                onCheckedChange = onAutoStartChange
            )
        }

        SettingsCard {
            SettingSegmented(
                title = "Audio Output",
                description = "Ensure enable audio output",
                options = listOf("Receive", "Audio"),
                selectedOption = audioOutputMode,
                onOptionSelected = onAudioOutputChange
            )
        }

        SettingsCard {
            SettingSegmented(
                title = "App Theme",
                description = "Set the app theme",
                options = listOf("App", "Default"),
                selectedOption = appTheme,
                onOptionSelected = onAppThemeChange
            )
        }

        SettingsCard {
            SettingToggle(
                title = "Dynamic Colors",
                description = "Enable dynamic colors",
                checked = dynamicColors,
                onCheckedChange = onDynamicColorsChange
            )
        }

        SettingsCard {
            SettingToggle(
                title = "Show Volume Slider",
                description = "Show volume slider",
                checked = showVolumeSlider,
                onCheckedChange = onShowVolumeSliderChange
            )
        }

        SettingsCard {
            SettingToggle(
                title = "Show Audio Visualizer",
                description = "Show audio visualizer",
                checked = showVisualizer,
                onCheckedChange = onShowVisualizerChange
            )
        }
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )
    }
}

@Composable
fun SettingSegmented(
    title: String,
    description: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                OutlinedButton(
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        1.dp, 
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(option)
                }
            }
        }
    }
}
