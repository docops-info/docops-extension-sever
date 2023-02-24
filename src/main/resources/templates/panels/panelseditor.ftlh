<h1>Panels</h1>
<button class="button-success pure-button"   onclick="copyTextToClipboard(editor.getValue());">Copy</button>
<button class="button-secondary pure-button" onclick="copyTextToClipboard(makeAdoc(editor.getValue()))">Copy as AsciiDoc</button>
<button class="button-secondary pure-button" onclick="downloadBlob('snippet.kts')">Download Snippet</button>
<br/>
<br/>
<div id="editor">
    panels {
        theme {
            colorMap {
                color("#FF9300")
                color("#c90076")
                color("#D3EAF1")
                color("#A93810")
            }
            legendOn = false
            layout {
                columns = 3
                groupBy = Grouping.ORDER
                groupOrder = GroupingOrder.ASCENDING
            }
            font = font {
                family = "Arial, Helvetica, sans-serif"
                size = "8pt"
                color = "black"
                spacing = "normal"
                bold = false
                italic = false
                underline = false
                vertical = false
                case = Case.NONE

            }
            newWin = false
            dropShadow = 0
        }
        rectangle {
            label = "Devices"
            link {
                label = "Apple"
                href = "https://www.apple.com"
            }
            buttonImage = ButtonImage(ref = "images/docops.svg")
        }
        rectangle {
            label = "Search Engine"
            link {
                href = "https://www.google.com"
                label = "Google Search"
            }
            link {
                href = "https://duckduckgo.com"
                label = "Duck Duck Go"
            }
            buttonImage = ButtonImage(ref = "images/trial.svg")
        }
        rectangle {
            label = "Devices 2"
            link {
                label = "Apple II"
                href = "https://www.apple.com"
            }
            buttonImage = ButtonImage(ref = "images/fav.svg")
        }
    }
</div>


<script>
    var editor = ace.edit("editor");
    editor.setTheme("ace/theme/clouds");
    editor.session.setMode("ace/mode/kotlin");
    editor.setOptions({
        maxLines: 15
    });
    editor.getSession().on('change', function() {
        showHTMLInIFrame()
    });

    function showHTMLInIFrame() //writes in <div> with id=output
    {
        getImage("api/panel/plain", "results");
    }

</script>
<br/>
<h3>Preview</h3>
<div id="results"></div>
<#include "panelcommon.ftlh">
<@panelsMacro panelgenerator = "" panelimagebuilder= "" twotoneimagebuilder= "" panelseditor= "pure-button-primary pure-button-active" slimpanel=""/>
