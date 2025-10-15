/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.svgsupport

import io.docops.docopsextensionssupport.web.CsvResponse
import io.docops.docopsextensionssupport.web.toCsvJsonMetaData
import java.awt.Canvas
import java.awt.Font
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

const val DISPLAY_RATIO_16_9 = 1.7777777778

class SvgToPng

fun String.textWidth(fontName: String, size: Int = 12, style: Int = Font.PLAIN): Int {
    val font =  Font(fontName,style,size)
    val c = Canvas()
    val fm = c.getFontMetrics(font)
    return fm.stringWidth(this)
}

fun String.textHeight(fontName: String, size: Int = 12): Int {
    val font =  Font(fontName,Font.PLAIN,size)
    val c = Canvas()
    val fm = c.getFontMetrics(font)
    return fm.height
}
fun String .textAscent(fontName: String, size: Int = 12): Int {
    val font =  Font(fontName,Font.PLAIN,size)
    val c = Canvas()
    val fm = c.getFontMetrics(font)
    return fm.ascent
}

fun itemTextWidth(itemText: String, maxWidth: Float, fontSize: Int = 12, fontName: String = "Helvetica", style: Int = Font.PLAIN): MutableList<String> {
    val split = itemText.split(" ")
    val itemArray = mutableListOf<String>()
    val width = itemText.textWidth(fontName, fontSize, style = style)
    if(width > maxWidth) {
        val sb = StringBuilder()
        split.forEachIndexed { index, s ->
            val itemWidth =  "$sb $s".textWidth(fontName, fontSize)
            if(itemWidth < maxWidth) {
                sb.append("$s ")
                if(index < itemArray.size - 1) {
                    sb.append(" ")
                }
            } else {
                itemArray.add("$sb")
                sb.clear()
                sb.append("$s ")
            }
        }
        if(sb.isNotEmpty()) {
            itemArray.add(sb.toString())
        }
    } else {
        itemArray.add(itemText)
    }
    return itemArray
}



fun getBezierPathFromPoints(points: List<Point>): String {
    val start = points.first()
    val controlPoints = points.drop(1).toMutableList()

    val path = mutableListOf("M ${ptToStr(start)}")

    when {
        // if only one point, draw a straight line
        controlPoints.size == 1 -> {
            path.add("L ${ptToStr(controlPoints[0])}")
        }
        // if there are groups of 3 points, draw cubic bezier curves
        controlPoints.size % 3 == 0 -> {
            for (i in controlPoints.indices step 3) {
                val (c1, c2, p) = controlPoints.slice(i until i + 3)
                path.add("C ${ptToStr(c1)}, ${ptToStr(c2)}, ${ptToStr(p)}")
            }
        }
        // if there's an even number of points, draw quadratic curves
        controlPoints.size % 2 == 0 -> {
            for (i in controlPoints.indices step 2) {
                val (c, p) = controlPoints.slice(i until i + 2)
                path.add("Q ${ptToStr(c)}, ${ptToStr(p)}")
            }
        }
        // else, add missing points and try again
        else -> {
            for (i in controlPoints.size - 3 downTo 2 step 2) {
                val missingPoint = midPoint(controlPoints[i - 1], controlPoints[i])
                controlPoints.add(i, missingPoint)
            }
            return getBezierPathFromPoints(listOf(start) + controlPoints)
        }
    }

    return path.joinToString(" ")
}

fun midPoint(pt1: Point, pt2: Point): Point {
    return Point(
        x = (pt2.x + pt1.x) / 2,
        y = (pt2.y + pt1.y) / 2
    )
}

fun ptToStr(point: Point): String {
    return "${point.x} ${point.y}"
}

data class Point(val x: Double, val y: Double)

fun main() {

    val points = "73,322.2857142857143 146,303.42857142857144 219,284.57142857142856 292,265.7142857142857 365,246.85714285714286 438,228.0 511,209.14285714285714 584,173.14285714285717 657,171.42857142857144"
    val ry = points.split(" ")
    val ary = mutableListOf<Point>()

    ry.forEach { el ->
        val items = el.split(",")
        ary.add(Point(x=items[0].toDouble(), items[1].toDouble()))
    }

    val curve = getBezierPathFromPoints(ary)
    println(curve)
    val graph = listOf(2, 2, 5, 8, 5, 4, 3, 9)
    val pointZ = mutableListOf<Point>()
    for (i in graph.indices) {
        pointZ.add(Point((i * 50 + 20).toDouble(), (graph[i] * 40 * -1 + 400).toDouble()))
    }

    println(makePath(ary))



}


