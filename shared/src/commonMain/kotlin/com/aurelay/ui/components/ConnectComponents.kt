package com.devindeed.aurelay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LargeActionButton(text: String, size: Dp = 180.dp, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(12.dp, CircleShape)
            .background(Color(0xFFFF6F5E), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White)
    }
}

@Composable
fun DeviceRow(name: String, subtitle: String? = null, onClick: () -> Unit = {}) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Box(modifier = Modifier.size(36.dp).background(Color(0xFF3A3A3A), CircleShape))
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(name, color = Color.White)
                if (subtitle != null) Text(subtitle, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun Sidebar(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        repeat(4) {
            Box(modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF2F2F2F), CircleShape)
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
        }
    }
}
