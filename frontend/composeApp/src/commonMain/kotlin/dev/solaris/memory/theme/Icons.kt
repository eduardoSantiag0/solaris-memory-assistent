package dev.solaris.memory.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * HANDOFF.md section 6: 24x24 viewBox, 1.75 stroke, square caps, no fill —
 * "linear icons". Implemented with direct Canvas drawing (not ImageVector
 * path parsing) to avoid hand-translating SVG arc/smooth-curve commands;
 * the three icons with curves (buscar, sensivel, revelar) are close visual
 * approximations of the spec'd paths, not byte-identical reproductions.
 */
enum class SolarisIcon {
    CHAT, MEMORIAS, AJUSTES, PERMANENTE, TEMPORARIA, SENSIVEL, REVELAR,
    BUSCAR, EDITAR, EXCLUIR, NOVA, ENVIAR, VOLTAR, FECHAR, ALERTA,
}

@Composable
fun SolarisIconGlyph(icon: SolarisIcon, tint: Color, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val k = this.size.width / 24f
        fun p(x: Float, y: Float) = Offset(x * k, y * k)
        val stroke = Stroke(width = 1.75f * k, cap = StrokeCap.Square)

        fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(tint, p(x1, y1), p(x2, y2), strokeWidth = stroke.width, cap = StrokeCap.Square)

        fun path(block: Path.() -> Unit) {
            val path = Path().apply(block)
            drawPath(path, tint, style = stroke)
        }

        when (icon) {
            SolarisIcon.CHAT -> path {
                moveTo(p(4f, 5f).x, p(4f, 5f).y)
                lineTo(p(20f, 5f).x, p(20f, 5f).y)
                lineTo(p(20f, 16f).x, p(20f, 16f).y)
                lineTo(p(9f, 16f).x, p(9f, 16f).y)
                lineTo(p(4f, 20f).x, p(4f, 20f).y)
                close()
            }

            SolarisIcon.MEMORIAS -> {
                path {
                    moveTo(p(3f, 4f).x, p(3f, 4f).y)
                    lineTo(p(21f, 4f).x, p(21f, 4f).y)
                    lineTo(p(21f, 8f).x, p(21f, 8f).y)
                    lineTo(p(3f, 8f).x, p(3f, 8f).y)
                    close()
                }
                path {
                    moveTo(p(5f, 8f).x, p(5f, 8f).y)
                    lineTo(p(5f, 20f).x, p(5f, 20f).y)
                    lineTo(p(19f, 20f).x, p(19f, 20f).y)
                    lineTo(p(19f, 8f).x, p(19f, 8f).y)
                }
                line(10f, 12f, 14f, 12f)
            }

            SolarisIcon.AJUSTES -> {
                line(4f, 7f, 12f, 7f)
                line(16f, 7f, 20f, 7f)
                path {
                    moveTo(p(12f, 5f).x, p(12f, 5f).y)
                    lineTo(p(16f, 5f).x, p(16f, 5f).y)
                    lineTo(p(16f, 9f).x, p(16f, 9f).y)
                    lineTo(p(12f, 9f).x, p(12f, 9f).y)
                    close()
                }
                line(4f, 17f, 6f, 17f)
                line(10f, 17f, 20f, 17f)
                path {
                    moveTo(p(6f, 15f).x, p(6f, 15f).y)
                    lineTo(p(10f, 15f).x, p(10f, 15f).y)
                    lineTo(p(10f, 19f).x, p(10f, 19f).y)
                    lineTo(p(6f, 19f).x, p(6f, 19f).y)
                    close()
                }
            }

            SolarisIcon.PERMANENTE -> path {
                moveTo(p(6f, 3f).x, p(6f, 3f).y)
                lineTo(p(18f, 3f).x, p(18f, 3f).y)
                lineTo(p(18f, 21f).x, p(18f, 21f).y)
                lineTo(p(12f, 17f).x, p(12f, 17f).y)
                lineTo(p(6f, 21f).x, p(6f, 21f).y)
                close()
            }

            SolarisIcon.TEMPORARIA -> path {
                moveTo(p(7f, 3f).x, p(7f, 3f).y)
                lineTo(p(17f, 3f).x, p(17f, 3f).y)
                lineTo(p(7f, 21f).x, p(7f, 21f).y)
                lineTo(p(17f, 21f).x, p(17f, 21f).y)
                close()
            }

            SolarisIcon.SENSIVEL -> {
                path {
                    moveTo(p(5f, 11f).x, p(5f, 11f).y)
                    lineTo(p(19f, 11f).x, p(19f, 11f).y)
                    lineTo(p(19f, 21f).x, p(19f, 21f).y)
                    lineTo(p(5f, 21f).x, p(5f, 21f).y)
                    close()
                }
                line(8f, 11f, 8f, 7f)
                line(16f, 11f, 16f, 7f)
                drawArc(
                    color = tint,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = p(8f, 3f),
                    size = Size(8f * k, 8f * k),
                    style = stroke,
                )
            }

            SolarisIcon.REVELAR -> {
                path {
                    val l = p(2f, 12f); val r = p(22f, 12f); val top = p(12f, 4f); val bottom = p(12f, 20f)
                    moveTo(l.x, l.y)
                    cubicTo(p(6f, 5f).x, p(6f, 5f).y, p(9f, 4f).x, p(9f, 4f).y, top.x, top.y)
                    cubicTo(p(15f, 4f).x, p(15f, 4f).y, p(18f, 5f).x, p(18f, 5f).y, r.x, r.y)
                    cubicTo(p(18f, 19f).x, p(18f, 19f).y, p(15f, 20f).x, p(15f, 20f).y, bottom.x, bottom.y)
                    cubicTo(p(9f, 20f).x, p(9f, 20f).y, p(6f, 19f).x, p(6f, 19f).y, l.x, l.y)
                    close()
                }
                drawCircle(tint, radius = 3f * k, center = p(12f, 12f), style = stroke)
            }

            SolarisIcon.BUSCAR -> {
                drawCircle(tint, radius = 6f * k, center = p(11f, 11f), style = stroke)
                line(16f, 16f, 21f, 21f)
            }

            SolarisIcon.EDITAR -> path {
                moveTo(p(4f, 20f).x, p(4f, 20f).y)
                lineTo(p(8f, 20f).x, p(8f, 20f).y)
                lineTo(p(19f, 9f).x, p(19f, 9f).y)
                lineTo(p(15f, 5f).x, p(15f, 5f).y)
                lineTo(p(4f, 16f).x, p(4f, 16f).y)
                close()
            }

            SolarisIcon.EXCLUIR -> {
                line(4f, 6f, 20f, 6f)
                path {
                    moveTo(p(9f, 6f).x, p(9f, 6f).y)
                    lineTo(p(9f, 3f).x, p(9f, 3f).y)
                    lineTo(p(15f, 3f).x, p(15f, 3f).y)
                    lineTo(p(15f, 6f).x, p(15f, 6f).y)
                }
                path {
                    moveTo(p(6f, 6f).x, p(6f, 6f).y)
                    lineTo(p(7f, 21f).x, p(7f, 21f).y)
                    lineTo(p(17f, 21f).x, p(17f, 21f).y)
                    lineTo(p(18f, 6f).x, p(18f, 6f).y)
                }
            }

            SolarisIcon.NOVA -> {
                line(12f, 5f, 12f, 19f)
                line(5f, 12f, 19f, 12f)
            }

            SolarisIcon.ENVIAR -> {
                line(5f, 12f, 18f, 12f)
                path {
                    moveTo(p(13f, 7f).x, p(13f, 7f).y)
                    lineTo(p(18f, 12f).x, p(18f, 12f).y)
                    lineTo(p(13f, 17f).x, p(13f, 17f).y)
                }
            }

            SolarisIcon.VOLTAR -> {
                line(19f, 12f, 6f, 12f)
                path {
                    moveTo(p(11f, 7f).x, p(11f, 7f).y)
                    lineTo(p(6f, 12f).x, p(6f, 12f).y)
                    lineTo(p(11f, 17f).x, p(11f, 17f).y)
                }
            }

            SolarisIcon.FECHAR -> {
                line(6f, 6f, 18f, 18f)
                line(18f, 6f, 6f, 18f)
            }

            SolarisIcon.ALERTA -> {
                path {
                    moveTo(p(12f, 3f).x, p(12f, 3f).y)
                    lineTo(p(2f, 21f).x, p(2f, 21f).y)
                    lineTo(p(22f, 21f).x, p(22f, 21f).y)
                    close()
                }
                line(12f, 10f, 12f, 15f)
                line(12f, 17.5f, 12f, 18.5f)
            }
        }
    }
}
