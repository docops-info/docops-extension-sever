package io.docops.docopsextensionssupport.util

import io.docops.docopsextensionssupport.support.generateRectanglePathData

object BackgroundHelper {


    fun getBackground(useDark: Boolean, id:String): String {
        return if(useDark) {
            """<rect width="100%" height="100%" fill="url(#backgroundGradient_${id})" rx="12" ry="12"/>
            <rect width="100%" height="100%" rx="12" ry="12"
                  fill="rgba(0,122,255,0.1)"
                  stroke="url(#glassBorder_${id})" stroke-width="1.5"
                  filter="url(#glassDropShadow_${id})"
            />
            <rect width="100%" height="100%" rx="12" ry="12"
                  fill="url(#glassOverlay_${id})" opacity="0.7"
            />"""
        } else {
            """<rect width="100%" height="100%" fill="#F2F2F7" rx="12" ry="12"/>"""
        }
    }

    fun getBackGroundPath(useDark: Boolean, id: String, width: Float, height: Float) : String {
        val rectPath = generateRectanglePathData(width, height, 12f,12f,12f,12f)
        return if(useDark) {
            """
           <path d="$rectPath" fill="url(#backgroundGradient_${id})" />    
           <path d="$rectPath" fill="rgba(0,122,255,0.1)"
                  stroke="url(#glassBorder_${id})" stroke-width="1.5"
                  filter="url(#glassDropShadow_${id})" />   
           <path d="$rectPath" fill="url(#glassOverlay_${id})" opacity="0.7" />        
           """.trimIndent()
        } else {
            """<path d="$rectPath" fill="#F2F2F7" />"""
        }
    }
    fun getBackgroundGradient(useDark: Boolean, id:String): String {
        return if(useDark) {
            """
            <linearGradient id="backgroundGradient_$id" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#1a1a2e;stop-opacity:1" />
              <stop offset="100%" style="stop-color:#16213e;stop-opacity:1" />
            </linearGradient>
            <!-- Glass Border (Dark Mode) -->
            <linearGradient id="glassBorder_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                <stop offset="50%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
            </linearGradient>
            <!-- Link Border (Dark Mode) -->
            <linearGradient id="linkBorder_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(96,165,250,0.6);stop-opacity:1" />
                <stop offset="50%" style="stop-color:rgba(96,165,250,0.4);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(96,165,250,0.2);stop-opacity:1" />
            </linearGradient>
            <!-- Apple Glass Effect Gradients (Dark Mode) -->
            <linearGradient id="glassOverlay_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:rgba(255,255,255,0.25);stop-opacity:1" />
                <stop offset="30%" style="stop-color:rgba(255,255,255,0.15);stop-opacity:1" />
                <stop offset="70%" style="stop-color:rgba(255,255,255,0.05);stop-opacity:1" />
                <stop offset="100%" style="stop-color:rgba(255,255,255,0.02);stop-opacity:1" />
            </linearGradient>
                """.trimIndent()
        }
        else """<linearGradient id="backgroundGradient_$id" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop class="stop1" offset="0%" stop-color="#ffffff"/>
                <stop class="stop2" offset="50%" stop-color="#F8FAFC"/>
                <stop class="stop3" offset="100%" stop-color="#e2e8f0"/>
            </linearGradient>
            <!-- Glass Border (Light Mode) -->
                <linearGradient id="glassBorder_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(0,122,255,0.4);stop-opacity:1" />
                    <stop offset="50%" style="stop-color:rgba(0,122,255,0.2);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(0,122,255,0.1);stop-opacity:1" />
                </linearGradient>
                <!-- Link Border (Light Mode) -->
                <linearGradient id="linkBorderLight_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(37,99,235,0.8);stop-opacity:1" />
                    <stop offset="50%" style="stop-color:rgba(37,99,235,0.6);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(37,99,235,0.4);stop-opacity:1" />
                </linearGradient>
                <!-- Apple Glass Effect Gradients (Light Mode) -->
                <linearGradient id="glassOverlay_$id" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color:rgba(255,255,255,0.8);stop-opacity:1" />
                    <stop offset="30%" style="stop-color:rgba(255,255,255,0.6);stop-opacity:1" />
                    <stop offset="70%" style="stop-color:rgba(255,255,255,0.3);stop-opacity:1" />
                    <stop offset="100%" style="stop-color:rgba(255,255,255,0.1);stop-opacity:1" />
                </linearGradient>
                """.trimIndent()
    }
}