fun catmullRom2bezier(points: List<Point>): List<List<Point>> {
    val result = mutableListOf<List<Point>>()
    for (i in 0 until points.size - 1) {
        val p = mutableListOf<Point>()

        val idx = max(i-1, 0)
        p.add(Point(points[idx].x, points[idx].y))
        p.add(Point(points[i].x, points[i].y))
        p.add(Point(points[i + 1].x, points[i + 1].y))
        val minIdx = min(i + 2, points.size - 1)
        p.add(Point(points[minIdx].x, points[minIdx].y))

        val bp = mutableListOf<Point>()
        bp.add(Point((-p[0].x + 6 * p[1].x + p[2].x) / 6, (-p[0].y + 6 * p[1].y + p[2].y) / 6))
        bp.add(Point((p[1].x + 6 * p[2].x - p[3].x) / 6, (p[1].y + 6 * p[2].y - p[3].y) / 6))
        bp.add(Point(p[2].x, p[2].y))
        result.add(bp)
    }

    return result
}

fun makePath(points: List<Point>): String {
    var result = "M${points[0].x},${points[0].y} "
    val catmull = catmullRom2bezier(points)
    for (i in catmull.indices) {
        result += "C${catmull[i][0].x},${catmull[i][0].y} ${catmull[i][1].x},${catmull[i][1].y} ${catmull[i][2].x},${catmull[i][2].y} "
    }
    return result
}

fun generateRectPathData(
    width: Float,
    height: Float,
    topLetRound: Float,
    topRightRound: Float,
    bottomRightRound: Float,
    bottomLeftRound: Float
): String {
    return """M 0 $topLetRound A $topLetRound $topLetRound 0 0 1 $topLetRound 0 L ${(width - topRightRound)} 0 A $topRightRound $topRightRound 0 0 1 $width $topRightRound L $width ${(height - bottomRightRound)} A $bottomRightRound $bottomRightRound 0 0 1 ${(width - bottomRightRound)} $height L $bottomLeftRound $height A $bottomLeftRound $bottomLeftRound 0 0 1 0 ${(height - bottomLeftRound)} Z"""
}

fun addLinebreaks(input: String, maxLineLength: Int): MutableList<StringBuilder> {
    val list = mutableListOf<StringBuilder>()

    val tok = StringTokenizer(input, " ")
    var output = StringBuilder(input.length)
    var lineLen = 0
    while (tok.hasMoreTokens()) {
        val word = tok.nextToken()
        if (lineLen + word.length > maxLineLength) {
            list.add(output)
            output = StringBuilder(input.length)
            lineLen = 0
        }
        output.append("$word ")
        lineLen += word.length
    }
    if (list.size == 0 || lineLen > 0) {
        list.add(output)
    }
    return list
}

fun String.makeLines(): List<String> {
    return this.split(" ")
}

/**
 * Decompresses a string that may be in one of several formats:
 * 1. URL-safe base64 encoded, gzipped data (original expected format)
 * 2. URL-safe base64 encoded data (without gzip)
 * 3. Standard base64 encoded, gzipped data
 * 4. Standard base64 encoded data (without gzip)
 * 5. Plain text
 *
 * This function attempts to handle all these formats, making it more robust
 * for browser-based clients that may not implement gzip compression.
 *
 * @param zippedBase64Str The input string which may be compressed and/or encoded
 * @return The decompressed/decoded string
 */
fun uncompressString(zippedBase64Str: String): String {
    // First try to decode as URL-safe base64
    try {
        val decoder = Base64.getUrlDecoder()
        val bytes: ByteArray = decoder.decode(zippedBase64Str)

        // Try to decompress as GZIP
        try {
            val zi = GZIPInputStream(ByteArrayInputStream(bytes))
            val reader = InputStreamReader(zi, Charset.defaultCharset())
            val input = BufferedReader(reader)

            val content = StringBuilder()
            try {
                var line = input.readLine()
                while (line != null) {
                    content.append(line + "\n")
                    line = input.readLine()
                }
            } finally {
                reader.close()
            }
            return content.toString()
        } catch (gzipEx: Exception) {
            // If GZIP decompression fails, assume it's just base64 encoded text
            return String(bytes, Charset.defaultCharset())
        }
    } catch (e: Exception) {
        // If URL-safe base64 decoding fails, try standard base64
        try {
            val decoder = Base64.getDecoder()
            val bytes: ByteArray = decoder.decode(zippedBase64Str)

            // Try to decompress as GZIP
            try {
                val zi = GZIPInputStream(ByteArrayInputStream(bytes))
                val reader = InputStreamReader(zi, Charset.defaultCharset())
                val input = BufferedReader(reader)

                val content = StringBuilder()
                try {
                    var line = input.readLine()
                    while (line != null) {
                        content.append(line + "\n")
                        line = input.readLine()
                    }
                } finally {
                    reader.close()
                }
                return content.toString()
            } catch (gzipEx: Exception) {
                // If GZIP decompression fails, assume it's just base64 encoded text
                return String(bytes, Charset.defaultCharset())
            }
        } catch (b64Ex: Exception) {
            // If both base64 decodings fail, return the original string
            // This allows for plain text input to be passed through
            return zippedBase64Str
        }
    }
}
fun compressString(body: String): String {
    val baos = ByteArrayOutputStream()
    val zos = GZIPOutputStream(baos)
    zos.use { z ->
        z.write(body.toByteArray())
    }
    val bytes = baos.toByteArray()
    return Base64.getUrlEncoder().encodeToString(bytes)
}

