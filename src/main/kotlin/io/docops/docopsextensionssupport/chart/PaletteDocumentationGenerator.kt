
package io.docops.docopsextensionssupport.chart

import java.util.Base64

object PaletteDocGenerator {
    
    /**
     * Generate AsciiDoc documentation for all available palettes
     */
    fun generatePaletteDoc(): String {
        val sb = StringBuilder()
        
        sb.appendLine("= Color Palette Reference")
        sb.appendLine()
        sb.appendLine("== Available Palettes")
        sb.appendLine()
        
        ColorPaletteFactory.getAvailablePalettes().forEach { paletteType ->
            sb.appendLine(generatePaletteSection(paletteType))
        }
        
        return sb.toString()
    }
    
    /**
     * Generate a section for a specific palette with color swatches
     */
    private fun generatePaletteSection(paletteType: ColorPaletteFactory.PaletteType): String {
        val palette = ColorPaletteFactory.getPalette(paletteType)
        val sb = StringBuilder()
        
        sb.appendLine("=== ${paletteType.name.replace("_", " ").lowercase().capitalize()}")
        sb.appendLine("*Usage:* `paletteType=\"${paletteType.name}\"`")
        sb.appendLine()
        
        // Generate color swatches table
        sb.appendLine("[cols=\"${palette.size}*^\", frame=none, grid=none]")
        sb.appendLine("|===")
        
        // Color swatch row
        palette.forEach { color ->
            sb.append("| ${generateColorSwatch(color)} ")
        }
        sb.appendLine()
        
        // Hex value row
        palette.forEach { color ->
            sb.append("| `$color` ")
        }
        sb.appendLine()
        
        sb.appendLine("|===")
        sb.appendLine()
        
        return sb.toString()
    }
    
    /**
     * Generate an inline SVG color swatch
     */
    private fun generateColorSwatch(hexColor: String): String {
        val svg = """
            <svg width="60" height="60" xmlns="http://www.w3.org/2000/svg">
                <rect width="60" height="60" fill="$hexColor" rx="4"/>
            </svg>
        """.trimIndent()
        
        val encoded = Base64.getEncoder().encodeToString(svg.toByteArray())
        return "image:data:image/svg+xml;base64,$encoded[]"
    }
    
    /**
     * Generate example usage for bar charts
     */
    fun generateUsageExamples(): String {
        return """
            == Usage Examples
            
            === Bar Group Chart
            
            [source,asciidoc]
            ----
            [docops,barchart, useDark="false"]
            ....
            title: Sales by Region
            xLabel: Regions
            yLabel: Revenue (${'$'}M)
            paletteType: OCEAN_BREEZE
            ....
            ----
            
            === Condensed Bar Chart
            
            [source,asciidoc]
            ----
            [docops,barchart]
            ....
            condensed=true
            paletteType=TROPICAL_PARADISE
            ---
            {your data}
            ....
            ----
        """.trimIndent()
    }
}

