package com.devindeed.aurelay.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import uniffi.rust_engine.startStream
import uniffi.rust_engine.stopStream

@Composable
@Preview
fun App() {
    var targetIp by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Idle") }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF009688), // Teal
            secondary = androidx.compose.ui.graphics.Color(0xFFE91E63) // Pink
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Aurelay Desktop Sender",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = targetIp,
                    onValueChange = { targetIp = it },
                    label = { Text("Target IP Address") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    enabled = !isStreaming
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isStreaming) {
                            try {
                                stopStream()
                                isStreaming = false
                                statusMessage = "Stopped"
                            } catch (e: Exception) {
                                statusMessage = "Error stopping: ${e.message}"
                            }
                        } else {
                            if (targetIp.isBlank()) {
                                statusMessage = "Please enter a valid IP"
                            } else {
                                try {
                                    startStream(targetIp)
                                    isStreaming = true
                                    statusMessage = "Streaming to $targetIp..."
                                } catch (e: Exception) {
                                    statusMessage = "Error starting: ${e.message}"
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    modifier = Modifier.height(56.dp).width(200.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStreaming) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isStreaming) "STOP" else "START")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Status: $statusMessage",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isStreaming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun main() {
    // Initialize native library
    try {
        val osName = System.getProperty("os.name").lowercase()
        val libName = if (osName.contains("win")) "rust_engine.dll" else "librust_engine.so"

        val inputStream = object {}.javaClass.getResourceAsStream("/$libName")
        if (inputStream != null) {
            val tempFile = File.createTempFile("rust_engine", if (osName.contains("win")) ".dll" else ".so")
            tempFile.deleteOnExit()
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            // Set the system property for UniFFI to find the library
            // UniFFI generated code uses: System.getProperty("uniffi.component.$componentName.libraryOverride")
            System.setProperty("uniffi.component.rust_engine.libraryOverride", tempFile.absolutePath)
        } else {
            System.err.println("Could not find library $libName in resources. Assuming it's in java.library.path")
        }
    } catch (e: Exception) {
        System.err.println("Error loading native library: ${e.message}")
        e.printStackTrace()
    }

    application {
        Window(onCloseRequest = ::exitApplication, title = "Aurelay Sender") {
            App()
        }
    }
}
