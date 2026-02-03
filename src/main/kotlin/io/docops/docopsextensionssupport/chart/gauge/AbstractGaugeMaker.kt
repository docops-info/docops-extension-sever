package io.docops.docopsextensionssupport.chart.gauge

import io.docops.docopsextensionssupport.support.DocOpsTheme
import io.docops.docopsextensionssupport.support.ThemeFactory
import io.docops.docopsextensionssupport.svgsupport.DISPLAY_RATIO_16_9
import io.docops.docopsextensionssupport.svgsupport.escapeXml
import io.docops.docopsextensionssupport.svgsupport.formatDecimal
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * Base interface for all gauge makers.
 */
interface GaugeMaker {
    /**
     * Generate SVG for the gauge chart.
     */
    fun makeGauge(gaugeChart: GaugeChart): String
}

/**
 * Abstract base class providing common functionality for all gauge makers.
 */
abstract class AbstractGaugeMaker : GaugeMaker {

    protected lateinit var theme: DocOpsTheme
    protected lateinit var ranges: GaugeRanges
    protected var width: Double = 400.0
    protected var height: Double = 300.0

    override fun makeGauge(gaugeChart: GaugeChart): String {
        theme = ThemeFactory.getTheme(gaugeChart.display)
        ranges = GaugeRanges()

        calculateDimensions(gaugeChart)

        val sb = StringBuilder()
        sb.append(createSvgStart(gaugeChart))
        sb.append(createDefs(gaugeChart))
        sb.append(createBackground(gaugeChart))
        sb.append(createTitle(gaugeChart))
        sb.append(createGaugeContent(gaugeChart))
        sb.append(createSvgEnd())

        return sb.toString()
    }

    protected open fun calculateDimensions(gaugeChart: GaugeChart) {
        width = 400.0
        height = 300.0
    }

