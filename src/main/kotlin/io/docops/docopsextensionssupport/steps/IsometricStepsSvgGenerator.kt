package io.docops.docopsextensionssupport.steps


import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.wrapTextToWidth
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import kotlin.math.*
import kotlin.text.get

class IsometricStepsSvgGenerator(private val useDark: Boolean = false) {
    private var theme: DocOpsTheme = ThemeFactory.getTheme(useDark)

    fun createSvg(payload: IsometricSteps, scale: Double = 1.0): String {
        val config = payload.config
        val steps = payload.steps
        val n = steps.size

        theme = ThemeFactory.getThemeByName(config.theme, useDark)

        val w = 120.0
        val h = 60.0
        val d = 20.0

        val dx = config.dx?.toDouble() ?: floor((config.canvasWidth * 0.55) / max(1, n - 1))
        val dy = config.dy?.toDouble() ?: round(-dx * 0.62)

        val startX = config.startX?.toDouble() ?: 150.0
        val startY = config.startY?.toDouble() ?: (config.canvasHeight - 150.0)

        val sb = StringBuilder()
        
        steps.forEachIndexed { i, step ->
            val xi = startX + i * dx
            val yi = startY + i * dy
            val color = step.color ?: theme.chartPalette[i % theme.chartPalette.size].color
            
            val delay = 200 + i * 150
            sb.append(drawStep(xi, yi, w, h, d, color, step, config, delay))
        }

        return """
            <svg xmlns="http://www.w3.org/2000/svg"
                 width="${(config.canvasWidth * scale).toInt() / DISPLAY_RATIO_16_9}" height="${(config.canvasHeight * scale).toInt()/ DISPLAY_RATIO_16_9}"
                 viewBox="0 0 ${config.canvasWidth} ${config.canvasHeight}" id="isometric-steps">
                <defs>
                    ${svgDefs()}
                </defs>
                <g transform="scale($scale)">
                    <rect width="${config.canvasWidth}" height="${config.canvasHeight}" fill="${theme.canvas}"/>
                    
                    <g transform="translate(40, 60)">
                        <text x="0" y="0" class="title" fill="${theme.primaryText}">${escapeXml(config.title)}</text>
                        ${if (config.subtitle != null) """<text x="0" y="30" class="subtitle" fill="${theme.secondaryText}">${escapeXml(config.subtitle)}</text>""" else ""}
                    </g>
                    
                    $sb
                </g>
            </svg>
        """.trimIndent()
    }

    private fun drawStep(x: Double, y: Double, w: Double, h: Double, d: Double, color: String, step: IsometricStep, config: IsometricStepsConfig, delay: Int): String {
        val svgColor = SVGColor(color)
        val darker = svgColor.darker() ?: color
        val lighter = svgColor.lighter() ?: color
        
        val lx = x + config.labelOffsetX
        val ly = y + config.labelOffsetY
        
        val descLines = wrapTextToWidth(step.desc, 200f).take(config.maxDescLines)
        val descText = descLines.mapIndexed { i, line ->
            """<text x="$lx" y="${ly + 45 + i * 18}" class="desc" fill="${theme.secondaryText}">${escapeXml(line)}</text>"""
        }.joinToString("\n")

        return """
        <g class="animate-fadeIn" style="animation-delay: ${delay}ms">
            <!-- Left Side -->
            <polygon points="${x - w / 2},$y ${x - w / 2},${y + d} $x,${y + h / 2 + d} $x,${y + h / 2}" fill="$darker" />
            <!-- Right Side -->
            <polygon points="${x + w / 2},$y ${x + w / 2},${y + d} $x,${y + h / 2 + d} $x,${y + h / 2}" fill="$color" />
            <!-- Top -->
            <polygon points="$x,${y - h / 2} ${x + w / 2},$y $x,${y + h / 2} ${x - w / 2},$y" fill="$lighter" stroke="$lighter" stroke-width="0.5"/>
            
            <!-- Badge -->
            <circle cx="$x" cy="${y - h / 2 - 15}" r="15" fill="$color" />
            <text x="$x" y="${y - h / 2 - 10}" text-anchor="middle" class="badge-text" fill="#FFFFFF">${step.order}</text>
            
            <!-- Label -->
            <g>
                <text x="$lx" y="${ly + 20}" class="step-title" fill="${theme.primaryText}">${escapeXml(step.title)}</text>
                $descText
                <line x1="$x" y1="$y" x2="$lx" y2="${ly + 10}" stroke="${theme.accentColor}" stroke-width="1" stroke-dasharray="3,3" opacity="0.4"/>
            </g>
        </g>
        """.trimIndent()
    }

    private fun svgDefs(): String = """
        <style>
            .title { font-family: ${theme.fontFamily}; font-size: 32px; font-weight: 800; }
            .subtitle { font-family: ${theme.fontFamily}; font-size: 18px; font-weight: 400; opacity: 0.7; }
            .step-title { font-family: ${theme.fontFamily}; font-size: 20px; font-weight: 700; }
            .desc { font-family: ${theme.fontFamily}; font-size: 14px; font-weight: 400; }
            .badge-text { font-family: ${theme.fontFamily}; font-size: 14px; font-weight: 800; }
            .animate-fadeIn {
                opacity: 0;
                animation: fadeIn 0.6s ease-out forwards;
            }
            @keyframes fadeIn {
                from { opacity: 0; transform: translateY(10px); }
                to { opacity: 1; transform: translateY(0); }
            }
        </style>
    """.trimIndent()

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
