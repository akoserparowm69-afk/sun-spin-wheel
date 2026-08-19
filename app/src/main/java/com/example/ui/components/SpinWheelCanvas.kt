package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.data.WheelSlice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val GEOMETRIC_BALANCE_PALETTE = listOf(
    Color(0xFFD0BCFF), // M3 Lavender
    Color(0xFFEADDFF), // M3 Light Purple
    Color(0xFFF9DEDC), // M3 Soft Rose
    Color(0xFFC4EED0), // Soft Mint
    Color(0xFFC2E7FF), // Soft Sky
    Color(0xFFFFD8E4), // Soft Peach
    Color(0xFFE8DEF8), // M3 Purple Gray
    Color(0xFFFFDBCF)  // Soft Coral
)

@Composable
fun SpinWheelCanvas(
    slices: List<WheelSlice>,
    rotationAngle: Float,
    isSpinning: Boolean,
    centerLogoUri: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var centerBitmap by remember(centerLogoUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(centerLogoUri) {
        if (!centerLogoUri.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(centerLogoUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        centerBitmap = BitmapFactory.decodeStream(stream)
                    }
                } catch (e: Throwable) {
                    centerBitmap = null
                }
            }
        } else {
            centerBitmap = null
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "lights")
    val bulbPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpinning) 150 else 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bulbPhase"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (size.width <= 0f || size.height <= 0f || size.minDimension <= 0f) return@Canvas

            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = (size.minDimension / 2f * 0.92f).coerceAtLeast(10f)
            val wheelRadius = (outerRadius - 10.dp.toPx()).coerceAtLeast(8f)
            val hubRadius = (wheelRadius * 0.25f).coerceAtLeast(4f)

            // 1. Draw outer rim shadow & Geometric Balance #6750A4 frame
            drawCircle(
                color = Color(0x336750A4),
                radius = outerRadius + 6.dp.toPx(),
                center = center + Offset(0f, 6.dp.toPx())
            )

            // Outer #6750A4 Rim Frame
            drawCircle(
                color = Color(0xFF6750A4),
                radius = outerRadius,
                center = center
            )

            // Light Lavender Inner track
            drawCircle(
                color = Color(0xFFEADDFF),
                radius = (outerRadius - 3.dp.toPx()).coerceAtLeast(2f),
                center = center
            )

            // 2. Draw Wheel Slices
            if (slices.isNotEmpty()) {
                val totalSlices = slices.size
                val sweepAngle = 360f / totalSlices

                rotate(degrees = rotationAngle, pivot = center) {
                    for (i in 0 until totalSlices) {
                        val slice = slices[i]
                        val startAngle = i * sweepAngle
                        val sliceColor = parseHexColor(slice.colorHex, defaultIndex = i)

                        // Draw slice wedge
                        drawArc(
                            color = sliceColor,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - wheelRadius, center.y - wheelRadius),
                            size = Size(wheelRadius * 2, wheelRadius * 2)
                        )

                        // Draw slice separator border in subtle #6750A4 tone
                        drawArc(
                            color = Color(0x446750A4),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - wheelRadius, center.y - wheelRadius),
                            size = Size(wheelRadius * 2, wheelRadius * 2),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Draw Slice Text
                        val sliceCenterAngle = startAngle + (sweepAngle / 2f)
                        drawSliceText(
                            text = slice.name,
                            centerAngle = sliceCenterAngle,
                            center = center,
                            wheelRadius = wheelRadius,
                            hubRadius = hubRadius,
                            totalSlices = totalSlices,
                            sliceColor = sliceColor,
                            fontDensity = density.density
                        )
                    }

                    // Inner border for wheel slices
                    drawCircle(
                        color = Color(0xFF6750A4),
                        radius = wheelRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            } else {
                // Empty wheel state
                drawCircle(
                    color = Color(0xFFF3EDF7),
                    radius = wheelRadius,
                    center = center
                )
            }

            // 3. Draw Decorative Geometric Balance Accent Dots around the rim
            val bulbCount = 12
            val bulbTrackRadius = (outerRadius - 6.dp.toPx()).coerceAtLeast(4f)
            for (b in 0 until bulbCount) {
                val bulbAngle = (b * (360f / bulbCount)) * (Math.PI / 180f)
                val bulbX = center.x + (bulbTrackRadius * Math.cos(bulbAngle)).toFloat()
                val bulbY = center.y + (bulbTrackRadius * Math.sin(bulbAngle)).toFloat()
                val isLit = (b % 2 == 0) xor (bulbPhase > 0.5f)

                val bulbColor = if (isLit) Color.White else Color(0xFFD0BCFF)
                drawCircle(
                    color = bulbColor,
                    radius = 3.dp.toPx(),
                    center = Offset(bulbX, bulbY)
                )
            }

            // 4. Draw Geometric Balance Center Hub
            drawCircle(
                color = Color(0x33000000),
                radius = hubRadius + 2.dp.toPx(),
                center = center + Offset(0f, 2.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = hubRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF6750A4),
                radius = hubRadius,
                center = center,
                style = Stroke(width = 3.5.dp.toPx())
            )

            // Inner Hub Content (Logo or Geometric Balance "GO" Badge)
            val customBitmap = centerBitmap
            if (customBitmap != null && !customBitmap.isRecycled) {
                val imageSize = (hubRadius * 1.6f).toInt()
                if (imageSize > 0) {
                    try {
                        val scaled = Bitmap.createScaledBitmap(customBitmap, imageSize, imageSize, true)
                        val imageBitmap = scaled.asImageBitmap()
                        drawImage(
                            image = imageBitmap,
                            topLeft = Offset(center.x - imageSize / 2f, center.y - imageSize / 2f)
                        )
                    } catch (e: Throwable) {
                        drawGeometricBalanceHub(center, hubRadius)
                    }
                } else {
                    drawGeometricBalanceHub(center, hubRadius)
                }
            } else {
                drawGeometricBalanceHub(center, hubRadius)
            }

            // 5. Draw Geometric Balance Top Indicator Pointer (#B3261E Crimson with white border)
            drawGeometricBalancePointer(center, wheelRadius, outerRadius)
        }
    }
}

