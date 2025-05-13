package io.docops.docopsextensionssupport.support

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ColorUtilTest {

    @Test
    fun `test hexToRgb conversion`() {
        // Test a simple color conversion
        val rgb = hexToRgb("#FF0000")
        assertEquals(255, rgb["r"])
        assertEquals(0, rgb["g"])
        assertEquals(0, rgb["b"])
        
        // Test another color
        val rgb2 = hexToRgb("#00FF00")
        assertEquals(0, rgb2["r"])
        assertEquals(255, rgb2["g"])
        assertEquals(0, rgb2["b"])
    }
    
    @Test
    fun `test rgbToHex conversion`() {
        // Test converting RGB to hex
        val hex = rgbToHex(255, 0, 0)
        assertEquals("#ff0000", hex.lowercase())
        
        // Test another color
        val hex2 = rgbToHex(0, 255, 0)
        assertEquals("#00ff00", hex2.lowercase())
    }
    
    @Test
    fun `test determineTextColor for light background`() {
        // Light background should have dark text
        val textColor = determineTextColor("#FFFFFF")
        assertEquals("#000000", textColor)
    }
    
    @Test
    fun `test determineTextColor for dark background`() {
        // Dark background should have light text
        val textColor = determineTextColor("#000000")
        assertEquals("#FCFCFC", textColor)
    }
    
    @Test
    fun `test SVGColor class`() {
        // Create an SVGColor instance
        val color = SVGColor("#FF0000")
        
        // Test foreground color determination
        assertEquals("#FCFCFC", color.foreGroundColor)
        
        // Test gradient generation
        assertNotNull(color.colorMap["color1"])
        assertNotNull(color.colorMap["color2"])
        assertNotNull(color.colorMap["color3"])
        
        // Test helper methods
        assertEquals("#FF0000", color.original())
        assertEquals(color.colorMap["color1"], color.lighter())
        assertEquals(color.colorMap["color3"], color.darker())
    }
    
    @Test
    fun `test generateGradient`() {
        // Generate a gradient from a color
        val gradient = generateGradient("#0000FF")
        
        // Check that all expected keys are present
        assertTrue(gradient.containsKey("original"))
        assertTrue(gradient.containsKey("lighter"))
        assertTrue(gradient.containsKey("darker"))
        
        // Check the original color is preserved
        assertEquals("#0000FF", gradient["original"])
        
        // Check that lighter and darker colors are different from original
        assertNotEquals(gradient["original"], gradient["lighter"])
        assertNotEquals(gradient["original"], gradient["darker"])
    }
}