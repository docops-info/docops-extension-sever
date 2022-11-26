package io.docops.docopsextensionssupport.web.echart


class ChartShell(val width: String? = "970", val height: String? = "500") {
}

fun ChartShell.build(content: String): String {
    val divId = "div_${System.currentTimeMillis()}"
    //language=html
    return """
        <div id="$divId" style="width: ${width}px; height: ${height}px;"></div>
        
        <script>
        myChart = echarts.init(document.getElementById('$divId'), 'shine');
       
        $content
        </script>
    """.trimIndent()
}