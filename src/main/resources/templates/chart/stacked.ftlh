<h1>Stacked Bar Editor</h1>
<button class="btn btn-primary"   onclick="copyTextToClipboard(editor.getValue());">Copy</button>
<button class="btn btn-secondary" onclick="copyTextToClipboard(makeAdoc(editor.getValue()))">Copy as AsciiDoc</button>
<button class="btn btn-secondary" onclick="downloadBlob('snippet.kts')">Download Snippet</button>
<br/>
<br/>
<div class="pure-g">
    <div class="pure-u-8-24">
        <div id="editor">
            stackBar {
                height=600
                width=970
                title = "Group 1 Startup Time"
                subTitle = "Memory & Disk Storage"
                yAxisLabel="(ms)|(mb)"
                xAxisLabel="State Group"
                dimension = mutableListOf("CT", "NY", "NH", "VT", "ME")
                s{
                    name = "Start Up (ms)"
                    stackGroupName = "group-1"
                    data = mutableListOf(24.0,33.0,23.0,33.0,43.0)
                }
                s{
                    name = "Storage (mb)"
                    stackGroupName = "group-1"
                    data = mutableListOf(5.0,17.0,12.0,14.0,15.0)
                }
                s{
                    name= " Memory (mb)"
                    stackGroupName = "group-1"
                    data = mutableListOf(100.0,120.0,90.0,55.0,62.0)
                }
            }
        </div>
    </div>
    <div class="pure-u-16-24">
        <h3>Preview</h3>
        <div id="results"></div>
    </div>
</div>
<script src="editor/js/theme.js"></script>

<script>
    var editor = ace.edit("editor");
    editor.setTheme("ace/theme/clouds");
    editor.session.setMode("ace/mode/kotlin");
    editor.setOptions({
        maxLines: 25
    });
    //editor.session.setMode("ace/mode/html");
    var barColors = [
        ['#ed9adc', '#f6afe2'],
        ['#9d95f4', '#9eabe8'],
        ['#c8a0f9', '#cb9ce5'],
        ['rgba(189,183,107, 0.5)', 'rgba(189,183,107, 1)'],
        ['rgba(176,196,222, 0.5)', 'rgba(176,196,222, 1)'],
        ['rgba(112,128,144, 0.5)', 'rgba(112,128,144, 1)'],
    ];
    var emphasisStyle = {
        itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(112,128,144, 0.3)'
        }
    };
    var itemStyle = function (idx){
        return {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {offset: 0, color: barColors[idx][0]},
                {offset: 1, color: barColors[idx][1]}
            ])
        };
    }
    // or use data: url to handle things like doctype
    function showHTMLInIFrame() {
        getImage("api/bar/stacked", "results");
    }
    editor.on("input", showHTMLInIFrame);
</script>
<br/>
<#include "chartcommon.ftlh">
<@chartCommon chart = "" treechart = "" stacked = "btn btn-active" mychart = ""/>
