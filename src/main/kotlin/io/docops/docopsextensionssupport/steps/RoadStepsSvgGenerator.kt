package io.docops.docopsextensionssupport.steps

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.SVGColor
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.support.wrapTextToWidth
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import kotlin.math.*

class RoadStepsSvgGenerator(
    private val useDark: Boolean = false
) {
    private var theme: DocOpsTheme = ThemeFactory.getTheme(useDark)

    fun createSvg(payload: IsometricSteps, scale: Double = 1.0): String {
        val config = payload.config
        val steps = payload.steps
        val n = steps.size

        theme = ThemeFactory.getThemeByName(config.theme, useDark)

        val headerHeight = 182.0
        val stepHeight = 176.0
        val footerHeight = 150.0
        val totalHeight = headerHeight + n * stepHeight + footerHeight
        val canvasWidth = config.canvasWidth.toDouble()
        val roadCenterX = canvasWidth / 2.0

        val stepSlices = mutableListOf<String>()
        val roadPath = StringBuilder()
        
        roadPath.append("M $roadCenterX $headerHeight ")
        
        steps.forEachIndexed { i, step ->
            val isLeft = i % 2 == 0
            val yNode = headerHeight + i * stepHeight + stepHeight / 2.0
            val peakX = if (isLeft) roadCenterX - 150.0 else roadCenterX + 150.0
            val color = step.color ?: theme.chartPalette[i % theme.chartPalette.size].color
            
            val yStart = headerHeight + i * stepHeight
            val yEnd = headerHeight + (i + 1) * stepHeight
            
            roadPath.append("Q $peakX $yStart, $peakX $yNode ")
            roadPath.append("Q $peakX $yEnd, $roadCenterX $yEnd ")
            
            stepSlices.add(drawStep(peakX, yNode, isLeft, color, step, config, i + 1))
        }
        
        val roadEnd = headerHeight + n * stepHeight + 50.0
        roadPath.append("L $roadCenterX $roadEnd")

        return """
            <svg xmlns="http://www.w3.org/2000/svg"
                 width="${(canvasWidth * scale).toInt() / DISPLAY_RATIO_16_9}" height="${(totalHeight * scale).toInt() / DISPLAY_RATIO_16_9}"
                 viewBox="0 0 $canvasWidth $totalHeight" id="road-steps">
                <defs>
                    ${svgDefs(steps)}
                </defs>
                <g transform="scale($scale)">
                    <!-- Background -->
                    <rect width="$canvasWidth" height="$totalHeight" fill="url(#bg-grad)"/>
                    <rect width="$canvasWidth" height="$totalHeight" fill="url(#dot-grid)"/>
                    
                    <!-- Header -->
                    <g>
                        <rect x="${roadCenterX - 514}" y="48" width="6" height="56" rx="3" fill="${theme.accentColor}"/>
                        <text x="${roadCenterX - 494}" y="88" class="road-title" fill="${theme.primaryText}">${escapeXml(config.title)}</text>
                        ${if (config.subtitle != null) """<text x="${roadCenterX - 494}" y="116" class="road-subtitle" fill="${theme.secondaryText}">${escapeXml(config.subtitle)}</text>""" else ""}
                        <line x1="${roadCenterX - 514}" y1="136" x2="${roadCenterX + 514}" y2="136" stroke="${theme.secondaryText}" stroke-opacity="0.6" stroke-width="1"/>
                    </g>
                    
                    <!-- START flag -->
                    <g transform="translate($roadCenterX, 156)">
                        <rect x="-36" y="0" width="72" height="26" rx="13" fill="${theme.primaryText}"/>
                        <text x="0" y="18" text-anchor="middle" font-family="'Syne',sans-serif" font-size="11" font-weight="800" fill="${theme.canvas}" letter-spacing="2">START</text>
                    </g>
                    
                    <!-- Road -->
                    <path id="road-body" d="$roadPath" fill="none" stroke="#3a3530" stroke-width="104" stroke-linejoin="round" stroke-linecap="round"/>
                    <path d="$roadPath" fill="none" stroke="#ffffff" stroke-width="104" stroke-opacity="0.06" stroke-linejoin="round" stroke-linecap="round"/>
                    <path d="$roadPath" fill="none" stroke="#f5e6c8" stroke-width="5" stroke-dasharray="18 14" stroke-opacity="0.75" stroke-linejoin="round" stroke-linecap="round"/>
                    
                    <!-- FINISH flag -->
                    <g transform="translate($roadCenterX, ${roadEnd + 10})">
                        <rect x="-36" y="0" width="72" height="26" rx="13" fill="${theme.primaryText}"/>
                        <text x="0" y="18" text-anchor="middle" font-family="'Syne',sans-serif" font-size="11" font-weight="800" fill="${theme.canvas}" letter-spacing="2">FINISH</text>
                    </g>
                    
                    <!-- Steps -->
                    ${stepSlices.joinToString("\n")}
                </g>
            </svg>
        """.trimIndent()
    }

    private fun drawStep(peakX: Double, y: Double, isLeft: Boolean, color: String, step: IsometricStep, config: IsometricStepsConfig, index: Int): String {
        val cardWidth = 260.0
        val cardHeight = 120.0
        val actualCardX = if (isLeft) peakX - 200.0 - cardWidth else peakX + 200.0
        val cardY = y - cardHeight / 2.0
        
        val connectorX1 = if (isLeft) actualCardX + 160.0 else peakX + 32.0
        val connectorX2 = if (isLeft) peakX - 32.0 else actualCardX
        
        val markerId = "arrow-$index"
        val iconSvg = getIcon(step.icon ?: "diamond", actualCardX, cardY, color)
        
        val textStartX = actualCardX + 58.0
        val textY = cardY + 46.0
        
        val descLines = wrapTextToWidth(step.desc, 180f).take(config.maxDescLines)
        val descSvg = descLines.mapIndexed { i, line ->
            """<text x="$textStartX" y="${textY + 20 + i * 16}" class="box-desc" fill="#ffffff" opacity="0.85">${escapeXml(line)}</text>"""
        }.joinToString("\n")

        return """
        <g class="step-group step-$index">
            <!-- connector line -->
            <line x1="$connectorX1" y1="$y" x2="$connectorX2" y2="$y"
                  stroke="$color" stroke-width="2.5" stroke-dasharray="5 4"
                  marker-end="url(#$markerId)"/>
            <!-- card -->
            <rect x="$actualCardX" y="$cardY" width="$cardWidth" height="$cardHeight" rx="18" fill="url(#grad-$index)" filter="url(#card-shadow)"/>
            <rect x="$actualCardX" y="$cardY" width="6" height="$cardHeight" rx="3" fill="#ffffff" fill-opacity="0.35"/>
            
            $iconSvg
            
            <!-- title -->
            <text x="$textStartX" y="${textY}" class="box-title" fill="#ffffff">${escapeXml(step.title)}</text>
            <!-- desc -->
            $descSvg
            
            <!-- node -->
            <circle cx="$peakX" cy="$y" r="30" fill="#ffffff" stroke="$color" stroke-width="4" filter="url(#node-glow-$index)"/>
            <text x="$peakX" y="${y + 6}" text-anchor="middle" class="road-number" fill="$color">${index.toString().padStart(2, '0')}</text>
        </g>
        """.trimIndent()
    }

    private fun getIcon(name: String, x: Double, y: Double, color: String): String {
        return when (name.lowercase()) {
            "diamond" -> """<polygon points="${x + 22},${y + 42} ${x + 34},${y + 30} ${x + 46},${y + 42} ${x + 34},${y + 54}" fill="#ffffff" fill-opacity="0.9"/>"""
            "code" -> """<text x="${x + 18}" y="${y + 69}" font-family="'Syne',sans-serif" font-size="22" fill="#ffffff" fill-opacity="0.9">&lt;/&gt;</text>"""
            "wallet", "market", "growth" -> """
                <g transform="translate(${x + 18}, ${y + 36})">
                    <rect x="0"  y="18" width="8" height="14" rx="2" fill="#ffffff" fill-opacity="0.9"/>
                    <rect x="12" y="10" width="8" height="22" rx="2" fill="#ffffff" fill-opacity="0.9"/>
                    <rect x="24" y="2"  width="8" height="30" rx="2" fill="#ffffff" fill-opacity="0.9"/>
                </g>
            """.trimIndent()
            "smiley", "satisfaction" -> """
                <g transform="translate(${x + 22}, ${y + 58})">
                    <circle cx="0" cy="0" r="14" fill="none" stroke="#ffffff" stroke-width="2.2" fill-opacity="0"/>
                    <circle cx="-5" cy="-4" r="2" fill="#ffffff"/>
                    <circle cx="5" cy="-4" r="2" fill="#ffffff"/>
                    <path d="M -7 5 Q 0 12 7 5" fill="none" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>
                </g>
            """.trimIndent()
            "user", "security", "padlock" -> """
                <g transform="translate(${x + 18}, ${y + 52})">
                    <rect x="0" y="14" width="22" height="17" rx="3" fill="#ffffff" fill-opacity="0.9"/>
                    <path d="M 4 14 Q 4 2 11 2 Q 18 2 18 14" fill="none" stroke="#ffffff" stroke-width="2.5" stroke-opacity="0.9"/>
                    <circle cx="11" cy="22" r="3" fill="$color"/>
                </g>
            """.trimIndent()
            "rocket", "innovation", "lightbulb" -> """
                <g transform="translate(${x + 22}, ${y + 36})">
                    <path d="M 0 12 Q 0 0 8 -4 Q 16 0 16 12 L 14 18 L 2 18 Z" fill="#ffffff" fill-opacity="0.9"/>
                    <rect x="2" y="18" width="12" height="5" rx="2" fill="#ffffff" fill-opacity="0.7"/>
                    <line x1="8" y1="-8" x2="8" y2="-12" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-opacity="0.8"/>
                </g>
            """.trimIndent()
            else -> """<polygon points="${x + 22},${y + 42} ${x + 34},${y + 30} ${x + 46},${y + 42} ${x + 34},${y + 54}" fill="#ffffff" fill-opacity="0.9"/>"""
        }
    }

    private fun svgDefs(steps: List<IsometricStep>): String {
        val svgCanvas = SVGColor(theme.canvas)
        val darkerCanvas = svgCanvas.darker() ?: theme.canvas
        val gradients = steps.mapIndexed { i, step ->
            val color = step.color ?: theme.chartPalette[i % theme.chartPalette.size].color
            val svgColor = SVGColor(color)
            val darker = svgColor.darker() ?: color
            """
            <linearGradient id="grad-${i + 1}" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%"   stop-color="$color"/>
                <stop offset="100%" stop-color="$darker"/>
            </linearGradient>
            <filter id="node-glow-${i + 1}" x="-60%" y="-60%" width="220%" height="220%">
                <feGaussianBlur stdDeviation="6" result="blur"/>
                <feFlood flood-color="$color" flood-opacity="0.5" result="colour"/>
                <feComposite in="colour" in2="blur" operator="in" result="glow"/>
                <feMerge><feMergeNode in="glow"/><feMergeNode in="SourceGraphic"/></feMerge>
            </filter>
            <marker id="arrow-${i + 1}" markerWidth="9" markerHeight="7" refX="8" refY="3.5" orient="auto">
                <polygon points="0 0, 9 3.5, 0 7" fill="$color"/>
            </marker>
            """.trimIndent()
        }.joinToString("\n")

        return """
            <linearGradient id="bg-grad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%"   stop-color="${theme.canvas}"/>
                <stop offset="100%" stop-color="$darkerCanvas"/>
            </linearGradient>

            <pattern id="dot-grid" x="0" y="0" width="40" height="40" patternUnits="userSpaceOnUse">
                <circle cx="20" cy="20" r="1.2" fill="${theme.secondaryText}" opacity="0.3"/>
            </pattern>

            <filter id="card-shadow" x="-10%" y="-10%" width="120%" height="130%">
                <feGaussianBlur in="SourceAlpha" stdDeviation="6"/>
                <feOffset dx="0" dy="6" result="offsetblur"/>
                <feFlood flood-color="#000000" flood-opacity="0.2"/>
                <feComposite in2="offsetblur" operator="in"/>
                <feMerge>
                    <feMergeNode/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>

            <style>
                @import url('https://fonts.googleapis.com/css2?family=Syne:wght@700;800&amp;family=DM+Sans:wght@400;500&amp;display=swap');

                .road-title {
                    font-family: 'Syne', sans-serif;
                    font-size: 32px;
                    font-weight: 800;
                    letter-spacing: -1.0px;
                }
                .road-subtitle {
                    font-family: 'DM Sans', sans-serif;
                    font-size: 17px;
                    font-weight: 400;
                    letter-spacing: 0.3px;
                }
                .box-title {
                    font-family: 'Syne', sans-serif;
                    font-size: 17px;
                    font-weight: 700;
                    letter-spacing: -0.3px;
                }
                .box-desc {
                    font-family: 'DM Sans', sans-serif;
                    font-size: 13px;
                    font-weight: 400;
                }
                .road-number {
                    font-family: 'Syne', sans-serif;
                    font-size: 19px;
                    font-weight: 800;
                }
                .step-group { cursor: pointer; }

                @keyframes rise {
                    from { opacity: 0; transform: translateY(16px); }
                    to   { opacity: 1; transform: translateY(0); }
                }
                .step-group { opacity: 0; animation: rise 0.55s cubic-bezier(0.16,1,0.3,1) forwards; }
                ${steps.mapIndexed { i, _ -> ".step-${i + 1} { animation-delay: ${0.3 * (i + 1)}s; }" }.joinToString("\n")}
            </style>
            
            $gradients
        """.trimIndent()
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
