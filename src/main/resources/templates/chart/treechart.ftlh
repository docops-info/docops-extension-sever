<h1>Tree Chart Editor</h1>
<button class="btn btn-primary"   onclick="copyTextToClipboard(editor.getValue());">Copy</button>
<button class="btn btn-secondary" onclick="copyTextToClipboard(makeAdoc(editor.getValue()))">Copy as AsciiDoc</button>
<button class="btn btn-secondary" onclick="downloadBlob('snippet.kts')">Download Snippet</button>
<br/>
<br/>
<div class="pure-g">
    <div class="pure-u-8-24">
        <div id="editor" class="editor">
            treeChart {
                name = "Product Range"
                child {
                    name = "USB Converter"
                    child {
                        name = "USB 3.0 Converter"
                    }
                    child {
                        name = "USB 2.0 Converter"
                        child {
                            name = "USB 2 Serial"
                        }
                        child {
                            name = "USB 2 RS485"
                        }
                        child {
                            name = "USB 2 Dual Serial"
                        }
                        child {
                            name = "USB 2 MIDI"
                        }
                    }
                }
                child {
                    name = "HDMI"
                }
            }
        </div>
    </div>
    <div class="pure-u-16-24">
        <h3>Preview</h3>
        <div id="results"></div>
    </div>
</div>
<br/>
<script>
    var editor = ace.edit("editor");
    editor.setTheme("ace/theme/clouds");
    editor.session.setMode("ace/mode/kotlin");
    editor.setOptions({
        maxLines: 25
    });

    // or use data: url to handle things like doctype
    function showHTMLInIFrame() {
        getImage("api/treechart", "results");
    }
    editor.on("input", showHTMLInIFrame);
</script>
<#include "chartcommon.ftlh">
<@chartCommon chart = "" treechart = "btn btn-active" stacked = "" mychart = ""/>