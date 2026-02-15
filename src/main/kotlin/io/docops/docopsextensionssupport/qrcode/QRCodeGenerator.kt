package io.docops.docopsextensionssupport.qrcode

import kotlin.compareTo
import kotlin.math.abs
import kotlin.text.iterator

class QRService {
}

/**
 * Pure Kotlin QR Code Generator with SVG output
 * Supports alphanumeric and byte encoding with error correction
 */


// Main QR Code generator class
class QRCodeGenerator(val useXml: Boolean = true, val width: Int, val height: Int,val theme: QRTheme = QRTheme()) {

    fun generate(text: String, errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M): String {
        val qrCode = QRCode(text, errorCorrectionLevel, theme)
        return qrCode.toSVG(useXml = useXml, width=width, height=height)
    }
}

enum class ErrorCorrectionLevel(val indicator: Int, val correctionPercent: Int) {
    L(0b01, 7),  // ~7% correction
    M(0b00, 15), // ~15% correction
    Q(0b11, 25), // ~25% correction
    H(0b10, 30)  // ~30% correction
}

enum class Mode(val indicator: Int, val charCountBits: IntArray) {
    NUMERIC(0b0001, intArrayOf(10, 12, 14)),
    ALPHANUMERIC(0b0010, intArrayOf(9, 11, 13)),
    BYTE(0b0100, intArrayOf(8, 16, 16));

    fun getCharCountBits(version: Int): Int {
        return when {
            version <= 9 -> charCountBits[0]
            version <= 26 -> charCountBits[1]
            else -> charCountBits[2]
        }
    }
}

class QRCode(private val text: String, private val ecLevel: ErrorCorrectionLevel, private val theme: QRTheme = QRTheme()) {
    private var version: Int = 0
    private var mode: Mode = Mode.BYTE
    private lateinit var matrix: Array<IntArray>  // 0=white, 1=black, -1=unset

    init {
        encode()
    }

    private fun encode() {
        // Determine best mode
        mode = determineMode(text)

        // Find minimum version needed
        version = findMinimumVersion(text, mode, ecLevel)

        // Encode data
        val dataBits = encodeData(text, mode, version)

        // Add error correction
        val finalBits = addErrorCorrection(dataBits, version, ecLevel)

        // Create matrix and place patterns
        val size = getMatrixSize(version)
        matrix = Array(size) { IntArray(size) { -1 } }

        // Place function patterns FIRST (before masking)
        placeFinderPatterns()
        placeSeparators()
        placeTimingPatterns()
        placeDarkModule()

        if (version >= 2) {
            placeAlignmentPatterns()
        }

        // Reserve format information areas
        reserveFormatAreas()

        // Place data
        placeData(finalBits)

        // Apply best mask
        val bestMask = findBestMask()
        applyMaskPattern(bestMask)

        // Place format information (AFTER masking)
        placeFormatInfo(ecLevel, bestMask)
    }

    private fun determineMode(text: String): Mode {
        if (text.all { it.isDigit() }) return Mode.NUMERIC
        if (text.all { it in ALPHANUMERIC_CHARS }) return Mode.ALPHANUMERIC
        return Mode.BYTE
    }

    private fun findMinimumVersion(text: String, mode: Mode, ecLevel: ErrorCorrectionLevel): Int {
        for (ver in 1..40) {
            val capacity = getDataCapacity(ver, ecLevel, mode)
            if (capacity >= text.length) {
                return ver
            }
        }
        throw IllegalArgumentException("Text too long for QR code")
    }

    private fun getDataCapacity(version: Int, ecLevel: ErrorCorrectionLevel, mode: Mode): Int {
        val totalCodewords = getTotalCodewords(version)
        val ecCodewords = getErrorCorrectionCodewords(version, ecLevel)
        val dataCodewords = totalCodewords - ecCodewords

        val bitsAvailable = dataCodewords * 8
        val overheadBits = 4 + mode.getCharCountBits(version)
        val dataBits = bitsAvailable - overheadBits

        return when (mode) {
            Mode.NUMERIC -> (dataBits / 10) * 3 + when (dataBits % 10) {
                in 7..9 -> 2
                in 4..6 -> 1
                else -> 0
            }
            Mode.ALPHANUMERIC -> (dataBits / 11) * 2 + if (dataBits % 11 >= 6) 1 else 0
            Mode.BYTE -> dataBits / 8
        }
    }

