package io.docops.docopsextensionssupport.chart

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

class NiceScale(min: Double, max: Double) {
    private var minPoint: Double
    private var maxPoint: Double
    private var maxTicks = 10.0
    private var tickSpacing = 0.0
    private var range = 0.0
    private var niceMin = 0.0
    private var niceMax = 0.0

    /**
     * Instantiates a new instance of the NiceScale class.
     *
     * @param min the minimum data point on the axis
     * @param max the maximum data point on the axis
     */
    init {
        this.minPoint = min
        this.maxPoint = max
        calculate()
    }

    /**
     * Calculate and update values for tick spacing and nice
     * minimum and maximum data points on the axis.
     */
    private fun calculate() {
        this.range = niceNum(maxPoint - minPoint, false)
        this.tickSpacing = niceNum(range / (maxTicks - 1), true)
        this.niceMin =
            floor(minPoint / tickSpacing) * tickSpacing
        this.niceMax =
            ceil(maxPoint / tickSpacing) * tickSpacing
    }

    /**
     * Returns a "nice" number approximately equal to range Rounds
     * the number if round = true Takes the ceiling if round = false.
     *
     * @param range the data range
     * @param round whether to round the result
     * @return a "nice" number to be used for the data range
     */
    private fun niceNum(range: Double, round: Boolean): Double {
        var exponent: Double

        /** exponent of range  */
        var fraction: Double

        /** fractional part of range  */
        var niceFraction: Double

        /** nice, rounded fraction  */
        exponent = floor(log10(range))
        fraction = range / 10.0.pow(exponent)

        if (round) {
            if (fraction < 1.5) niceFraction = 1.0
            else if (fraction < 3) niceFraction = 2.0
            else if (fraction < 7) niceFraction = 5.0
            else niceFraction = 10.0
        } else {
            if (fraction <= 1) niceFraction = 1.0
            else if (fraction <= 2) niceFraction = 2.0
            else if (fraction <= 5) niceFraction = 5.0
            else niceFraction = 10.0
        }

        return niceFraction * 10.0.pow(exponent)
    }

    /**
     * Sets the minimum and maximum data points for the axis.
     *
     * @param minPoint the minimum data point on the axis
     * @param maxPoint the maximum data point on the axis
     */
    fun setMinMaxPoints(minPoint: Double, maxPoint: Double) {
        this.minPoint = minPoint
        this.maxPoint = maxPoint
        calculate()
    }

    /**
     * Sets maximum number of tick marks we're comfortable with
     *
     * @param maxTicks the maximum number of tick marks for the axis
     */
    fun setMaxTicks(maxTicks: Double) {
        this.maxTicks = maxTicks
        calculate()
    }

    fun getTickSpacing(): Double {
        return tickSpacing
    }

    fun getNiceMin(): Double {
        return niceMin
    }

    fun getNiceMax(): Double {
        return niceMax
    }

}

fun main() {
    val niceScale = NiceScale(5.0, 14.0)
    println(niceScale.getTickSpacing())
    println(niceScale.getNiceMin())
    println(niceScale.getNiceMax())
}