package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import kotlinx.coroutines.delay

import androidx.compose.foundation.clickable

@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit
) {
  var startAnimation by remember { mutableStateOf(true) }
  val alphaAnim = animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0.8f,
    animationSpec = tween(durationMillis = 600),
    label = "splash_alpha"
  )

  LaunchedEffect(key1 = true) {
    delay(1000) // Show splash for 1.0 second
    onSplashFinished()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFE2D3B8))
      .clickable { onSplashFinished() },
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(id = R.drawable.splash_logo),
      contentDescription = "Startup Splash Screen",
      modifier = Modifier
        .fillMaxSize()
        .alpha(alphaAnim.value),
      contentScale = ContentScale.Crop
    )
  }
}