    private fun encodeData(text: String, mode: Mode, version: Int): List<Boolean> {
        val bits = mutableListOf<Boolean>()

        // Add mode indicator (4 bits)
        bits.addAll(mode.indicator.toBits(4))

        // Add character count
        bits.addAll(text.length.toBits(mode.getCharCountBits(version)))

        // Encode the data
        when (mode) {
            Mode.NUMERIC -> encodeNumeric(text, bits)
            Mode.ALPHANUMERIC -> encodeAlphanumeric(text, bits)
            Mode.BYTE -> encodeByte(text, bits)
        }

        return bits
    }

    private fun encodeNumeric(text: String, bits: MutableList<Boolean>) {
        var i = 0
        while (i < text.length) {
            val chunk = text.substring(i, minOf(i + 3, text.length))
            val value = chunk.toInt()
            val bitCount = when (chunk.length) {
                3 -> 10
                2 -> 7
                else -> 4
            }
            bits.addAll(value.toBits(bitCount))
            i += 3
        }
    }

    private fun encodeAlphanumeric(text: String, bits: MutableList<Boolean>) {
        var i = 0
        while (i < text.length) {
            if (i + 1 < text.length) {
                val value = ALPHANUMERIC_CHARS.indexOf(text[i]) * 45 +
                        ALPHANUMERIC_CHARS.indexOf(text[i + 1])
                bits.addAll(value.toBits(11))
                i += 2
            } else {
                val value = ALPHANUMERIC_CHARS.indexOf(text[i])
                bits.addAll(value.toBits(6))
                i += 1
            }
        }
    }

    private fun encodeByte(text: String, bits: MutableList<Boolean>) {
        for (char in text) {
            bits.addAll(char.code.toBits(8))
        }
    }

    private fun addErrorCorrection(dataBits: List<Boolean>, version: Int, ecLevel: ErrorCorrectionLevel): List<Boolean> {
        val totalCodewords = getTotalCodewords(version)
        val ecCodewords = getErrorCorrectionCodewords(version, ecLevel)
        val dataCodewords = totalCodewords - ecCodewords

        val dataBytes = bitsToBytes(dataBits, dataCodewords)
        val ecBytes = generateErrorCorrectionCodewords(dataBytes, ecCodewords)

        return bytesToBits(dataBytes + ecBytes)
    }

    private fun bitsToBytes(bits: List<Boolean>, targetBytes: Int): List<Int> {
        val bytes = mutableListOf<Int>()
        val workingBits = bits.toMutableList()

        // Add terminator (up to 4 zero bits)
        repeat(minOf(4, targetBytes * 8 - workingBits.size)) {
            workingBits.add(false)
        }

        // Pad to byte boundary
        while (workingBits.size % 8 != 0) {
            workingBits.add(false)
        }

        // Convert to bytes
        for (i in workingBits.indices step 8) {
            var byte = 0
            for (j in 0..7) {
                if (i + j < workingBits.size && workingBits[i + j]) {
                    byte = byte or (1 shl (7 - j))
                }
            }
            bytes.add(byte)
        }

        // Add padding bytes
        val padBytes = listOf(0b11101100, 0b00010001)
        var padIndex = 0
        while (bytes.size < targetBytes) {
            bytes.add(padBytes[padIndex % 2])
            padIndex++
        }

        return bytes.take(targetBytes)
    }

    private fun bytesToBits(bytes: List<Int>): List<Boolean> {
        return bytes.flatMap { it.toBits(8) }
    }

    private fun generateErrorCorrectionCodewords(data: List<Int>, ecCount: Int): List<Int> {
        val generator = getReedSolomonGenerator(ecCount)
        val result = data.toMutableList()

        repeat(ecCount) { result.add(0) }

        for (i in data.indices) {
            val coef = result[i]
            if (coef != 0) {
                for (j in generator.indices) {
                    result[i + j] = result[i + j] xor gfMultiply(generator[j], coef)
                }
            }
        }

        return result.takeLast(ecCount)
    }

    private fun getReedSolomonGenerator(degree: Int): List<Int> {
        var generator = listOf(1)

        for (i in 0 until degree) {
            generator = gfPolyMultiply(generator, listOf(1, gfPow(2, i)))
        }

        return generator
    }

