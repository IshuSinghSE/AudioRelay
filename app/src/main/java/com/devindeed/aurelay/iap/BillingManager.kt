package com.devindeed.aurelay.iap

import android.app.Activity

/**
 * Lightweight stub BillingManager for development.
 * Uses the `PurchaseManager` mock for UI/demo flows. Replace with real BillingClient integration
 * when you are ready to connect to Play Billing / RevenueCat.
 */
class BillingManager(private val context: android.content.Context) {

    fun startConnection(onReady: (() -> Unit)? = null) {
        // Immediately call ready in dev mode
        onReady?.invoke()
    }

    fun queryPurchases(onResult: (List<Any>) -> Unit) {
        // Return empty list for development
        onResult(emptyList())
    }

    fun purchaseRemoveAds(activity: Activity, productId: String) {
        // In development, rely on PurchaseManager mock instead of real billing
    }

    fun endConnection() {}
}
