package io.docops.extension.server.echart


class ChartShell {
}

fun ChartShell.build(content: String): String {
    val divId = "div_${System.currentTimeMillis()}"
    //language=html
    return """
        <div id="$divId" style="width: 800px; height: 800px;"></div>
        
        <script>
        myChart = echarts.init(document.getElementById('$divId'), 'shine');
       
        $content
        </script>
    """.trimIndent()
}