private fun DrawScope.drawSliceText(
    text: String,
    centerAngle: Float,
    center: Offset,
    wheelRadius: Float,
    hubRadius: Float,
    totalSlices: Int,
    sliceColor: Color,
    fontDensity: Float
) {
    if (text.isBlank()) return
    val maxTextWidth = (wheelRadius - hubRadius) * 0.78f
    if (maxTextWidth <= 10f) return

    val nativeCanvas = drawContext.canvas.nativeCanvas

    // Adaptive font size calculation based on number of slices
    val baseFontSize = when {
        totalSlices <= 6 -> 18f
        totalSlices <= 10 -> 15f
        totalSlices <= 16 -> 13f
        totalSlices <= 24 -> 11f
        else -> 9.5f
    } * fontDensity.coerceAtLeast(1f)

    val isBright = (sliceColor.red * 0.299 + sliceColor.green * 0.587 + sliceColor.blue * 0.114) > 0.65
    val textColor = if (isBright) android.graphics.Color.BLACK else android.graphics.Color.WHITE
    val shadowColor = if (isBright) android.graphics.Color.WHITE else android.graphics.Color.BLACK

    val paint = Paint().apply {
        color = textColor
        textSize = baseFontSize.coerceAtLeast(8f)
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.RIGHT
        try {
            setShadowLayer(3f, 1f, 1f, shadowColor)
        } catch (e: Throwable) {
            // ignore
        }
    }

    // Measure and truncate text if it exceeds available radial length
    var displayText = text
    if (paint.measureText(displayText) > maxTextWidth) {
        var len = displayText.length
        while (len > 1 && paint.measureText(displayText.substring(0, len) + "…") > maxTextWidth) {
            len--
        }
        displayText = displayText.substring(0, len) + "…"
    }

    try {
        nativeCanvas.save()
        // Rotate canvas around center to align text along radius
        nativeCanvas.rotate(centerAngle, center.x, center.y)

        // Position text starting near the outer rim pointing inward
        val textX = center.x + wheelRadius - (14f * fontDensity)
        val textY = center.y + (paint.textSize / 3f)

        nativeCanvas.drawText(displayText, textX, textY, paint)
        nativeCanvas.restore()
    } catch (e: Throwable) {
        // Safe canvas fallback
    }
}

private fun DrawScope.drawGeometricBalanceHub(center: Offset, hubRadius: Float) {
    val innerRadius = (hubRadius * 0.65f).coerceAtLeast(2f)
    drawCircle(
        color = Color(0xFF6750A4),
        radius = innerRadius,
        center = center
    )

    if (innerRadius >= 6f) {
        try {
            val nativeCanvas = drawContext.canvas.nativeCanvas
            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = (innerRadius * 0.8f).coerceAtLeast(8f)
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            val textY = center.y - ((paint.descent() + paint.ascent()) / 2f)
            nativeCanvas.drawText("GO", center.x, textY, paint)
        } catch (e: Throwable) {
            // Safe canvas fallback
        }
    }
}

private fun DrawScope.drawGeometricBalancePointer(center: Offset, wheelRadius: Float, outerRadius: Float) {
    val pointerTop = center.y - outerRadius - 10.dp.toPx()
    val pointerBottom = center.y - wheelRadius + 16.dp.toPx()
    val pointerWidth = 24.dp.toPx()

    val pointerPath = Path().apply {
        moveTo(center.x, pointerBottom) // Bottom tip
        lineTo(center.x - pointerWidth / 2f, pointerTop + 6.dp.toPx()) // Top left
        cubicTo(
            center.x - pointerWidth / 2f, pointerTop,
            center.x + pointerWidth / 2f, pointerTop,
            center.x + pointerWidth / 2f, pointerTop + 6.dp.toPx()
        )
        close()
    }

    // Pointer shadow
    val shadowPath = Path().apply {
        moveTo(center.x, pointerBottom + 3.dp.toPx())
        lineTo(center.x - pointerWidth / 2f, pointerTop + 9.dp.toPx())
        cubicTo(
            center.x - pointerWidth / 2f, pointerTop + 3.dp.toPx(),
            center.x + pointerWidth / 2f, pointerTop + 3.dp.toPx(),
            center.x + pointerWidth / 2f, pointerTop + 9.dp.toPx()
        )
        close()
    }
    drawPath(shadowPath, color = Color(0x33000000))

    // #B3261E Crimson Body
    drawPath(
        pointerPath,
        color = Color(0xFFB3261E)
    )

    // White border (border-2 border-white)
    drawPath(
        pointerPath,
        color = Color.White,
        style = Stroke(width = 2.5.dp.toPx())
    )
}

private fun parseHexColor(hex: String?, defaultIndex: Int): Color {
    if (hex.isNullOrBlank()) {
        return GEOMETRIC_BALANCE_PALETTE[defaultIndex % GEOMETRIC_BALANCE_PALETTE.size]
    }
    return try {
        val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
        Color(android.graphics.Color.parseColor(cleanHex))
    } catch (e: Throwable) {
        GEOMETRIC_BALANCE_PALETTE[defaultIndex % GEOMETRIC_BALANCE_PALETTE.size]
    }
}
