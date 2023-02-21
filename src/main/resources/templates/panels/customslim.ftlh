<h3>Customize Slim Panel Theme</h3>

<div class="pure-g">
    <form class="pure-form pure-form-stacked" data-hx-put="api/slimpanelcustom" data-hx-target="#contentBox">
        <fieldset>
        <div class="pure-u-6-24">
            <label for="color1">Color1</label>
            <input type="text" id="color1" name="color1"
                   value="${theme.color1}" class="pure-u-6-24">
        </div>
        <div class="pure-u-6-24">
            <label for="color2">Color2</label>
            <input type="text" id="color2" name="color2"
                   value="${theme.color2}" class="pure-u-6-24">
        </div>
        <div class="pure-u-6-24">
            <label for="color3">Color3</label>
            <input type="text" id="color3" name="color3"
                   value="${theme.color3}" class="pure-u-6-24">
        </div>
        <div class="pure-u-6-24">
            <label for="fontColor">Font Color</label>
            <input type="text" id="fontColor" name="fontColor"
                   value="#ffffff" class="pure-u-6-24">
        </div>
        <div class="pure-u-6-24">
            <label for="titleColor">Title Color</label>
            <input type="text" id="titleColor" name="titleColor"
                   value="#000000" class="pure-u-6-24">
        </div>
        <div class="pure-u-6-24">
            <label for="strokeColor">Stroke Color</label>
            <input type="text" id="strokeColor" name="strokeColor"
                   value="#000000" class="pure-u-6-24">
        </div>
            <div class="pure-u-1-4">
                <label for="strokeWidth">Stroke Width</label>
                <input type="number" id="strokeWidth" name="strokeWidth" data-hx-put="api/slimpanelcustom" data-hx-target="#contentBox"
                       value="${theme.panelStroke.width}" step="1" max="9" min="1" class="pure-u-1-4">
            </div>
        </fieldset>
        <div class="pure-u-18-24">
        <button class="button-success pure-button">Submit</button>
        <input class="button-secondary pure-button" type="button" onclick="copyTextToClipboard(panelSource);"
               value="Copy Panel As AsciiDoc">
        </div>
    </form>
</div>
<script>
    $("#color1").spectrum({
        color: "${theme.color1}"
    });
    $("#color2").spectrum({
        color: "${theme.color2}"
    });
    $("#color3").spectrum({
        color: "${theme.color3}"
    });

    $("#fontColor").spectrum({
        color: "${theme.fontColor}"
    });
    $("#titleColor").spectrum({
        color: "${theme.titleColor}"
    });
    $("#strokeColor").spectrum({
        color: "${theme.panelStroke.color}"
    });
</script>

<div class="pure-g">
    <div class="pure-u-22-24">
        <section id="contentBox" class="tool-bar">

        </section>
    </div>
</div>
