package com.devindeed.aurelay

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.devindeed.aurelay.engine.AudioEngine
import com.devindeed.aurelay.ui.AurelayTheme
import com.devindeed.aurelay.ui.ThemeMode
import com.devindeed.aurelay.ui.screens.AudioVisualizerScreen
import com.devindeed.aurelay.ui.screens.DashboardScreen
import com.devindeed.aurelay.ui.screens.SettingsScreen

sealed class AppScreen(val label: String, val route: String, val icon: ImageVector) {
    data object Connect : AppScreen("Connect", "connect", Icons.Rounded.PowerSettingsNew)
    data object Audio : AppScreen("Audio", "audio", Icons.Rounded.GraphicEq)
    data object Settings : AppScreen("Settings", "settings", Icons.Rounded.Settings)

    companion object {
        val saver: Saver<AppScreen, String> = Saver(
            save = { it.route },
            restore = { route ->
                when (route) {
                    Connect.route -> Connect
                    Audio.route -> Audio
                    Settings.route -> Settings
                    else -> Connect
                }
            }
        )
    }
}

/**
 * Navigation shell with responsive rail/bar and animated content switching.
 */
@Composable
fun App(
    audioEngine: AudioEngine,
    themeMode: ThemeMode = ThemeMode.SYSTEM
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Robust breakpoint check
        val isWideScreen = maxWidth >= 800.dp
        val widthLabel = "maxWidth=$maxWidth"

        AurelayTheme(themeMode = themeMode) {
            var currentScreen by rememberSaveable(stateSaver = AppScreen.saver) { mutableStateOf<AppScreen>(AppScreen.Connect) }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isWideScreen) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        NavigationRail(
                            modifier = Modifier
                                .width(80.dp)
                                .fillMaxHeight(),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            val items = listOf(AppScreen.Connect, AppScreen.Audio, AppScreen.Settings)
                            items.forEach { screen ->
                                val selected = currentScreen == screen
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.label,
                                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    label = { Text(screen.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }

                        VerticalDivider()

                        Scaffold(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            containerColor = MaterialTheme.colorScheme.background
                        ) { paddingValues ->
                            Crossfade(
                                targetState = currentScreen,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                            ) { destination ->
                                when (destination) {
                                    AppScreen.Connect -> DashboardScreen(audioEngine)
                                    AppScreen.Audio -> AudioVisualizerScreen(audioEngine)
                                    AppScreen.Settings -> SettingsScreen(audioEngine)
                                }
                            }
                        }
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            val items = listOf(AppScreen.Connect, AppScreen.Audio, AppScreen.Settings)
                            Column {
                                HorizontalDivider()
                                NavigationBar(
                                    modifier = Modifier.height(80.dp),
                                    containerColor = MaterialTheme.colorScheme.surface
                                ) {
                                    items.forEach { screen ->
                                        val selected = currentScreen == screen
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { currentScreen = screen },
                                            icon = {
                                                Icon(
                                                    imageVector = screen.icon,
                                                    contentDescription = screen.label,
                                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            label = { Text(screen.label) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { paddingValues ->
                        Crossfade(
                            targetState = currentScreen,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) { destination ->
                            when (destination) {
                                AppScreen.Connect -> DashboardScreen(audioEngine)
                                AppScreen.Audio -> AudioVisualizerScreen(audioEngine)
                                AppScreen.Settings -> SettingsScreen(audioEngine)
                            }
                        }
                    }
                }

                // Visual debug overlay for width
                Text(
                    text = widthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                )
            }
        }
    }
}
