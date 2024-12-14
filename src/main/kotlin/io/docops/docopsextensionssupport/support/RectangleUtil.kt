package io.docops.docopsextensionssupport.support

fun generateRectanglePathData(width: Float, height: Float, topLetRound:Float, topRightRound:Float, bottomRightRound:Float, bottomLeftRound:Float): String {
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

fun main() {
    val path = generateRectanglePathData(255f, 10f, 0f,5f,5f,0f)
    println(path)
}