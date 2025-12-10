package com.devindeed.aurelay.ui

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.devindeed.aurelay.BuildConfig
import com.devindeed.aurelay.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

@Composable
fun SmartAdBanner(
    isPremium: Boolean = false,
    modifier: Modifier = Modifier,
    adUnitId: String? = null,
) {
    // Respect compile-time kill switch & premium status
    if (isPremium || !BuildConfig.ENABLE_ADS) return

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Initialize MobileAds (One-time setup)
    LaunchedEffect(Unit) {
        try {
            // Using standard emulator ID + your specific test device ID
            val testDeviceIds = listOf(AdRequest.DEVICE_ID_EMULATOR, "a28d0f01-d4a2-4d31-ae72-632c77dbb682")
            val req = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
            MobileAds.setRequestConfiguration(req)
            MobileAds.initialize(context) {}
        } catch (e: Exception) {
            Log.w("SmartAdBanner", "AdMob init failed: ${e.message}")
        }
    }

    val resolvedAdUnitId = adUnitId ?: context.getString(R.string.admob_banner_id)
    var isAdLoaded by remember { mutableStateOf(false) }

    // Adaptive Banner Sizing
    val screenWidthDp = configuration.screenWidthDp.coerceAtLeast(320)
    val estimatedAdSize = try {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
    } catch (e: Exception) {
        AdSize.BANNER
    }
    val estimatedHeightDp = with(density) { estimatedAdSize.getHeightInPixels(context).toDp() }

    // Shimmer Animation Setup
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_float"
    )

    // Common Colors for Skeleton
    val baseColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val highlightColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    
    // Dynamic Shimmer Brush
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(estimatedHeightDp)
            .background(MaterialTheme.colorScheme.surface) // Background behind ad
    ) {
        // --- 1. SKELETON PLACEHOLDER (Visible when loading) ---
        if (!isAdLoaded) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App Icon Skeleton
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(shimmerBrush)
                )

                // Text Lines Skeleton
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Headline
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Description / Advertiser
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                }

                // CTA Button Skeleton (e.g., "Install")
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp)) // Pill shape
                        .background(shimmerBrush)
                )
            }
            
            // tiny "Ad" badge overlay
            Text(
                text = "Ad",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 9.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp, top = 2.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 2.dp)
            )
        }

        // --- 2. REAL AD VIEW ---
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(estimatedAdSize)
                    this.adUnitId = resolvedAdUnitId
                    // Start invisible to prevent flicker
                    alpha = 0f 
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d("SmartAdBanner", "Ad loaded")
                            isAdLoaded = true
                            alpha = 1f // Fade/Snap in
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e("SmartAdBanner", "Ad failed: ${error.message}")
                            isAdLoaded = false
                            alpha = 0f
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}