    private fun gfPolyMultiply(a: List<Int>, b: List<Int>): List<Int> {
        val result = MutableList(a.size + b.size - 1) { 0 }

        for (i in a.indices) {
            for (j in b.indices) {
                result[i + j] = result[i + j] xor gfMultiply(a[i], b[j])
            }
        }

        return result
    }

    private fun gfMultiply(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return GF_EXP[(GF_LOG[a] + GF_LOG[b]) % 255]
    }

    private fun gfPow(base: Int, exp: Int): Int {
        var result = 1
        repeat(exp) {
            result = gfMultiply(result, base)
        }
        return result
    }

    private fun placeFinderPatterns() {
        val positions = listOf(
            0 to 0,                     // Top-left (correct)
            matrix.size - 7 to 0,       // Bottom-left (FIXED: was 0 to matrix.size - 7)
            0 to matrix.size - 7        // Top-right (FIXED: was matrix.size - 7 to 0)
        )

        for ((row, col) in positions) {
            // Place 7x7 finder pattern
            for (i in 0..6) {
                for (j in 0..6) {
                    val isBlack = (i == 0 || i == 6 || j == 0 || j == 6) ||
                            (i in 2..4 && j in 2..4)
                    matrix[row + i][col + j] = if (isBlack) 1 else 0
                }
            }
        }
    }

    private fun placeSeparators() {
        val size = matrix.size

        // Top-left separator
        for (i in 0..7) {
            if (i < size) matrix[7][i] = 0
            if (i < size) matrix[i][7] = 0
        }

        // Bottom-left separator
        for (i in 0..7) {
            if (size - 8 + i >= 0 && size - 8 + i < size) matrix[size - 8 + i][7] = 0
            if (i < size) matrix[size - 8][i] = 0
        }

        // Top-right separator
        for (i in 0..7) {
            if (i < size && size - 8 >= 0) matrix[i][size - 8] = 0
            if (size - 8 + i >= 0 && size - 8 + i < size) matrix[7][size - 8 + i] = 0
        }
    }

    private fun placeTimingPatterns() {
        for (i in 8 until matrix.size - 8) {
            matrix[6][i] = if (i % 2 == 0) 1 else 0
            matrix[i][6] = if (i % 2 == 0) 1 else 0
        }
    }

    private fun placeDarkModule() {
        matrix[4 * version + 9][8] = 1
    }

    private fun placeAlignmentPatterns() {
        val positions = getAlignmentPatternPositions(version)

        for (row in positions) {
            for (col in positions) {
                // Skip if overlaps with finder patterns
                if (matrix[row][col] != -1) continue

                // Draw 5x5 alignment pattern
                for (i in -2..2) {
                    for (j in -2..2) {
                        val r = row + i
                        val c = col + j
                        if (r in matrix.indices && c in matrix.indices) {
                            val isBlack = (i == -2 || i == 2 || j == -2 || j == 2) || (i == 0 && j == 0)
                            matrix[r][c] = if (isBlack) 1 else 0
                        }
                    }
                }
            }
        }
    }

    private fun reserveFormatAreas() {
        val size = matrix.size

        // Reserve format info positions
        for (i in 0..8) {
            if (matrix[8][i] == -1) matrix[8][i] = 0
            if (matrix[i][8] == -1) matrix[i][8] = 0
        }

        for (i in 0..7) {
            if (matrix[size - 1 - i][8] == -1) matrix[size - 1 - i][8] = 0
            if (matrix[8][size - 1 - i] == -1) matrix[8][size - 1 - i] = 0
        }
    }

    private fun placeData(bits: List<Boolean>) {
        var bitIndex = 0
        var direction = -1 // -1 for up, 1 for down
        var col = matrix.size - 1

        while (col > 0) {
            if (col == 6) col-- // Skip timing column

            val startRow = if (direction == -1) matrix.size - 1 else 0
            var row = startRow

            while (row >= 0 && row < matrix.size) {
                for (c in 0..1) {
                    val currentCol = col - c

                    if (matrix[row][currentCol] == -1) {
                        matrix[row][currentCol] = if (bitIndex < bits.size && bits[bitIndex]) 1 else 0
                        bitIndex++
                    }
                }

                row += direction
            }

            direction *= -1
            col -= 2
        }
    }

