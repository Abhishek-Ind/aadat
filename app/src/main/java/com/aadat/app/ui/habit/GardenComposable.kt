package com.aadat.app.ui.habit

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.aadat.app.domain.model.GardenState
import com.aadat.app.domain.model.GrowthLevel
import com.aadat.app.domain.model.HealthState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GardenComposable(
    gardenState: GardenState,
    habitColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "garden")

    val stemHeightFraction by animateFloatAsState(
        targetValue = when (gardenState.growthLevel) {
            GrowthLevel.BARE_SOIL -> 0f
            GrowthLevel.SEEDLING -> 0.2f
            GrowthLevel.SPROUT -> 0.4f
            GrowthLevel.SMALL_PLANT -> 0.55f
            GrowthLevel.FLOWERING_1 -> 0.65f
            GrowthLevel.FLOWERING_2 -> 0.72f
            GrowthLevel.FULL_BLOOM -> 0.8f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "stemHeight"
    )

    val leafScale by animateFloatAsState(
        targetValue = when (gardenState.growthLevel) {
            GrowthLevel.BARE_SOIL, GrowthLevel.SEEDLING -> 0f
            GrowthLevel.SPROUT -> 0.5f
            GrowthLevel.SMALL_PLANT -> 0.8f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "leafScale"
    )

    val flowerScale by animateFloatAsState(
        targetValue = if (gardenState.flowerCount > 0) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "flowerScale"
    )

    val wiltAngle by animateFloatAsState(
        targetValue = when (gardenState.healthState) {
            HealthState.HEALTHY -> 0f
            HealthState.WILTING -> 20f
            HealthState.DEAD -> 45f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "wiltAngle"
    )

    val plantColor = when (gardenState.healthState) {
        HealthState.HEALTHY -> habitColor
        HealthState.WILTING -> Color(0xFF8B7355)
        HealthState.DEAD -> Color(0xFF6B6B6B)
    }

    val leafColor = when (gardenState.healthState) {
        HealthState.HEALTHY -> Color(0xFF4CAF50)
        HealthState.WILTING -> Color(0xFF8B7355)
        HealthState.DEAD -> Color(0xFF9E9E9E)
    }

    val sparkle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "sparkle"
    )

    Canvas(modifier = modifier.size(160.dp, 160.dp)) {
        val w = size.width
        val h = size.height
        val groundY = h * 0.82f
        val potBottom = h * 0.92f

        // Soil / pot
        drawRoundRect(
            color = Color(0xFF795548),
            topLeft = Offset(w * 0.3f, groundY),
            size = Size(w * 0.4f, potBottom - groundY),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )
        drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(w * 0.27f, groundY - 8),
            size = Size(w * 0.46f, 14f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )

        if (stemHeightFraction > 0f) {
            val stemBottomX = w * 0.5f
            val stemBottomY = groundY - 4
            val stemHeight = h * 0.65f * stemHeightFraction
            val stemTopX = stemBottomX + sin(Math.toRadians(wiltAngle.toDouble())).toFloat() * stemHeight * 0.3f
            val stemTopY = stemBottomY - stemHeight

            val stemPath = Path().apply {
                moveTo(stemBottomX, stemBottomY)
                quadraticBezierTo(
                    stemBottomX + sin(Math.toRadians(wiltAngle.toDouble())).toFloat() * stemHeight * 0.15f,
                    stemBottomY - stemHeight * 0.5f,
                    stemTopX,
                    stemTopY
                )
            }
            drawPath(stemPath, color = plantColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f, cap = StrokeCap.Round))

            // Leaves
            if (leafScale > 0f) {
                val leafMidX = stemBottomX + sin(Math.toRadians(wiltAngle.toDouble())).toFloat() * stemHeight * 0.07f
                val leafMidY = stemBottomY - stemHeight * 0.45f
                val leafSize = 28f * leafScale

                // Left leaf
                drawOval(
                    color = leafColor,
                    topLeft = Offset(leafMidX - leafSize - 8, leafMidY - leafSize * 0.5f),
                    size = Size(leafSize, leafSize * 0.6f)
                )
                // Right leaf
                drawOval(
                    color = leafColor,
                    topLeft = Offset(leafMidX + 8, leafMidY - leafSize * 0.35f),
                    size = Size(leafSize, leafSize * 0.6f)
                )
            }

            // Flowers
            if (flowerScale > 0f && gardenState.flowerCount > 0) {
                val flowerRadius = 12f * flowerScale
                val petalCount = 5
                repeat(minOf(gardenState.flowerCount, 3)) { fi ->
                    val offsetX = when (fi) {
                        0 -> 0f
                        1 -> -18f * flowerScale
                        else -> 18f * flowerScale
                    }
                    val flowerX = stemTopX + offsetX
                    val flowerY = stemTopY + fi * 8f * flowerScale

                    repeat(petalCount) { pi ->
                        val angle = Math.toRadians((pi * 360.0 / petalCount) + sparkle * 15)
                        drawCircle(
                            color = habitColor.copy(alpha = 0.8f),
                            radius = flowerRadius * 0.7f,
                            center = Offset(
                                flowerX + cos(angle).toFloat() * flowerRadius,
                                flowerY + sin(angle).toFloat() * flowerRadius
                            )
                        )
                    }
                    drawCircle(
                        color = Color.Yellow.copy(alpha = 0.9f),
                        radius = flowerRadius * 0.5f,
                        center = Offset(flowerX, flowerY)
                    )
                }
            }

            // Sparkles for full bloom
            if (gardenState.growthLevel == GrowthLevel.FULL_BLOOM && gardenState.healthState == HealthState.HEALTHY) {
                repeat(4) { i ->
                    val angle = Math.toRadians((i * 90.0) + sparkle * 360)
                    val dist = 40f + 10f * sin(sparkle * Math.PI * 2).toFloat()
                    val sx = stemTopX + cos(angle).toFloat() * dist
                    val sy = stemTopY + sin(angle).toFloat() * dist
                    drawCircle(color = habitColor.copy(alpha = 0.6f * (1f - sparkle)), radius = 4f, center = Offset(sx, sy))
                }
            }
        } else {
            // Bare soil
            drawRoundRect(
                color = Color(0xFFA1887F).copy(alpha = 0.5f),
                topLeft = Offset(w * 0.33f, groundY - 6),
                size = Size(w * 0.34f, 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
        }
    }
}
