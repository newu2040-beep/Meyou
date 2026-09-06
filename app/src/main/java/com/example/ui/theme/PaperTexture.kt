package com.example.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.datastore.PaperTexture

fun Modifier.paperTexture(
    texture: PaperTexture,
    backgroundColor: Color,
    isDark: Boolean = false
): Modifier = this.drawBehind {
    // 1. Draw baseline background color
    drawRect(color = backgroundColor)

    when (texture) {
        PaperTexture.SMOOTH -> {
            // Pristine solid background
        }

        PaperTexture.KRAFT -> {
            // Warm tactile recycled paper with organic fiber speckles
            val speckleColor = if (isDark) Color(0xFFD7CCC8) else Color(0xFF5D4037)
            val step = 28f
            var x = 8f
            while (x < size.width) {
                var y = 12f
                while (y < size.height) {
                    val hash = ((x.toInt() * 73856093) xor (y.toInt() * 19349663)).and(0x7fffffff)
                    val modVal = hash % 100
                    if (modVal < 14) {
                        val radius = when {
                            modVal < 3 -> 1.8f
                            modVal < 8 -> 1.2f
                            else -> 0.7f
                        }
                        val alpha = if (isDark) 0.04f + (modVal % 5) * 0.01f else 0.05f + (modVal % 6) * 0.015f
                        drawCircle(
                            color = speckleColor.copy(alpha = alpha),
                            radius = radius,
                            center = Offset(x + (hash % 16 - 8), y + ((hash / 17) % 16 - 8))
                        )
                    }
                    y += step
                }
                x += step
            }
        }

        PaperTexture.PARCHMENT -> {
            // Vintage antique patina with soft radial edge vignette and deckle warmth
            val edgeColor = if (isDark) Color(0xFF000000).copy(alpha = 0.35f) else Color(0xFF8D6E63).copy(alpha = 0.08f)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, edgeColor),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = maxOf(size.width, size.height) * 0.75f
                )
            )
            // Subtle horizontal parchment fiber lines
            val fiberColor = if (isDark) Color(0xFFBCAAA4).copy(alpha = 0.025f) else Color(0xFF4E342E).copy(alpha = 0.03f)
            var lineY = 20f
            while (lineY < size.height) {
                val h = ((lineY.toInt() * 374761393).and(0x7fffffff)) % 100
                if (h < 25) {
                    drawLine(
                        color = fiberColor,
                        start = Offset(0f, lineY),
                        end = Offset(size.width, lineY),
                        strokeWidth = 1f
                    )
                }
                lineY += 24f
            }
        }

        PaperTexture.LINEN -> {
            // Delicate crisscross woven texture
            val gridColor = if (isDark) Color(0xFFE0E0E0).copy(alpha = 0.035f) else Color(0xFF37474F).copy(alpha = 0.03f)
            val spacing = 16f
            var gx = 0f
            while (gx < size.width) {
                drawLine(
                    color = gridColor,
                    start = Offset(gx, 0f),
                    end = Offset(gx, size.height),
                    strokeWidth = 0.75f
                )
                gx += spacing
            }
            var gy = 0f
            while (gy < size.height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, gy),
                    end = Offset(size.width, gy),
                    strokeWidth = 0.75f
                )
                gy += spacing
            }
        }

        PaperTexture.E_INK -> {
            // High-contrast anti-reflective matte e-paper simulation
            val matteColor = if (isDark) Color(0xFF263238).copy(alpha = 0.12f) else Color(0xFFECEFF1).copy(alpha = 0.35f)
            drawRect(color = matteColor)
            drawRect(
                color = if (isDark) Color(0xFF455A64).copy(alpha = 0.3f) else Color(0xFF90A4AE).copy(alpha = 0.35f),
                size = size,
                style = Stroke(width = 1.5f)
            )
        }
    }
}
