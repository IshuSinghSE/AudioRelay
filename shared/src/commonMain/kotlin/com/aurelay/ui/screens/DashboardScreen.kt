package com.devindeed.aurelay.ui.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devindeed.aurelay.engine.AudioEngine

// Enums for UI state
enum class AppMode {
    Receiver, Sender
}

enum class HeroState {
    Idle,           // Sender: Ready to start
    Broadcasting,   // Sender: Streaming
    Disconnected,   // Receiver: Waiting
    Connected       // Receiver: Listening (Optional)
}

@Composable
fun DashboardScreen(
    audioEngine: AudioEngine,
    modifier: Modifier = Modifier
) {
    // State
    var appMode by remember { mutableStateOf(AppMode.Sender) }
    // For now, we simulate HeroState based on AppMode and a local toggle.
    // In a real app, this would come from audioEngine.streamState
    var isBroadcasting by remember { mutableStateOf(false) }
    
    val heroState = when (appMode) {
        AppMode.Sender -> if (isBroadcasting) HeroState.Broadcasting else HeroState.Idle
        AppMode.Receiver -> HeroState.Disconnected // Placeholder for Receiver state
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        val isWide = maxWidth >= 900.dp
        
        if (isWide) {
            DesktopLayout(
                appMode = appMode,
                heroState = heroState,
                onModeChanged = { appMode = it },
                onHeroClick = { 
                    if (appMode == AppMode.Sender) isBroadcasting = !isBroadcasting 
                }
            )
        } else {
            MobileLayout(
                appMode = appMode,
                heroState = heroState,
                onModeChanged = { appMode = it },
                onHeroClick = { 
                    if (appMode == AppMode.Sender) isBroadcasting = !isBroadcasting 
                }
            )
        }
    }
}

@Composable
fun MobileLayout(
    appMode: AppMode,
    heroState: HeroState,
    onModeChanged: (AppMode) -> Unit,
    onHeroClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top: Segmented Control
        SegmentedModeToggle(
            selectedMode = appMode,
            onModeChanged = onModeChanged
        )

        // Center: Hero
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            HeroControl(
                state = heroState,
                onClick = onHeroClick
            )
        }

        // Bottom: Status Text
        StatusText(heroState)
        
        Spacer(modifier = Modifier.height(32.dp)) // Bottom padding
    }
}

@Composable
fun DesktopLayout(
    appMode: AppMode,
    heroState: HeroState,
    onModeChanged: (AppMode) -> Unit,
    onHeroClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left Pane
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SegmentedModeToggle(
                selectedMode = appMode,
                onModeChanged = onModeChanged
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            HeroControl(
                state = heroState,
                onClick = onHeroClick
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            StatusText(heroState)
        }

        // Right Pane (Placeholder)
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(32.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Details & Settings Placeholder",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SegmentedModeToggle(
    selectedMode: AppMode,
    onModeChanged: (AppMode) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.height(48.dp).width(240.dp) // Fixed width for better look
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            AppMode.values().forEach { mode ->
                val isSelected = mode == selectedMode
                val transition = updateTransition(isSelected, label = "Selection")
                val bgColor by transition.animateColor(label = "BgColor") { selected ->
                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                }
                val contentColor by transition.animateColor(label = "ContentColor") { selected ->
                    if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(bgColor, RoundedCornerShape(50))
                        .clickable { onModeChanged(mode) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.name,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
fun HeroControl(
    state: HeroState,
    onClick: () -> Unit
) {
    val isBroadcasting = state == HeroState.Broadcasting
    
    // Pulse Animation for Broadcasting
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by if (isBroadcasting) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PulseScale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val glowAlpha by if (isBroadcasting) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowAlpha"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp)
    ) {
        // Glow Effect
        if (isBroadcasting) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.Red.copy(alpha = glowAlpha),
                    radius = size.minDimension / 2 * pulseScale
                )
            }
        }

        // Main Button
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = when (state) {
                HeroState.Broadcasting -> BorderStroke(4.dp, MaterialTheme.colorScheme.error)
                HeroState.Idle -> BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                HeroState.Disconnected -> BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
                else -> null
            },
            modifier = Modifier
                .size(200.dp)
                .let {
                    if (isBroadcasting) {
                        it.shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            ambientColor = MaterialTheme.colorScheme.error,
                            spotColor = MaterialTheme.colorScheme.error
                        )
                    } else it
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when (state) {
                        HeroState.Broadcasting -> Icons.Default.Stop
                        HeroState.Idle -> Icons.Default.PowerSettingsNew
                        HeroState.Disconnected -> Icons.Default.LinkOff
                        else -> Icons.Default.PowerSettingsNew
                    },
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = when (state) {
                        HeroState.Broadcasting -> MaterialTheme.colorScheme.error
                        HeroState.Idle -> MaterialTheme.colorScheme.primary
                        HeroState.Disconnected -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (state) {
                        HeroState.Broadcasting -> "STOP"
                        HeroState.Idle -> "Power"
                        HeroState.Disconnected -> "Disconnected"
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun StatusText(state: HeroState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = when (state) {
                HeroState.Broadcasting -> "Broadcasting Audio"
                HeroState.Idle -> "Ready to Broadcast"
                HeroState.Disconnected -> "Waiting for Connection"
                else -> ""
            },
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (state) {
                HeroState.Broadcasting -> "Listening on port 5000"
                HeroState.Idle -> "Tap to start stream"
                HeroState.Disconnected -> "Connect from a sender"
                else -> ""
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
