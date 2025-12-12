package com.devindeed.aurelay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.devindeed.aurelay.ui.components.DeviceRow
import com.devindeed.aurelay.ui.components.LargeActionButton
import com.devindeed.aurelay.ui.components.Sidebar
import com.devindeed.aurelay.ui.AurelayTheme

@Composable
fun ConnectScreen() {
    AurelayTheme {
        Row(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))) {

            Sidebar(modifier = Modifier.fillMaxHeight().padding(12.dp))

            Spacer(modifier = Modifier.width(12.dp))

            Card(modifier = Modifier.weight(1f).fillMaxHeight().padding(12.dp)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LargeActionButton(text = "Stop")
                    Spacer(modifier = Modifier.padding(16.dp))
                    Text("Broadcasting Audio", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Card(modifier = Modifier.width(380.dp).fillMaxHeight().padding(12.dp)) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Text("Nearby Devices & Stats", color = Color.White)
                    Spacer(modifier = Modifier.padding(8.dp))
                    val devices = remember { mutableStateListOf(
                        "Apple Device-1",
                        "Apple Device 2",
                        "AudioDevice 3",
                        "AudioDevice 4",
                        "Another Connected",
                        "Apple Airchel4"
                    ) }
                    for (d in devices) {
                        DeviceRow(name = d, subtitle = "21 ms • 34Mbps")
                    }
                }
            }
        }
    }
}
