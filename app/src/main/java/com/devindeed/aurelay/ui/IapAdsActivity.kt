package com.devindeed.aurelay.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.devindeed.aurelay.R
import com.devindeed.aurelay.ads.AdsManager
import com.devindeed.aurelay.iap.BillingManager
import com.google.android.gms.ads.AdView

class IapAdsActivity : AppCompatActivity() {

    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iap_ads)

        billingManager = BillingManager(this)
        billingManager.startConnection()

        AdsManager.initialize(this)
        val adView = findViewById<AdView>(R.id.adView)
        AdsManager.loadBanner(adView)

        findViewById(android.R.id.content).rootView.findViewById<android.widget.Button>(R.id.btn_purchase_remove_ads)?.setOnClickListener {
            // Replace with your product id
            billingManager.purchaseRemoveAds(this, "remove_ads_product_id")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.endConnection()
    }
}
