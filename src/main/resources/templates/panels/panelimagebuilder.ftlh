<H1>Panel Image Builder</H1>

<div class="pure-g">
    <div class="pure-u-8-24">
        <form class="pure-form pure-form-stacked" data-hx-put="api/panelimage" data-hx-target="#contentBox">
            <fieldset>
                <legend>Panel Image Builder</legend>
                <div class="pure-g">
                    <div class="pure-u-8-24">
                        <label for="color">Fill Color</label>
                        <input type="text" id="color" name="fillColor" data-hx-put="api/panelimage" data-hx-target="#contentBox"
                               value="#000000" class="pure-u-18-24">
                    </div>
                    <div class="pure-u-8-24">
                        <label for="fontColor">Font Color</label>
                        <input type="text" id="fontColor" name="fontColor" data-hx-put="api/panelimage" data-hx-target="#contentBox"
                               value="#000000" class="pure-u-18-24">
                    </div>
                    <div class="pure-u-8-24">
                        <label for="imageType">Image Type</label>
                        <select id="imageType" name="imageType" data-hx-put="api/panelimage" data-hx-target="#contentBox">
                            <option value="LINE">LINE</option>
                            <option value="TAGLINE">TAGLINE</option>
                            <option value="CIRCLE">CIRCLE</option>
                        </select>
                    </div>
                </div>
            </fieldset>
            <fieldset class="pure-group">
                <label for="line1">Line 1</label>
                <input type="text" id="line1" name="line1" data-hx-put="api/panelimage" data-hx-target="#contentBox"
                       value="Black"  class="pure-u-md-1-2">
                <label for="line2">Line 2</label>
                <input type="text" id="line2" name="line2" value="Panther" class="pure-input-1-2" placeholder="" data-hx-put="api/panelimage" data-hx-target="#contentBox">
                <label for="line3">Line 3</label>
                <input type="text" id="line3" name="line3" value="Wakanda Forever" class="pure-input-1-2" placeholder="" data-hx-put="api/panelimage" data-hx-target="#contentBox">
                <label for="transparent">Transparent</label>
                <input type="checkbox" id="transparent" name="transparent" data-hx-put="api/panelimage" checked
                       data-hx-target="#contentBox">
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
    $("#color").spectrum({
        color: "#c90076"
    });
    $("#fontColor").spectrum({
        color: "#000000"
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
<@panelsMacro panelgenerator = "" panelimagebuilder= "btn btn-outline btn-active" twotoneimagebuilder= "" panelseditor= "" slimpanel="" />

