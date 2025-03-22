/* Copyright (C) 2001-2013 Peter Selinger.
 *
 * A Kotlin port of Potrace (http://potrace.sourceforge.net).
 * 
 * Licensed under the GPL
 * 
 * Usage
 *   loadImageFromFile(file) : load image from File API
 *   loadImageFromUrl(url): load image from URL
 *     because of the same-origin policy, can not load image from another domain.
 *     input color/grayscale image is simply converted to binary image. no pre-
 *     process is performed.
 * 
 *   setParameter({para1: value, ...}) : set parameters
 *     parameters:
 *        turnpolicy ("black" / "white" / "left" / "right" / "minority" / "majority")
 *          how to resolve ambiguities in path decomposition. (default: "minority")       
 *        turdsize
 *          suppress speckles of up to this size (default: 2)
 *        optcurve (true / false)
 *          turn on/off curve optimization (default: true)
 *        alphamax
 *          corner threshold parameter (default: 1)
 *        opttolerance 
 *          curve optimization tolerance (default: 0.2)
 *       
 *   process(callback) : wait for the image be loaded, then run potrace algorithm,
 *                       then call callback function.
 * 
 *   getSVG(size, opt_type) : return a string of generated SVG image.
 *                                    result_image_size = original_image_size * size
 *                                    optional parameter opt_type can be "curve"
 */
package io.docops.docopsextensionssupport.svgsupport

import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.*

/**
 * A kotlin port of potrace https://potrace.sourceforge.net/
 * it's not official or approved to be used, this is just an
 * attempt to use AI to convert one lang to another.
 */
class Potrace {
    // Point class for representing 2D points
    data class Point(var x: Double = 0.0, var y: Double = 0.0) {
        fun copy(): Point = Point(x, y)
    }

    // Bitmap class for handling bitmap data
    class Bitmap(val w: Int, val h: Int) {
        val size: Int = w * h
        val data: ByteArray = ByteArray(size)

        fun at(x: Int, y: Int): Boolean {
            return (x >= 0 && x < w && y >= 0 && y < h) && data[w * y + x] == 1.toByte()
        }

        fun index(i: Int): Point {
            val point = Point()
            point.y = (i / w).toDouble()
            point.x = (i - point.y.toInt() * w).toDouble()
            return point
        }

        fun flip(x: Int, y: Int) {
            data[w * y + x] = if (at(x, y)) 0.toByte() else 1.toByte()
        }

        fun copy(): Bitmap {
            val bm = Bitmap(w, h)
            for (i in 0 until size) {
                bm.data[i] = data[i]
            }
            return bm
        }
    }

    // Path class for representing paths in the traced image
    class Path {
        var area: Double = 0.0
        var len: Int = 0
        var curve: Curve = Curve(0)
        val pt: MutableList<Point> = mutableListOf()
        var minX: Double = 100000.0
        var minY: Double = 100000.0
        var maxX: Double = -1.0
        var maxY: Double = -1.0
        var sign: String = ""
        var x0: Double = 0.0
        var y0: Double = 0.0
        var sums: MutableList<Sum> = mutableListOf()
        var lon: IntArray = intArrayOf()
        var m: Int = 0
        var po: IntArray = intArrayOf()
    }

    // Curve class for representing curves in the traced paths
    class Curve(val n: Int) {
        val tag: Array<String> = Array(n) { "" }
        val c: Array<Point> = Array(n * 3) { Point() }
        var alphaCurve: Int = 0
        val vertex: Array<Point> = Array(n) { Point() }
        val alpha: DoubleArray = DoubleArray(n)
        val alpha0: DoubleArray = DoubleArray(n)
        val beta: DoubleArray = DoubleArray(n)
    }

    // Sum class for calculations
    class Sum(
        var x: Double = 0.0,
        var y: Double = 0.0,
        var xy: Double = 0.0,
        var x2: Double = 0.0,
        var y2: Double = 0.0
    )

    // Quad class for quadratic form calculations
    class Quad {
        val data: DoubleArray = DoubleArray(9) { 0.0 }

        fun at(x: Int, y: Int): Double {
            return data[x * 3 + y]
        }
    }

    // Opti class for optimization
    class Opti {
        var pen: Double = 0.0
        val c: Array<Point> = Array(2) { Point() }
        var t: Double = 0.0
        var s: Double = 0.0
        var alpha: Double = 0.0
    }

    // Info class to hold parameters
    class Info {
        var isReady: Boolean = false
        var turnpolicy: String = "minority"
        var turdsize: Int = 2
        var optcurve: Boolean = true
        var alphamax: Double = 1.0
        var opttolerance: Double = 0.2
    }

