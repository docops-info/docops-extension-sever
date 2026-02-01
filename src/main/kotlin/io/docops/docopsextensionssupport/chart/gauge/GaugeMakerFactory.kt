package io.docops.docopsextensionssupport.chart.gauge

object GaugeMakerFactory {
    
    /**
     * Factory method to create the appropriate GaugeMaker based on the GaugeType.
     * 
     * @param gaugeChart The gauge chart configuration
     * @return A GaugeMaker instance specific to the gauge type
     */
    fun createMaker(gaugeChart: GaugeChart): GaugeMaker {
        return when (gaugeChart.type) {
            GaugeType.SEMI_CIRCLE -> SemiCircleGaugeMaker()
            GaugeType.FULL_CIRCLE -> FullCircleGaugeMaker()
            GaugeType.LINEAR -> LinearGaugeMaker()
            GaugeType.SOLID_FILL -> SolidFillGaugeMaker()
            GaugeType.MULTI_GAUGE -> MultiGaugeMaker()
            GaugeType.DIGITAL -> DigitalGaugeMaker()
            GaugeType.DASHBOARD -> DashboardGaugeMaker()
        }
    }
}