    private fun findBestMask(): Int {
        var bestMask = 0
        var lowestPenalty = Int.MAX_VALUE

        for (mask in 0..7) {
            val testMatrix = Array(matrix.size) { i ->
                IntArray(matrix[i].size) { j -> matrix[i][j] }
            }
            applyMaskToMatrix(testMatrix, mask)
            val penalty = calculatePenalty(testMatrix)

            if (penalty < lowestPenalty) {
                lowestPenalty = penalty
                bestMask = mask
            }
        }

        return bestMask
    }

    private fun applyMaskPattern(maskPattern: Int) {
        applyMaskToMatrix(matrix, maskPattern)
    }

    private fun applyMaskToMatrix(mat: Array<IntArray>, maskPattern: Int) {
        for (row in mat.indices) {
            for (col in mat[row].indices) {
                // Only mask data modules (not function patterns)
                if (isDataModule(row, col)) {
                    val shouldInvert = when (maskPattern) {
                        0 -> (row + col) % 2 == 0
                        1 -> row % 2 == 0
                        2 -> col % 3 == 0
                        3 -> (row + col) % 3 == 0
                        4 -> (row / 2 + col / 3) % 2 == 0
                        5 -> (row * col) % 2 + (row * col) % 3 == 0
                        6 -> ((row * col) % 2 + (row * col) % 3) % 2 == 0
                        7 -> ((row + col) % 2 + (row * col) % 3) % 2 == 0
                        else -> false
                    }
                    if (shouldInvert) {
                        mat[row][col] = if (mat[row][col] == 1) 0 else 1
                    }
                }
            }
        }
    }

    private fun isDataModule(row: Int, col: Int): Boolean {
        val size = matrix.size

        // Finder patterns and separators (all three corners)
        if ((row < 9 && col < 9) ||                          // Top-left
            (row >= size - 9 && col < 9) ||                  // Bottom-left (FIXED)
            (row < 9 && col >= size - 9)) {                  // Top-right (FIXED)
            return false
        }

        // Timing patterns
        if (row == 6 || col == 6) return false

        // Dark module
        if (row == 4 * version + 9 && col == 8) return false

        // Format information
        if ((row == 8 && (col < 9 || col >= size - 8)) ||
            (col == 8 && (row < 9 || row >= size - 8))) {
            return false
        }

        return true
    }

    private fun calculatePenalty(mat: Array<IntArray>): Int {
        var penalty = 0

        // Rule 1: Consecutive same-color modules
        for (row in mat.indices) {
            var runColor = -1
            var runLength = 0
            for (col in mat[row].indices) {
                if (mat[row][col] == runColor) {
                    runLength++
                } else {
                    if (runLength >= 5) {
                        penalty += runLength - 2
                    }
                    runColor = mat[row][col]
                    runLength = 1
                }
            }
            if (runLength >= 5) penalty += runLength - 2
        }

        for (col in mat[0].indices) {
            var runColor = -1
            var runLength = 0
            for (row in mat.indices) {
                if (mat[row][col] == runColor) {
                    runLength++
                } else {
                    if (runLength >= 5) {
                        penalty += runLength - 2
                    }
                    runColor = mat[row][col]
                    runLength = 1
                }
            }
            if (runLength >= 5) penalty += runLength - 2
        }

        // Rule 2: 2x2 blocks
        for (row in 0 until mat.size - 1) {
            for (col in 0 until mat[row].size - 1) {
                val color = mat[row][col]
                if (mat[row][col + 1] == color &&
                    mat[row + 1][col] == color &&
                    mat[row + 1][col + 1] == color) {
                    penalty += 3
                }
            }
        }

        // Rule 3 & 4: Simplified
        var darkCount = 0
        for (row in mat) {
            for (module in row) {
                if (module == 1) darkCount++
            }
        }
        val total = mat.size * mat.size
        val percent = (darkCount * 100) / total
        val deviation = abs(percent - 50) / 5
        penalty += deviation * 10

        return penalty
    }

