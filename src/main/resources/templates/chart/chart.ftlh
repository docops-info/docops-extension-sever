<h1>Charts Editor</h1>

<button class="btn btn-success"   onclick="copyTextToClipboard(editor.getValue());">Copy</button>
<button class="btn btn-secondary" onclick="copyTextToClipboard(makeAdoc(editor.getValue()))">Copy as AsciiDoc</button>
<button class="btn btn-secondary" onclick="downloadBlob('snippet.kts')">Download Snippet</button>
<br/>
<br/>
<div class="pure-g">
    <div class="pure-u-8-24">
        <div id="editor">
            barChart {
                title = "Hello"
                subTitle = "World"
                xAxisLabel=""
                yAxisLabel=""
                data {
                    name = "Animals"
                    value = 5.0
                    nv("Cats", 4.0)
                    nv("Dogs", 2.0)
                    nv("Cows", 1.0)
                    nv("Sheep", 2.0)
                    nv("Pigs", 1.0)
                }
                data {
                    name = "Fruits"
                    value = 2.0
                    nv("Apples", 4.0)
                    nv("Oranges", 2.0)
                }
            }
        </div>

    </div>
    <div class="pure-u-16-24">
        <h3>Preview</h3>
        <div id="results"></div>
    </div>
</div>

<script>
    var editor = ace.edit("editor");
    editor.setTheme("ace/theme/clouds");
    editor.session.setMode("ace/mode/kotlin");
    editor.setShowPrintMargin(false);
    editor.getSession().setUseWrapMode(true);
    editor.setOptions({
        maxLines: 22
    });
    function showHTMLInIFrame() {
        getImage("api/bar", 'results');
    }
    editor.on("input", showHTMLInIFrame);
</script>
<br/>
<#include "chartcommon.ftlh">
<@chartCommon chart = "btn btn-active" treechart = "" stacked = "" mychart = ""/>

