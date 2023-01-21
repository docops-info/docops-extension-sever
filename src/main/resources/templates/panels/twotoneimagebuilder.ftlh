<H1>Two Color Panel Image Builder</H1>

<div class="pure-g">
    <div class="pure-u-8-24">
        <form class="pure-form pure-form-stacked" data-hx-put="api/panelimagetone" data-hx-target="#contentBox">
            <fieldset>
                <legend>Panel Image Builder</legend>
                <div class="pure-g">
                    <div class="pure-u-8-24">
                        <label for="fillColor1">Top Color</label>
                        <input type="text" id="fillColor1" name="fillColor1" data-hx-put="api/panelimagetone" data-hx-target="#contentBox"
                               value="#000000" class="pure-u-18-24">
                    </div>
                    <div class="pure-u-8-24">
                        <label for="fillColor2">Bottom Color</label>
                        <input type="text" id="fillColor2" name="fillColor2" data-hx-put="api/panelimagetone" data-hx-target="#contentBox"
                               value="#000000" class="pure-u-18-24">
                    </div>

                </div>
            </fieldset>
            <fieldset class="pure-group">
                <label for="line1">Line 1</label>
                <input type="text" id="line1" name="line1" data-hx-put="api/panelimagetone" data-hx-target="#contentBox"
                       value="Black"  class="pure-u-md-1-2">
                <label for="line2">Line 2</label>
                <input type="text" id="line2" name="line2" value="Panther" class="pure-input-1-2" placeholder="" data-hx-put="api/panelimagetone" data-hx-target="#contentBox">
            </fieldset>

            <fieldset class="pure-group">
                <label for="line1Size">Line 1 Size</label>
                <input type="number" id="line1Size" name="line1Size" data-hx-put="api/panelimagetone" data-hx-target="#contentBox"
                       value="60" step="1" max="100" min="0" class="pure-u-12-24">
                <label for="line2Size">Line 1 Size</label>
                <input type="number" id="line2Size" name="line2Size" data-hx-put="api/panelimagetone" data-hx-target="#contentBox"
                       value="60" step="1" max="100" min="0" class="pure-u-12-24">
            </fieldset>
            <fieldset>
                <button class="button-success pure-button">Submit</button>
                <input class="button-secondary pure-button" type="button" onclick="saveSvg(panelText, ImageText);"
                       value="Save Image">
            </fieldset>
        </form>
    </div>
    <div >
        <div class="pure-u-16-24">
            <section id="contentBox" class="tool-bar">

            </section>
        </div>
    </div>
</div>
<script>
    $("#fillColor1").spectrum({
        color: "#ffffff"
    });
    $("#fillColor2").spectrum({
        color: "#0000bf"
    });
    var saveSvg = function(elId, name) {
        var svgEl = document.getElementById(elId);
        svgEl.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        var svgData = svgEl.outerHTML;
        var preface = '<?xml version="1.0" standalone="no"?>\r\n';
        var svgBlob = new Blob([preface, svgData], {type:"image/svg+xml;charset=utf-8"});
        var svgUrl = URL.createObjectURL(svgBlob);
        var downloadLink = document.createElement("a");
        downloadLink.href = svgUrl;
        downloadLink.download = name;
        document.body.appendChild(downloadLink);
        downloadLink.click();
        document.body.removeChild(downloadLink);
    }
</script>

<#include "panelcommon.ftlh">
<@panelsMacro panelgenerator = "" panelimagebuilder= "" twotoneimagebuilder= "pure-button-primary pure-button-active" panelseditor= "" />
