package io.docops.docopsextensionssupport.web.panel

import io.docops.asciidoc.buttons.models.Button
import io.docops.asciidoc.buttons.theme.Theme
import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@Observed(name = "panel.twoToneImageBuilderController")
class TwoToneImageBuilderController {


    @PutMapping("/panelimagetone")
    @ResponseBody
    @Timed(value = "docops.panel.image.tone", histogram = true, percentiles = [0.5, 0.95])
    fun panelImageTone(httpServletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        val params = httpServletRequest.parameterMap
        var fillColor1 = params["fillColor1"]?.get(0)!!
        val fillColor2 = params["fillColor2"]?.get(0)!!
        val line1 = params["line1"]?.get(0)!!
        val line2 = params["line2"]?.get(0)!!
        val line1Size = params["line1Size"]?.get(0)!!
        val line2Size = params["line2Size"]?.get(0)!!
        val contents = makeImage(
            fillColor1 = fillColor1,
            fillColor2 = fillColor2,
            text1 = line1,
            text2 = line2,
            line1Size = line1Size,
            line2Size = line2Size
        )
        servletResponse.contentType = "image/svg+xml";
        servletResponse.characterEncoding = "UTF-8";
        servletResponse.status = 200
        val writer = servletResponse.writer
        writer.print(contents)
        writer.flush()
    }

    fun  makeImage(fillColor1: String, fillColor2: String, text1: String, text2: String, line1Size: String, line2Size:String) : String {
        val btn1 = Button("","","", mutableListOf(),"","",null, fillColor1)
        val btn2 = Button("","","", mutableListOf(),"","",null, fillColor2)
        val theme = Theme()
        val color1 = theme.buildGradientStyle(button = btn1)
        val color2 = theme.buildGradientStyle(button =    btn2)
        val def1= theme.buildGradientDef(btn1)
        val def2= theme.buildGradientDef(btn2)
        //language=svg
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg id="${theme.id}" width="300px" height="191px" viewBox="0 0 300 191" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
                <title>ICON</title>
                <defs>
                $def1
                                    
                $def2
                </defs>
                <style>
                    .oddstyle {
                        font: bold ${line1Size}px Arial, Helvetica, sans-serif;
                        fill: $fillColor2;
                    }
                    .evenstyle {
                        font: bold ${line2Size}px Arial, Helvetica, sans-serif;
                        fill: $fillColor1;
                    }
                    
                    $color1
                    
                    $color2
                    
                </style>
                <g id="Page-1" stroke="none" stroke-width="1" fill="#FFFFFF" fill-rule="evenodd">
                    <path d="${generateRectPathData(300.toFloat(), 191f, 22.0F, 22.0F, 0.0F, 0.0F)}" fill="none"/>
                    <path d="${generateRectPathData(300.toFloat(), (191/2).toFloat(), 22.0F, 22.0F, 0.0F, 0.0F)}" class="${btn1.id}_cls"/>
                    <rect y="95.5" width="100%" height="50%"  class="${btn2.id}_cls" />
                    <text text-anchor="middle" x="150" y="67.75" fill="#000" opacity="0.25" class="oddstyle">$text1</text>
                    <text text-anchor="middle" x="151" y="64.75" fill="blue" class="oddstyle">$text1</text>
                    <text text-anchor="middle" x="150" y="166.25" fill="#000" opacity="0.25" class="evenstyle">$text2</text>
                    <text text-anchor="middle" x="151" y="163.25" class="evenstyle">$text2</text>
                </g>
            </svg>
        """.trimIndent()
    }
    fun generateRectPathData(width: Float, height: Float, topLetRound:Float, topRightRound:Float, bottomRightRound:Float, bottomLeftRound:Float): String {
        return """M 0 $topLetRound 
 A $topLetRound $topLetRound 0 0 1 $topLetRound 0
 L ${(width - topRightRound)} 0
 A $topRightRound $topRightRound 0 0 1 $width $topRightRound
 L $width ${(height - bottomRightRound)}
 A $bottomRightRound $bottomRightRound 0 0 1 ${(width - bottomRightRound)} $height
 L $bottomLeftRound $height
 A $bottomLeftRound $bottomLeftRound 0 0 1 0 ${(height - bottomLeftRound)}
 Z"""
    }
}