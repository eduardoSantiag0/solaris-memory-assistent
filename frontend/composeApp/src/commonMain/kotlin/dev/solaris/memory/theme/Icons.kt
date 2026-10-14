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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * HANDOFF.md section 6: 24x24 viewBox, 1.75 stroke, square caps, no fill —
 * "linear icons". Implemented with direct Canvas drawing (not ImageVector
 * path parsing) to avoid hand-translating SVG arc/smooth-curve commands;
 * the three icons with curves (buscar, sensivel, revelar) are close visual
 * approximations of the spec'd paths, not byte-identical reproductions.
 *
 * Two exceptions per HANDOFF.md 1.0: send and the settings gear come from
 * Remix Icon as filled glyphs, not the hand-drawn linear style — those use
 * `PathParser` to parse the exact SVG path data instead of hand-tracing
 * their curves, since they're too intricate to approximate by eye.
 */
private fun DrawScope.fillSvgPath(pathData: String, tint: Color, k: Float) {
    val path = PathParser().parsePathString(pathData).toPath()
    scale(k, k, pivot = Offset.Zero) { drawPath(path, tint) }
}
enum class SolarisIcon {
    CHAT, MEMORIAS, AJUSTES, PERMANENTE, TEMPORARIA, SENSIVEL, REVELAR,
    BUSCAR, EDITAR, EXCLUIR, NOVA, ENVIAR, VOLTAR, FECHAR, ALERTA,
    DOCUMENTO, SETA_DIREITA,
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

            SolarisIcon.AJUSTES -> fillSvgPath(
                "M3.33946 17.0002C2.90721 16.2515 2.58277 15.4702 2.36133 14.6741C3.3338 14.1779 3.99972 13.1668 3.99972 12.0002C3.99972 10.8345 3.3348 9.824 2.36353 9.32741C2.81025 7.71651 3.65857 6.21627 4.86474 4.99001C5.7807 5.58416 6.98935 5.65534 7.99972 5.072C9.01009 4.48866 9.55277 3.40635 9.4962 2.31604C11.1613 1.8846 12.8847 1.90004 14.5031 2.31862C14.4475 3.40806 14.9901 4.48912 15.9997 5.072C17.0101 5.65532 18.2187 5.58416 19.1346 4.99007C19.7133 5.57986 20.2277 6.25151 20.66 7.00021C21.0922 7.7489 21.4167 8.53025 21.6381 9.32628C20.6656 9.82247 19.9997 10.8336 19.9997 12.0002C19.9997 13.166 20.6646 14.1764 21.6359 14.673C21.1892 16.2839 20.3409 17.7841 19.1347 19.0104C18.2187 18.4163 17.0101 18.3451 15.9997 18.9284C14.9893 19.5117 14.4467 20.5941 14.5032 21.6844C12.8382 22.1158 11.1148 22.1004 9.49633 21.6818C9.55191 20.5923 9.00929 19.5113 7.99972 18.9284C6.98938 18.3451 5.78079 18.4162 4.86484 19.0103C4.28617 18.4205 3.77172 17.7489 3.33946 17.0002ZM8.99972 17.1964C10.0911 17.8265 10.8749 18.8227 11.2503 19.9659C11.7486 20.0133 12.2502 20.014 12.7486 19.9675C13.1238 18.8237 13.9078 17.8268 14.9997 17.1964C16.0916 16.5659 17.347 16.3855 18.5252 16.6324C18.8146 16.224 19.0648 15.7892 19.2729 15.334C18.4706 14.4373 17.9997 13.2604 17.9997 12.0002C17.9997 10.74 18.4706 9.5632 19.2729 8.6665C19.1688 8.4405 19.0538 8.21822 18.9279 8.00021C18.802 7.78219 18.667 7.57148 18.5233 7.36842C17.3457 7.61476 16.0911 7.43414 14.9997 6.80405C13.9083 6.17395 13.1246 5.17768 12.7491 4.03455C12.2509 3.98714 11.7492 3.98646 11.2509 4.03292C10.8756 5.17671 10.0916 6.17364 8.99972 6.80405C7.9078 7.43447 6.65245 7.61494 5.47428 7.36803C5.18485 7.77641 4.93463 8.21117 4.72656 8.66637C5.52881 9.56311 5.99972 10.74 5.99972 12.0002C5.99972 13.2604 5.52883 14.4372 4.72656 15.3339C4.83067 15.5599 4.94564 15.7822 5.07152 16.0002C5.19739 16.2182 5.3324 16.4289 5.47612 16.632C6.65377 16.3857 7.90838 16.5663 8.99972 17.1964ZM11.9997 15.0002C10.3429 15.0002 8.99972 13.6571 8.99972 12.0002C8.99972 10.3434 10.3429 9.00021 11.9997 9.00021C13.6566 9.00021 14.9997 10.3434 14.9997 12.0002C14.9997 13.6571 13.6566 15.0002 11.9997 15.0002ZM11.9997 13.0002C12.552 13.0002 12.9997 12.5525 12.9997 12.0002C12.9997 11.4479 12.552 11.0002 11.9997 11.0002C11.4474 11.0002 10.9997 11.4479 10.9997 12.0002C10.9997 12.5525 11.4474 13.0002 11.9997 13.0002Z",
                tint,
                k,
            )

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

            SolarisIcon.ENVIAR -> fillSvgPath(
                "M13.9999 19.0001L5.00003 19.0002L5 17.0002L11.9999 17.0001L12 6.8283L8.05027 10.778L6.63606 9.36381L13 2.99985L19.364 9.36381L17.9498 10.778L14 6.82825L13.9999 19.0001Z",
                tint,
                k,
            )

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

            SolarisIcon.DOCUMENTO -> {
                path {
                    moveTo(p(5f, 3f).x, p(5f, 3f).y)
                    lineTo(p(15f, 3f).x, p(15f, 3f).y)
                    lineTo(p(19f, 7f).x, p(19f, 7f).y)
                    lineTo(p(19f, 21f).x, p(19f, 21f).y)
                    lineTo(p(5f, 21f).x, p(5f, 21f).y)
                    close()
                }
                line(9f, 11f, 15f, 11f)
                line(9f, 15f, 15f, 15f)
            }

            SolarisIcon.SETA_DIREITA -> path {
                moveTo(p(9f, 6f).x, p(9f, 6f).y)
                lineTo(p(15f, 12f).x, p(15f, 12f).y)
                lineTo(p(9f, 18f).x, p(9f, 18f).y)
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