    protected open fun createSvgStart(gaugeChart: GaugeChart): String {
        val scaledWidth = (width * gaugeChart.display.scale) / DISPLAY_RATIO_16_9
        val scaledHeight = (height * gaugeChart.display.scale) / DISPLAY_RATIO_16_9

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg" 
                 width="$scaledWidth" 
                 height="$scaledHeight" 
                 viewBox="0 0 $width $height"
                 id="gauge_${gaugeChart.display.id}"
                 aria-label="DocOps: Gauge Chart">
        """.trimIndent()
    }

    protected open fun createDefs(gaugeChart: GaugeChart): String {
        val id = gaugeChart.display.id
        val isDark = gaugeChart.display.useDark

        // Adjust filter strengths based on theme
        val glowIntensity = if (isDark) "4" else "2"
        val shadowIntensity = if (isDark) "0.3" else "0.15"

        //language=css
        return """
        <defs>
            <style>
                ${theme.fontImport}
                
                .gauge-text { 
                    fill: ${theme.primaryText}; 
                    font-family: ${theme.fontFamily}; 
                }
                
                .gauge-value-large {
                    font-family: ${theme.fontFamily};
                    font-weight: 700;
                    font-size: 64px;
                    letter-spacing: -0.03em;
                }
                
                .gauge-value-medium {
                    font-family: ${theme.fontFamily};
                    font-weight: 700;
                    font-size: 42px;
                    letter-spacing: -0.02em;
                }
                
                .gauge-label {
                    font-family: ${theme.fontFamily};
                    font-weight: 400;
                    font-size: 10px;
                    fill: ${theme.secondaryText};
                    text-transform: uppercase;
                    letter-spacing: 0.15em;
                }
                
                .range-label {
                    font-family: ${theme.fontFamily};
                    font-weight: 700;
                    font-size: 11px;
                    letter-spacing: 0.08em;
                }
                
                ${if (gaugeChart.display.animateArc) """
                @keyframes arcDraw {
                    from { stroke-dashoffset: var(--arc-length); opacity: 0; }
                    to { stroke-dashoffset: var(--arc-offset); opacity: 1; }
                }
                
                .animated-arc {
                    stroke-dasharray: var(--arc-length);
                    stroke-dashoffset: var(--arc-length);
                    animation: arcDraw 1.2s cubic-bezier(0.34, 1.56, 0.64, 1) 0.4s forwards;
                }
                
                @keyframes digitFlip {
                    0% { opacity: 0; transform: rotateX(-90deg); }
                    50% { opacity: 0; }
                    100% { opacity: 1; transform: rotateX(0deg); }
                }
                
                .animated-digit {
                    animation: digitFlip 0.6s ease-out 1.2s both;
                }
                @keyframes fillGrow {
                    0% { width: 0; }
                    100% { width: var(--fill-width); }
                }
                
                .animated-fill {
                    transform-origin: left center;
                    animation: fillGrow 1s cubic-bezier(0.34, 1.56, 0.64, 1) 0.5s forwards;
                }
                """ else ""}
                
                /* Dark mode specific enhancements */
                ${if (isDark) """
                .gauge-value-large, .gauge-value-medium {
                    filter: drop-shadow(0px 0px 8px rgba(56, 189, 248, 0.3));
                }
                """ else ""}
            </style>
            
            <!-- Gradients -->
            <linearGradient id="criticalGrad_$id" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#DC2626"/>
                <stop offset="100%" stop-color="#EF4444"/>
            </linearGradient>
            
            <linearGradient id="warningGrad_$id" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#D97706"/>
                <stop offset="100%" stop-color="#F59E0B"/>
            </linearGradient>
            
            <linearGradient id="successGrad_$id" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#059669"/>
                <stop offset="100%" stop-color="#10B981"/>
            </linearGradient>
            
            <!-- Glow filter with dynamic intensity -->
            <filter id="glow_$id">
                <feGaussianBlur stdDeviation="$glowIntensity" result="coloredBlur"/>
                <feMerge>
                    <feMergeNode in="coloredBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                </feMerge>
            </filter>
            
            ${if (isDark) """
            <!-- Dark mode specific gradients -->
            <radialGradient id="bgGlow_$id" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stop-color="#1e293b" stop-opacity="1"/>
                <stop offset="100%" stop-color="#020617" stop-opacity="1"/>
            </radialGradient>
            
            <filter id="dropShadow_$id">
                <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="rgba(0, 0, 0, $shadowIntensity)"/>
            </filter>
            """ else """
            <!-- Light mode specific effects -->
            <filter id="dropShadow_$id">
                <feDropShadow dx="0" dy="2" stdDeviation="3" flood-color="rgba(0, 0, 0, $shadowIntensity)"/>
            </filter>
            """}
            
            <pattern id="dotPattern_$id" x="0" y="0" width="30" height="30" patternUnits="userSpaceOnUse">
                <circle cx="2" cy="2" r="1" fill="${if (isDark) "#334155" else "#cbd5e1"}" fill-opacity="${if (isDark) "0.4" else "0.6"}"/>
            </pattern>
        </defs>
    """.trimIndent()
    }

    protected open fun createBackground(gaugeChart: GaugeChart): String {
        val id = gaugeChart.display.id
        val isDark = gaugeChart.display.useDark

        return if (isDark) {
            // Dark mode: Atmospheric background with depth
            """
            <rect width="$width" height="$height" fill="${theme.canvas}" rx="12"/>
            <rect width="$width" height="$height" fill="url(#bgGlow_$id)" rx="12" opacity="0.5"/>
            <rect width="$width" height="$height" fill="url(#dotPattern_$id)" rx="12" opacity="0.3" pointer-events="none"/>
        """.trimIndent()
        } else {
            // Light mode: Clean, minimal background
            """
            <rect width="$width" height="$height" fill="${theme.canvas}" rx="12"/>
            <rect width="$width" height="$height" fill="url(#dotPattern_$id)" rx="12" opacity="0.15" pointer-events="none"/>
        """.trimIndent()
        }
    }

    protected open fun createTitle(gaugeChart: GaugeChart): String {
        if (gaugeChart.title.isEmpty()) return ""

        return """
            <text x="${width/2}" y="35" 
                  font-family="${theme.fontFamily}" 
                  font-size="22" 
                  font-weight="800" 
                  text-anchor="middle" 
                  fill="${theme.primaryText}"
                  style="text-transform: uppercase; letter-spacing: 1px;">
                ${gaugeChart.title.escapeXml()}
            </text>
        """.trimIndent()
    }

    protected abstract fun createGaugeContent(gaugeChart: GaugeChart): String

    protected fun createSvgEnd(): String = "</svg>"

    protected fun getColorForValue(value: Double, customColor: String?, showRanges: Boolean): String {
        return if (showRanges) {
            ranges.getColorForValue(value)
        } else {
            customColor ?: theme.accentColor
        }
    }

    protected fun getGradientForValue(value: Double, id: String, showRanges: Boolean): String {
        if (!showRanges) return theme.accentColor

        return when {
            value <= ranges.normalEnd -> "url(#successGrad_$id)"
            value <= ranges.cautionEnd -> "url(#warningGrad_$id)"
            else -> "url(#criticalGrad_$id)"
        }
    }

    /**
     * Formats a numeric value for display.
     * Uses formatDecimal from TextUtils for multiplatform compatibility.
     */
    protected fun formatNumber(value: Double): String {
        return if (value == value.toInt().toDouble()) {
            value.toInt().toString()
        } else {
            formatDecimal(value, 1)
        }
    }

    /**
     * Calculates a nice step value for axis ticks.
     * Uses kotlin.math functions for multiplatform compatibility.
     */
    protected fun calculateAxisStep(minValue: Double, maxValue: Double): Double {
        val range = maxValue - minValue
        val roughStep = range / 8
        val magnitude = 10.0.pow(floor(log10(roughStep)))
        val normalizedStep = roughStep / magnitude

        return when {
            normalizedStep <= 1.0 -> magnitude
            normalizedStep <= 2.0 -> 2.0 * magnitude
            normalizedStep <= 5.0 -> 5.0 * magnitude
            else -> 10.0 * magnitude
        }
    }
}