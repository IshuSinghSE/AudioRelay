package com.devindeed.aurelay.ui

import androidx.compose.ui.window.singleWindowApplication
import com.devindeed.aurelay.ui.screens.ConnectScreen

fun main() = singleWindowApplication(title = "Connect Preview") {
    ConnectScreen()
}
