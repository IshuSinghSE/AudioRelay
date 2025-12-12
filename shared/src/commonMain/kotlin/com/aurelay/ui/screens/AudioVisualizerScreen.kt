package com.devindeed.aurelay.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devindeed.aurelay.engine.AudioEngine
import com.devindeed.aurelay.engine.StreamState
import com.devindeed.aurelay.ui.components.Visualizer
import androidx.compose.runtime.collectAsState

@Composable
fun AudioVisualizerScreen(
    audioEngine: AudioEngine,
    modifier: Modifier = Modifier
) {
    val streamState by audioEngine.streamState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Audio",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = when (streamState) {
                StreamState.Streaming -> "Streaming live"
                StreamState.Starting -> "Preparing stream"
                StreamState.Stopping -> "Stopping"
                StreamState.Error -> "Error"
                else -> "Idle"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Visualizer(isStreaming = streamState == StreamState.Streaming)
        }
    }
}