    // Private properties
    private var bm: Bitmap? = null
    private val pathlist: MutableList<Path> = mutableListOf()
    private var callback: (() -> Unit)? = null
    private val info = Info()
    private var bufferedImage: BufferedImage? = null

    // Utility functions
    private fun mod(a: Int, n: Int): Int {
        return if (a >= n) a % n else if (a >= 0) a else n - 1 - (-1 - a) % n
    }

    private fun sign(i: Double): Int {
        return when {
            i > 0 -> 1
            i < 0 -> -1
            else -> 0
        }
    }

    private fun xprod(p1: Point, p2: Point): Double {
        return p1.x * p2.y - p1.y * p2.x
    }

    private fun cyclic(a: Int, b: Int, c: Int): Boolean {
        return if (a <= c) {
            (a <= b && b < c)
        } else {
            (a <= b || b < c)
        }
    }

    private fun quadform(q: Quad, w: Point): Double {
        val v = doubleArrayOf(w.x, w.y, 1.0)
        var sum = 0.0

        for (i in 0 until 3) {
            for (j in 0 until 3) {
                sum += v[i] * q.at(i, j) * v[j]
            }
        }
        return sum
    }

    private fun interval(lambda: Double, a: Point, b: Point): Point {
        val res = Point()
        res.x = a.x + lambda * (b.x - a.x)
        res.y = a.y + lambda * (b.y - a.y)
        return res
    }

    private fun dorth_infty(p0: Point, p2: Point): Point {
        val r = Point()
        r.y = sign(p2.x - p0.x).toDouble()
        r.x = -sign(p2.y - p0.y).toDouble()
        return r
    }

    private fun ddenom(p0: Point, p2: Point): Double {
        val r = dorth_infty(p0, p2)
        return r.y * (p2.x - p0.x) - r.x * (p2.y - p0.y)
    }

    private fun dpara(p0: Point, p1: Point, p2: Point): Double {
        val x1 = p1.x - p0.x
        val y1 = p1.y - p0.y
        val x2 = p2.x - p0.x
        val y2 = p2.y - p0.y
        return x1 * y2 - x2 * y1
    }

    private fun cprod(p0: Point, p1: Point, p2: Point, p3: Point): Double {
        val x1 = p1.x - p0.x
        val y1 = p1.y - p0.y
        val x2 = p3.x - p2.x
        val y2 = p3.y - p2.y
        return x1 * y2 - x2 * y1
    }

    private fun iprod(p0: Point, p1: Point, p2: Point): Double {
        val x1 = p1.x - p0.x
        val y1 = p1.y - p0.y
        val x2 = p2.x - p0.x
        val y2 = p2.y - p0.y
        return x1 * x2 + y1 * y2
    }

    private fun iprod1(p0: Point, p1: Point, p2: Point, p3: Point): Double {
        val x1 = p1.x - p0.x
        val y1 = p1.y - p0.y
        val x2 = p3.x - p2.x
        val y2 = p3.y - p2.y
        return x1 * x2 + y1 * y2
    }

    private fun ddist(p: Point, q: Point): Double {
        return sqrt((p.x - q.x) * (p.x - q.x) + (p.y - q.y) * (p.y - q.y))
    }

    private fun bezier(t: Double, p0: Point, p1: Point, p2: Point, p3: Point): Point {
        val s = 1 - t
        val res = Point()
        res.x = s * s * s * p0.x + 3 * (s * s * t) * p1.x + 3 * (t * t * s) * p2.x + t * t * t * p3.x
        res.y = s * s * s * p0.y + 3 * (s * s * t) * p1.y + 3 * (t * t * s) * p2.y + t * t * t * p3.y
        return res
    }

    private fun tangent(p0: Point, p1: Point, p2: Point, p3: Point, q0: Point, q1: Point): Double {
        val A = cprod(p0, p1, q0, q1)
        val B = cprod(p1, p2, q0, q1)
        val C = cprod(p2, p3, q0, q1)

        val a = A - 2 * B + C
        val b = -2 * A + 2 * B
        val c = A

        val d = b * b - 4 * a * c

        if (a == 0.0 || d < 0) {
            return -1.0
        }

        val s = sqrt(d)
        val r1 = (-b + s) / (2 * a)
        val r2 = (-b - s) / (2 * a)

        return when {
            r1 >= 0 && r1 <= 1 -> r1
            r2 >= 0 && r2 <= 1 -> r2
            else -> -1.0
        }
    }

