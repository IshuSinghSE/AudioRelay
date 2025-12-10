package com.devindeed.aurelay.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration


object AdsManager {
    fun initialize(context: Context) {
        // Force test device IDs during development to avoid serving live ads
        // Include emulator plus your physical test device (Redmi Note 14)
        // Provided device identifier: a28d0f01-d4a2-4d31-ae72-632c77dbb682
        val testDeviceIds = listOf(
            AdRequest.DEVICE_ID_EMULATOR,
            "a28d0f01-d4a2-4d31-ae72-632c77dbb682"
        )
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)
        MobileAds.initialize(context) { }
    }

    fun loadBanner(adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {}
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {}
        }
    }
}
