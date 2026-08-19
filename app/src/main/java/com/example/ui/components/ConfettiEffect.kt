package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val color: Color,
    val size: Float,
    val speedX: Float,
    val speedY: Float,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 80
) {
    val progress = remember { Animatable(0f) }

    val colors = remember {
        listOf(
            Color(0xFFFF3B30),
            Color(0xFFFF9500),
            Color(0xFFFFCC00),
            Color(0xFF34C759),
            Color(0xFF007AFF),
            Color(0xFF5856D6),
            Color(0xFFAF52DE),
            Color(0xFFFF2D55),
            Color(0xFF00E5FF),
            Color(0xFFFFD700)
        )
    }

    val particles = remember {
        List(particleCount) {
            ConfettiParticle(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat() * 0.3f, // start near top
                color = colors.random(),
                size = Random.nextFloat() * 12f + 8f,
                speedX = (Random.nextFloat() - 0.5f) * 350f,
                speedY = Random.nextFloat() * 600f + 300f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                isCircle = Random.nextBoolean()
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        val t = progress.value

        for (p in particles) {
            var currentX = p.initialX * canvasWidth + p.speedX * t
            currentX = ((currentX % canvasWidth) + canvasWidth) % canvasWidth
            val currentY = p.initialY * canvasHeight + p.speedY * t + 0.5f * 400f * t * t
            val alpha = (1f - (t * 0.9f)).coerceIn(0f, 1f)
            val currentRotation = p.rotationSpeed * t

            if (currentY in 0f..canvasHeight) {
                rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                    if (p.isCircle) {
                        drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = (p.size / 2f).coerceAtLeast(1f),
                            center = Offset(currentX, currentY)
                        )
                    } else {
                        drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(currentX - p.size / 2f, currentY - p.size / 4f),
                            size = Size(p.size.coerceAtLeast(1f), (p.size / 2f).coerceAtLeast(1f))
                        )
                    }
                }
            }
        }
    }

}
