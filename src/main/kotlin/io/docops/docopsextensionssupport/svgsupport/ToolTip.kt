package io.docops.docopsextensionssupport.svgsupport


class ToolTip {

    fun getTopToolTip(params: ToolTipConfig) : String
    {
        val top = -params.offset - params.height
        val sb = StringBuilder()
        sb.append("M 0,0 L ${-params.offset},${-params.offset} H ${-params.width / 2 + params.radius}")
        sb.append(" Q ${-params.width / 2},${-params.offset}  ${-params.width / 2},${-params.offset - params.radius}")
        sb.append(" V ${top + params.radius}")
        sb.append(" Q ${-params.width / 2},${top}  ${-params.width / 2 + params.radius},${top}")
        sb.append(" H ${params.width / 2 - params.radius}")
        sb.append(" Q ${params.width / 2},${top}  ${params.width / 2},${top + params.radius}")
        sb.append(" V ${-params.offset - params.radius}")
        sb.append(" Q ${params.width / 2},${-params.offset}  ${params.width / 2 - params.radius},${-params.offset}")
        sb.append(" H ${params.offset}")
        sb.append(" z")
        return sb.toString()
    }
    fun bottomTooltipPath(width: Int, height: Int, offset: Int, radius: Int): String {
        val left = -width / 2
        val right = width / 2
        val bottom = offset + height
        val top = offset
        val sb = StringBuilder()
        sb.append("M 0,0 L ${-offset},${top} H ${left + radius}")
        sb.append(" Q ${left},${top}  ${left},${top + radius}")
        sb.append(" V ${bottom - radius}")
        sb.append(" Q ${left},${bottom} ${left + radius},${bottom}")
        sb.append(" H ${right - radius}")
        sb.append(" Q ${right},${bottom} ${right},${bottom - radius}")
        sb.append(" V ${top + radius}")
        sb.append(" Q ${right},${top} ${right - radius},${top}")
        sb.append(" H ${offset}")
        sb.append(" L 0,0 z")
        return sb.toString()

    }
    fun leftTooltipPath(width: Int, height: Int, offset: Int, radius: Int): String {
        val left = -offset - width
        val right = -offset
        val top = -height / 2
        val bottom = height / 2
        val sb = StringBuilder()
        sb.append("M 0,0 L ${right},${-offset}")
        sb.append(" V ${top + radius}")
        sb.append(" Q ${right},${top} ${right - radius},${top}")
        sb.append(" H ${left + radius}")
        sb.append(" Q ${left},${top} ${left},${top + radius}")
        sb.append(" V ${bottom - radius}")
        sb.append(" Q ${left},${bottom} ${left + radius},${bottom}")
        sb.append(" H ${right - radius}")
        sb.append(" Q ${right},${bottom} ${right},${bottom - radius}")
        sb.append(" V $offset")
        sb.append(" L 0,0 z")
        return sb.toString()
    }
    fun rightTooltipPath(width: Int, height: Int, offset: Int, radius: Int) : String {
        val left = offset
        val right = offset + width
        val top = -height / 2
        val bottom = height / 2
        val sb = StringBuilder()
        sb.append("M 0,0 L ${left},${-offset}")
        sb.append(" V ${top + radius}")
        sb.append(" Q ${left},${top} ${left + radius},${top}")
        sb.append(" H ${right - radius}")
        sb.append(" Q ${right},${top} ${right},${top + radius}")
        sb.append(" V ${bottom - radius}")
        sb.append(" Q ${right},${bottom} ${right - radius},${bottom}")
        sb.append(" H ${left + radius}")
        sb.append(" Q ${left},${bottom} ${left},${bottom - radius}")
        sb.append(" V $offset")
        sb.append(" L 0,0 z")
        return sb.toString()
    }
}
class ToolTipConfig(val offset: Int = 15, val radius: Int = 5, val width: Int = 70, val height: Int = 50)