fun String.escapeXml(): String {
    val sb = StringBuilder()
    for (c in this) {
        when (c) {
            '<' -> sb.append("&lt;")
            '>' -> sb.append("&gt;")
            '"' -> sb.append("&quot;")
            '&' -> sb.append("&amp;")
            '\'' -> sb.append("&apos;")
            else -> sb.append(c)
        }
    }
    return sb.toString()
}

 fun addSvgMetadata(svgContent: String, jsonData: CsvResponse): String {
    // Find the opening <svg> tag and insert metadata after it
    val svgOpenTagEnd = svgContent.indexOf(">", svgContent.indexOf("<svg"))
    if (svgOpenTagEnd == -1) return svgContent

    val metadata = """
        <metadata>
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:cc="http://creativecommons.org/ns#">
                <cc:Work rdf:about="">
                    <dc:creator>DocOps.io</dc:creator>
                    <dc:rights>MIT License</dc:rights>
                    <dc:source>https://docops.io</dc:source>
                    <dc:date>${java.time.LocalDate.now()}</dc:date>
                </cc:Work>
            </rdf:RDF>
        </metadata>
        ${jsonData.toCsvJsonMetaData()}
        <desc>Generated by DocOps.io - Licensed under MIT License</desc>
    """.trimIndent()

    return svgContent.substring(0, svgOpenTagEnd + 1) +
            metadata +
            svgContent.substring(svgOpenTagEnd + 1)
}

/**
 * Joins multiple lines of XML into a single line by removing whitespace.
 *
 * This utility function is used to clean up SVG output by removing unnecessary
 * whitespace and line breaks, which can cause issues in some SVG renderers.
 * It's particularly important when the SVG is embedded in HTML or other documents.
 *
 * The function preserves necessary spaces between XML attributes when joining lines.
 *
 * @param str The multi-line XML string to join
 * @return A single-line XML string with whitespace removed but preserving attribute spacing
 */
fun joinXmlLines(str: String): String {
    val sb = StringBuilder()
    var previousLine = ""

    str.lines().forEach { line ->
        val trimmedLine = line.trim()
        if (trimmedLine.isEmpty()) {
            return@forEach
        }

        // Add a space if the previous line ends with a quote and the current line starts with an attribute
        if (previousLine.endsWith("\"") &&
            (trimmedLine.startsWith("style=") ||
                    trimmedLine.matches(Regex("^[a-zA-Z-]+=.*")))) {
            sb.append(" ")
        }

        // If the previous line doesn't end with a tag closing character, quote, or space,
        // and the current line doesn't start with a tag opening character, quote, or space,
        // then add a space to prevent content from running together
        else if (previousLine.isNotEmpty() &&
            !previousLine.endsWith(">") &&
            !previousLine.endsWith("\"") &&
            !previousLine.endsWith("'") &&
            !previousLine.endsWith(" ") &&
            trimmedLine.isNotEmpty() &&
            !trimmedLine.startsWith("<") &&
            !trimmedLine.startsWith("\"") &&
            !trimmedLine.startsWith("'") &&
            !trimmedLine.startsWith(" ")) {
            sb.append(" ")
        }

        sb.append(trimmedLine)
        previousLine = trimmedLine
    }

    // Fix any remaining attribute issues by ensuring there's a space between quotes and attributes
    return sb.toString().replace("\"style=", "\" style=")
        .replace("\"class=", "\" class=")
        .replace("\"id=", "\" id=")
        .replace("\"width=", "\" width=")
        .replace("\"height=", "\" height=")
        .replace("\"x=", "\" x=")
        .replace("\"y=", "\" y=")
        .replace("\"rx=", "\" rx=")
        .replace("\"ry=", "\" ry=")
        .replace("\"fill=", "\" fill=")
        .replace("\"stroke=", "\" stroke=")
        .replace("\"d=", "\" d=")
        .replace("\"transform=", "\" transform=")
        .replace("\"viewBox=", "\" viewBox=")
        .replace("\"xmlns=", "\" xmlns=")
}

fun formatDecimal(value: Double, decimals: Int): String {
    val multiplier = when (decimals) {
        0 -> 1.0
        1 -> 10.0
        2 -> 100.0
        else ->  decimals.toDouble().pow(10.0)
    }
    val rounded = round(value * multiplier) / multiplier
    return if (decimals == 0) {
        rounded.toInt().toString()
    } else {
        val str = rounded.toString()
        val dotIndex = str.indexOf('.')
        if (dotIndex == -1) {
            str + "." + "0".repeat(decimals)
        } else {
            val currentDecimals = str.length - dotIndex - 1
            when {
                currentDecimals < decimals -> str + "0".repeat(decimals - currentDecimals)
                currentDecimals > decimals -> str.substring(0, dotIndex + decimals + 1)
                else -> str
            }
        }
    }
}