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