    private fun placeFormatInfo(ecLevel: ErrorCorrectionLevel, maskPattern: Int) {
        val formatInfo = (ecLevel.indicator shl 3) or maskPattern
        val formatBits = calculateFormatBits(formatInfo)

        // Place around top-left
        for (i in 0..5) {
            matrix[8][i] = if (formatBits[i]) 1 else 0
        }
        matrix[8][7] = if (formatBits[6]) 1 else 0
        matrix[8][8] = if (formatBits[7]) 1 else 0
        matrix[7][8] = if (formatBits[8]) 1 else 0

        for (i in 9..14) {
            matrix[14 - i][8] = if (formatBits[i]) 1 else 0
        }

        // Place around top-right and bottom-left
        for (i in 0..7) {
            matrix[matrix.size - 1 - i][8] = if (formatBits[i]) 1 else 0
        }

        for (i in 8..14) {
            matrix[8][matrix.size - 15 + i] = if (formatBits[i]) 1 else 0
        }
    }

    private fun calculateFormatBits(formatInfo: Int): List<Boolean> {
        var data = formatInfo shl 10

        // BCH error correction
        for (i in 0..4) {
            if ((data shr (14 - i)) and 1 == 1) {
                data = data xor (0b10100110111 shl (4 - i))
            }
        }

        val format = ((formatInfo shl 10) or (data and 0x3FF)) xor 0b101010000010010
        return (0..14).map { (format shr (14 - it)) and 1 == 1 }
    }

    fun toSVG(moduleSize: Int = 10, useXml: Boolean = true, width: Int, height: Int): String {
        val quietZone = 4
        val totalSize = (matrix.size + 2 * quietZone) * moduleSize

        val svg = StringBuilder()
        if (useXml) {
            svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        }
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
        svg.append("width=\"$width\" height=\"$height\" ")
        svg.append("viewBox=\"0 0 $totalSize $totalSize\">\n")

        // Add defs for gradients and patterns if needed
        if (theme.useGradient && theme.gradientColors != null) {
            svg.append("""
              <defs>
                <linearGradient id="qr-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:${theme.gradientColors.first};stop-opacity:1" />
                  <stop offset="100%" style="stop-color:${theme.gradientColors.second};stop-opacity:1" />
                </linearGradient>
              </defs>
            """.trimIndent())
        }

        // Add background pattern if specified
        if (theme.backgroundPattern == "dots") {
            svg.append("""
              <defs>
                <pattern id="dots" x="0" y="0" width="20" height="20" patternUnits="userSpaceOnUse">
                  <circle cx="2" cy="2" r="1" fill="${theme.foreground}" opacity="0.1"/>
                </pattern>
              </defs>
              <rect width="$totalSize" height="$totalSize" fill="${theme.background}"/>
              <rect width="$totalSize" height="$totalSize" fill="url(#dots)"/>
            """.trimIndent())
        } else {
            svg.append("  <rect width=\"$totalSize\" height=\"$totalSize\" fill=\"${theme.background}\"/>\n")
        }

        // Determine fill color
        val fillColor = if (theme.useGradient) "url(#qr-gradient)" else theme.foreground

        // Add filter/shadow group if specified
        if (theme.filter != null) {
            svg.append("  <g filter=\"${theme.filter}\">\n")
        }

        // Render QR modules
        for (row in matrix.indices) {
            for (col in matrix[row].indices) {
                if (matrix[row][col] == 1) {
                    val x = (col + quietZone) * moduleSize
                    val y = (row + quietZone) * moduleSize

                    if (theme.moduleRadius > 0.0) {
                        val radius = moduleSize * theme.moduleRadius
                        svg.append("  <rect x=\"$x\" y=\"$y\" width=\"$moduleSize\" height=\"$moduleSize\" rx=\"$radius\" fill=\"$fillColor\"/>\n")
                    } else {
                        svg.append("  <rect x=\"$x\" y=\"$y\" width=\"$moduleSize\" height=\"$moduleSize\" fill=\"$fillColor\"/>\n")
                    }
                }
            }
        }

        if (theme.filter != null) {
            svg.append("  </g>\n")
        }

        svg.append("</svg>")
        return svg.toString()
    }


    private fun getMatrixSize(version: Int): Int = version * 4 + 17