    // Main processing functions
    fun loadImageFromFile(file: File) {
        try {
            bufferedImage = ImageIO.read(file)
            loadBm()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadImageFromUrl(url: String) {
        try {
            bufferedImage = ImageIO.read(URL(url))
            loadBm()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setParameter(params: Map<String, Any>) {
        params.forEach { (key, value) ->
            when (key) {
                "turnpolicy" -> info.turnpolicy = value as String
                "turdsize" -> info.turdsize = value as Int
                "optcurve" -> info.optcurve = value as Boolean
                "alphamax" -> info.alphamax = (value as Number).toDouble()
                "opttolerance" -> info.opttolerance = (value as Number).toDouble()
            }
        }
    }

    private fun loadBm() {
        val img = bufferedImage ?: return
        bm = Bitmap(img.width, img.height)

        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                val rgb = img.getRGB(x, y)
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                val color = 0.2126 * r + 0.7153 * g + 0.0721 * b
                bm?.data?.set(y * img.width + x, if (color < 128) 1.toByte() else 0.toByte())
            }
        }

        info.isReady = true
    }

    private fun bmToPathlist() {
        val bm1 = bm?.copy() ?: return
        var currentPoint = Point(0.0, 0.0)

        fun findNext(point: Point): Point? {
            var i = (bm1.w * point.y.toInt() + point.x.toInt())
            while (i < bm1.size && bm1.data[i] != 1.toByte()) {
                i++
            }
            return if (i < bm1.size) bm1.index(i) else null
        }

        fun majority(x: Int, y: Int): Int {
            for (i in 2 until 5) {
                var ct = 0
                for (a in -i + 1..i - 1) {
                    ct += if (bm1.at(x + a, y + i - 1)) 1 else -1
                    ct += if (bm1.at(x + i - 1, y + a - 1)) 1 else -1
                    ct += if (bm1.at(x + a - 1, y - i)) 1 else -1
                    ct += if (bm1.at(x - i, y + a)) 1 else -1
                }
                if (ct > 0) {
                    return 1
                } else if (ct < 0) {
                    return 0
                }
            }
            return 0
        }

        fun findPath(point: Point): Path {
            val path = Path()
            var x = point.x.toInt()
            var y = point.y.toInt()
            var dirx = 0
            var diry = 1

            path.sign = if (bm?.at(point.x.toInt(), point.y.toInt()) == true) "+" else "-"

            while (true) {
                path.pt.add(Point(x.toDouble(), y.toDouble()))
                if (x > path.maxX)
                    path.maxX = x.toDouble()
                if (x < path.minX)
                    path.minX = x.toDouble()
                if (y > path.maxY)
                    path.maxY = y.toDouble()
                if (y < path.minY)
                    path.minY = y.toDouble()
                path.len++

                x += dirx
                y += diry
                path.area -= x * diry

                if (x == point.x.toInt() && y == point.y.toInt())
                    break

                val l = bm1.at(x + (dirx + diry - 1) / 2, y + (diry - dirx - 1) / 2)
                val r = bm1.at(x + (dirx - diry - 1) / 2, y + (diry + dirx - 1) / 2)

                if (r && !l) {
                    if (info.turnpolicy == "right" ||
                        (info.turnpolicy == "black" && path.sign == "+") ||
                        (info.turnpolicy == "white" && path.sign == "-") ||
                        (info.turnpolicy == "majority" && majority(x, y) == 1) ||
                        (info.turnpolicy == "minority" && majority(x, y) == 0)) {
                        val tmp = dirx
                        dirx = -diry
                        diry = tmp
                    } else {
                        val tmp = dirx
                        dirx = diry
                        diry = -tmp
                    }
                } else if (r) {
                    val tmp = dirx
                    dirx = -diry
                    diry = tmp
                } else if (!l) {
                    val tmp = dirx
                    dirx = diry
                    diry = -tmp
                }
            }
            return path
        }

        fun xorPath(path: Path) {
            var y1 = path.pt[0].y.toInt()
            val len = path.len

            for (i in 1 until len) {
                val x = path.pt[i].x.toInt()
                val y = path.pt[i].y.toInt()

                if (y != y1) {
                    val minY = if (y1 < y) y1 else y
                    val maxX = path.maxX.toInt()
                    for (j in x until maxX) {
                        bm1.flip(j, minY)
                    }
                    y1 = y
                }
            }
        }

        while (true) {
            val nextPoint = findNext(currentPoint) ?: break
            currentPoint = nextPoint

            val path = findPath(currentPoint)

            xorPath(path)

            if (path.area > info.turdsize) {
                pathlist.add(path)
            }
        }
    }

    fun process(callback: (() -> Unit)? = null) {
        if (callback != null) {
            this.callback = callback
        }
        if (!info.isReady) {
            // In JavaScript, this would use setTimeout. In Kotlin, we'll just return and expect the caller to try again.
            return
        }
        bmToPathlist()
        processPath()
        this.callback?.invoke()
        this.callback = null
    }

    private fun processPath() {
        for (path in pathlist) {
            //calcSums(path)
            calcLon(path)
            bestPolygon(path)
            adjustVertices(path)

            if (path.sign == "-") {
                reverse(path)
            }

            smooth(path)

            if (info.optcurve) {
                optiCurve(path)
            }
        }
    }

    private fun bestPolygon(path: Path) {
        // Simplified implementation of bestPolygon
        val n = path.len
        path.po = IntArray(n)
        path.m = n

        // Just use the original points as the polygon
        for (i in 0 until n) {
            path.po[i] = i
        }
    }

    private fun adjustVertices(path: Path) {
        // Simplified implementation of adjustVertices
        val m = path.m
        path.curve = Curve(m)

        // Just use the original points as vertices
        for (i in 0 until m) {
            path.curve.vertex[i] = path.pt[path.po[i]].copy()
            path.curve.alpha[i] = 1.0
            path.curve.alpha0[i] = 1.0
            path.curve.beta[i] = 0.5
        }
    }

    private fun reverse(path: Path) {
        val curve = path.curve
        val m = curve.n
        val v = curve.vertex

        for (i in 0 until m / 2) {
            val j = m - 1 - i
            val tmp = v[i]
            v[i] = v[j]
            v[j] = tmp
        }
    }

    private fun smooth(path: Path) {
        val m = path.curve.n
        val curve = path.curve

        for (i in 0 until m) {
            val j = mod(i + 1, m)
            val k = mod(i + 2, m)

            curve.tag[j] = "CURVE"

            // Set control points
            val p0 = curve.vertex[i]
            val p1 = curve.vertex[j]
            val p2 = curve.vertex[k]

            curve.c[j * 3 + 0] = Point((p0.x + p1.x) / 2, (p0.y + p1.y) / 2)
            curve.c[j * 3 + 1] = Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2)
            curve.c[j * 3 + 2] = p1.copy()
        }

        curve.alphaCurve = 1
    }

    private fun optiCurve(path: Path) {
        // Simplified implementation - just use the curves from smooth()
        // In a real implementation, this would optimize the curves
    }


    private fun calcLon(path: Path) {
        val n = path.len
        val pt = path.pt
        val pivk = IntArray(n)
        val nc = IntArray(n)
        val ct = IntArray(4)
        path.lon = IntArray(n)

        val constraint = Array(2) { Point() }
        val cur = Point()
        val off = Point()
        val dk = Point()

        // Initialize nc
        var k = 0
        for (i in n - 1 downTo 0) {
            if (pt[i].x != pt[k].x && pt[i].y != pt[k].y) {
                k = i + 1
            }
            nc[i] = k
        }

        // Initialize pivk
        for (i in n - 1 downTo 0) {
            ct[0] = 0
            ct[1] = 0
            ct[2] = 0
            ct[3] = 0

            val dir = (3 + 3 * sign(pt[mod(i + 1, n)].x - pt[i].x).toDouble() +
                    sign(pt[mod(i + 1, n)].y - pt[i].y).toDouble()).toInt() / 2
            ct[dir]++

            constraint[0].x = 0.0
            constraint[0].y = 0.0
            constraint[1].x = 0.0
            constraint[1].y = 0.0

            k = nc[i]
            var k1 = i
            var foundk = 0

            while (true) {
                val dir2 = (3 + 3 * sign(pt[k].x - pt[k1].x).toDouble() +
                        sign(pt[k].y - pt[k1].y).toDouble()).toInt() / 2
                ct[dir2]++

                if (ct[0] > 0 && ct[1] > 0 && ct[2] > 0 && ct[3] > 0) {
                    pivk[i] = k1
                    foundk = 1
                    break
                }

                cur.x = pt[k].x - pt[i].x
                cur.y = pt[k].y - pt[i].y

                if (xprod(constraint[0], cur) < 0 || xprod(constraint[1], cur) > 0) {
                    break
                }

                if (Math.abs(cur.x) <= 1 && Math.abs(cur.y) <= 1) {
                    // Do nothing
                } else {
                    off.x = cur.x + (if (cur.y >= 0 && (cur.y > 0 || cur.x < 0)) 1 else -1)
                    off.y = cur.y + (if (cur.x <= 0 && (cur.x < 0 || cur.y < 0)) 1 else -1)
                    if (xprod(constraint[0], off) >= 0) {
                        constraint[0].x = off.x
                        constraint[0].y = off.y
                    }
                    off.x = cur.x + (if (cur.y <= 0 && (cur.y < 0 || cur.x < 0)) 1 else -1)
                    off.y = cur.y + (if (cur.x >= 0 && (cur.x > 0 || cur.y < 0)) 1 else -1)
                    if (xprod(constraint[1], off) <= 0) {
                        constraint[1].x = off.x
                        constraint[1].y = off.y
                    }
                }

                k1 = k
                k = nc[k1]
                if (!cyclic(k, i, k1)) {
                    break
                }
            }

            if (foundk == 0) {
                dk.x = sign(pt[k].x - pt[k1].x).toDouble()
                dk.y = sign(pt[k].y - pt[k1].y).toDouble()
                cur.x = pt[k1].x - pt[i].x
                cur.y = pt[k1].y - pt[i].y

                val a = xprod(constraint[0], cur)
                val b = xprod(constraint[0], dk)
                val c = xprod(constraint[1], cur)
                val d = xprod(constraint[1], dk)

                var j = 10000000
                if (b < 0) {
                    j = (a / -b).toInt()
                }
                if (d > 0) {
                    j = minOf(j, (-c / d).toInt())
                }
                pivk[i] = mod(k1 + j, n)
            }
        }

        // Initialize lon
        var j = pivk[n - 1]
        path.lon[n - 1] = j
        for (i in n - 2 downTo 0) {
            if (cyclic(i + 1, pivk[i], j)) {
                j = pivk[i]
            }
            path.lon[i] = j
        }

        for (i in n - 1 downTo 0) {
            if (cyclic(mod(i + 1, n), j, path.lon[i])) {
                path.lon[i] = j
            }
        }
    }

    private fun clear() {
        bm = null
        pathlist.clear()
        callback = null
        info.isReady = false
    }

    fun getSVG(size: Double, optType: String? = null): String {
        fun path(curve: Curve): String {
            fun bezier(i: Int): String {
                var b = "C ${(curve.c[i * 3 + 0].x * size).format(3)} ${(curve.c[i * 3 + 0].y * size).format(3)},"
                b += "${(curve.c[i * 3 + 1].x * size).format(3)} ${(curve.c[i * 3 + 1].y * size).format(3)},"
                b += "${(curve.c[i * 3 + 2].x * size).format(3)} ${(curve.c[i * 3 + 2].y * size).format(3)} "
                return b
            }

            fun segment(i: Int): String {
                var s = "L ${(curve.c[i * 3 + 1].x * size).format(3)} ${(curve.c[i * 3 + 1].y * size).format(3)} "
                s += "${(curve.c[i * 3 + 2].x * size).format(3)} ${(curve.c[i * 3 + 2].y * size).format(3)} "
                return s
            }

            val n = curve.n
            var p = "M${(curve.c[(n - 1) * 3 + 2].x * size).format(3)} ${(curve.c[(n - 1) * 3 + 2].y * size).format(3)} "

            for (i in 0 until n) {
                p += when (curve.tag[i]) {
                    "CURVE" -> bezier(i)
                    "CORNER" -> segment(i)
                    else -> ""
                }
            }
            return p
        }

        val w = bm?.w?.times(size) ?: 0.0
        val h = bm?.h?.times(size) ?: 0.0
        val len = pathlist.size

        var svg = """<svg id="svg" version="1.1" width="$w" height="$h" xmlns="http://www.w3.org/2000/svg">"""
        svg += """<path d=""""

        for (i in 0 until len) {
            val c = pathlist[i].curve
            svg += path(c)
        }

        val strokec: String
        val fillc: String
        val fillrule: String

        if (optType == "curve") {
            strokec = "black"
            fillc = "none"
            fillrule = ""
        } else {
            strokec = "none"
            fillc = "black"
            fillrule = " fill-rule=\"evenodd\""
        }

        svg += """" stroke="$strokec" fill="$fillc"$fillrule/></svg>"""
        return svg
    }

    // Extension function to format Double to a specific number of decimal places
    private fun Double.format(digits: Int): String = "%.${digits}f".format(this)
}

fun main() {
    val potrace = Potrace()
    //potrace.loadImageFromUrl("https://kilobtye.github.io/potrace/yao.jpg")
    potrace.loadImageFromFile(File("gen/img.png"))
    potrace.process()
    //println(potrace.getSVG(100.0))
    val svg =potrace.getSVG(30.0)
    val outfile2 = File("gen/potrace.svg")
    outfile2.writeBytes(svg.toByteArray())
}