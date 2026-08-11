package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun DuolingoFlame(
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    isActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame_anim")

    // Pulsing scale for flickering fire
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    // Flame top tip sway oscillation
    val tipSway by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tip_sway"
    )

    // Inner core pulse
    val coreAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_alpha"
    )

    // Floating sparks offset
    val sparkOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spark_offset"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            if (!isActive) {
                // Inactive dim flame
                val inactivePath = Path().apply {
                    moveTo(w * 0.5f, h * 0.1f)
                    cubicTo(w * 0.85f, h * 0.4f, w * 0.95f, h * 0.75f, w * 0.5f, h * 0.95f)
                    cubicTo(w * 0.05f, h * 0.75f, w * 0.15f, h * 0.4f, w * 0.5f, h * 0.1f)
                    close()
                }
                drawPath(
                    path = inactivePath,
                    color = Color.Gray.copy(alpha = 0.4f)
                )
                return@Canvas
            }

            // Outer Fire Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF5722).copy(alpha = 0.35f * coreAlpha),
                        Color(0xFFFF9800).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.65f),
                    radius = w * 0.65f * flameScale
                )
            )

            // Outer Red-Orange Flame Layer
            val outerFlame = Path().apply {
                val topX = w * 0.5f + tipSway
                moveTo(topX, h * 0.05f)
                cubicTo(w * 0.88f * flameScale, h * 0.35f, w * 0.96f, h * 0.75f, w * 0.5f, h * 0.95f)
                cubicTo(w * 0.04f, h * 0.75f, w * 0.12f * (2f - flameScale), h * 0.35f, topX, h * 0.05f)
                close()
            }
            drawPath(
                path = outerFlame,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFFF3D00), // Fiery Red
                        Color(0xFFFF6D00), // Deep Orange
                        Color(0xFFFFAB00)  // Gold
                    )
                )
            )

            // Middle Bright Orange Flame Layer
            val midFlame = Path().apply {
                val midTopX = w * 0.5f - tipSway * 0.6f
                moveTo(midTopX, h * 0.22f)
                cubicTo(w * 0.78f, h * 0.45f, w * 0.82f, h * 0.78f, w * 0.5f, h * 0.92f)
                cubicTo(w * 0.18f, h * 0.78f, w * 0.22f, h * 0.45f, midTopX, h * 0.22f)
                close()
            }
            drawPath(
                path = midFlame,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFFF9100),
                        Color(0xFFFFC400)
                    )
                )
            )

            // Inner Golden Core
            val innerCore = Path().apply {
                moveTo(w * 0.5f, h * 0.42f)
                cubicTo(w * 0.68f, h * 0.58f, w * 0.72f, h * 0.82f, w * 0.5f, h * 0.90f)
                cubicTo(w * 0.28f, h * 0.82f, w * 0.32f, h * 0.58f, w * 0.5f, h * 0.42f)
                close()
            }
            drawPath(
                path = innerCore,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFEA00),
                        Color(0xFFFFFFFF)
                    )
                ),
                alpha = coreAlpha
            )

            // Rising Sparks / Embers
            val spark1Y = (h * 0.5f + sparkOffsetY) % h
            val spark2Y = (h * 0.7f + sparkOffsetY * 0.8f) % h
            drawCircle(
                color = Color(0xFFFFF59D),
                radius = 3.5f,
                center = Offset(w * 0.35f + tipSway, spark1Y)
            )
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = 2.5f,
                center = Offset(w * 0.68f - tipSway, spark2Y)
            )
        }
    }
}