    private fun getTotalCodewords(version: Int): Int {
        return when (version) {
            1 -> 26
            2 -> 44
            3 -> 70
            4 -> 100
            5 -> 134
            6 -> 172
            7 -> 196
            8 -> 242
            9 -> 292
            10 -> 346
            else -> {
                // Approximate formula for higher versions
                val size = getMatrixSize(version)
                val totalModules = size * size
                val functionModules = 192 + 2 * version * version
                (totalModules - functionModules) / 8
            }
        }
    }

    private fun getErrorCorrectionCodewords(version: Int, ecLevel: ErrorCorrectionLevel): Int {
        val ecTable = mapOf(
            1 to mapOf(ErrorCorrectionLevel.L to 7, ErrorCorrectionLevel.M to 10,
                ErrorCorrectionLevel.Q to 13, ErrorCorrectionLevel.H to 17),
            2 to mapOf(ErrorCorrectionLevel.L to 10, ErrorCorrectionLevel.M to 16,
                ErrorCorrectionLevel.Q to 22, ErrorCorrectionLevel.H to 28),
            3 to mapOf(ErrorCorrectionLevel.L to 15, ErrorCorrectionLevel.M to 26,
                ErrorCorrectionLevel.Q to 36, ErrorCorrectionLevel.H to 44),
            4 to mapOf(ErrorCorrectionLevel.L to 20, ErrorCorrectionLevel.M to 36,
                ErrorCorrectionLevel.Q to 52, ErrorCorrectionLevel.H to 64),
            5 to mapOf(ErrorCorrectionLevel.L to 26, ErrorCorrectionLevel.M to 48,
                ErrorCorrectionLevel.Q to 72, ErrorCorrectionLevel.H to 88),
            6 to mapOf(ErrorCorrectionLevel.L to 36, ErrorCorrectionLevel.M to 64,
                ErrorCorrectionLevel.Q to 96, ErrorCorrectionLevel.H to 112),
            7 to mapOf(ErrorCorrectionLevel.L to 40, ErrorCorrectionLevel.M to 72,
                ErrorCorrectionLevel.Q to 108, ErrorCorrectionLevel.H to 130),
            8 to mapOf(ErrorCorrectionLevel.L to 48, ErrorCorrectionLevel.M to 88,
                ErrorCorrectionLevel.Q to 132, ErrorCorrectionLevel.H to 156),
            9 to mapOf(ErrorCorrectionLevel.L to 60, ErrorCorrectionLevel.M to 110,
                ErrorCorrectionLevel.Q to 160, ErrorCorrectionLevel.H to 192),
            10 to mapOf(ErrorCorrectionLevel.L to 72, ErrorCorrectionLevel.M to 130,
                ErrorCorrectionLevel.Q to 192, ErrorCorrectionLevel.H to 224),
        )

        return ecTable[version]?.get(ecLevel)
            ?: (getTotalCodewords(version) * ecLevel.correctionPercent / 100)
    }

    private fun getAlignmentPatternPositions(version: Int): List<Int> {
        return when (version) {
            1 -> emptyList()
            2 -> listOf(6, 18)
            3 -> listOf(6, 22)
            4 -> listOf(6, 26)
            5 -> listOf(6, 30)
            6 -> listOf(6, 34)
            7 -> listOf(6, 22, 38)
            8 -> listOf(6, 24, 42)
            9 -> listOf(6, 26, 46)
            10 -> listOf(6, 28, 50)
            else -> {
                // Generate positions for higher versions
                val count = version / 7 + 2
                val step = (version * 4 + count * 2 + 1) / (count * 2 - 2) * 2
                val positions = mutableListOf(6)
                for (i in 1 until count) {
                    positions.add(version * 4 + 10 - step * (count - 1 - i))
                }
                positions
            }
        }
    }
}

// Extension function to convert Int to bits
private fun Int.toBits(length: Int): List<Boolean> {
    val bits = mutableListOf<Boolean>()
    for (i in length - 1 downTo 0) {
        bits.add((this shr i) and 1 == 1)
    }
    return bits
}

// Galois Field tables for Reed-Solomon
private val GF_EXP = IntArray(256) { i ->
    var x = 1
    if (i > 0) {
        repeat(i) {
            x = x shl 1
            if (x and 0x100 != 0) {
                x = x xor 0x11d
            }
        }
    }
    x
}

private val GF_LOG = IntArray(256) { i ->
    GF_EXP.indexOf(i)
}

private const val ALPHANUMERIC_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"

