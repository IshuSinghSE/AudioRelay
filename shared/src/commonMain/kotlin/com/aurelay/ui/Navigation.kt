package com.devindeed.aurelay.ui

/**
 * Forwarding alias to the navigation `Screen` sealed class.
 * This keeps existing imports of `com.devindeed.aurelay.ui.Screen` working
 * while the canonical definition lives in `ui.navigation.Screen`.
 */
typealias Screen = com.devindeed.aurelay.ui.navigation.Screen
