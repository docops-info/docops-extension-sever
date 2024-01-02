<div>
    <p>Welcome to badge creator UI. It will help you create a badge and provide the asciidoctor syntax to use.</p>
</div>
<div id="contentBox"></div>
<div class="divider"></div>
<form class="grid grid-cols-2 gap-4" data-hx-put="api/badge/item" data-hx-target="#contentBox" data-hx-ext="json-enc" hx-trigger="submit, load">
                <div class="flex flex-col col-span-2">
                    <label for="label" class="mb-2">Label</label>
                    <input type="text" id="label" name="label" data-hx-put="api/badge/item" data-hx-target="#contentBox"
                           value="Build" class="p-2 border rounded-md">
                </div>
                <div class="flex flex-col col-span-2">
                    <label for="message" class="mb-2">Message</label>
                    <input type="text" id="message" name="message" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox"
                           value="Failed" class="p-2 border rounded-md">
                </div>
                <div class="flex flex-col">
                    <label for="url" class="mb-2">Url</label>
                    <input type="text" id="url" name="url" data-hx-put="api/badge/item" data-hx-target="#contentBox"
                           value="" class="p-2 border rounded-md">
                </div>
                <div class="flex flex-col">
                    <label for="fontColor" class="mb-2">Font Color</label>
                    <input type="text" id="fontColor" name="fontColor" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox'"
                           value="#fcfcfc" class="p-2 border rounded-md">

                </div>
                <div class="flex flex-col">
                    <label for="labelColor" class="mb-2">Label Color</label>

                    <input type="text" id="labelColor" name="labelColor" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox"
                           value="#000000" class="p-2 border rounded-md">

                </div>
                <div class="flex flex-col">
                    <label for="messageColor" class="mb-2">Message Color</label>
                    <input type="text" id="messageColor" name="messageColor" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox'"
                           value="#000000" class="p-2 border rounded-md">

                </div>
                <div class="flex flex-col col-span-2">
                    <span>A simpleicon with '&lt; &gt;' around the name or an url like(https://docops.io/images/docops.svg) to an SVG image</span>
                        <div class="flex flex-col">
                            <label for="logo" class="mb-2">Logo</label>
                            <input type="text" id="logo" name="logo" data-hx-put="api/badge/item" data-hx-target="#contentBox"
                                   value="<Tekton>"  class="p-2 border rounded-md">

                        </div>
                        <div class="flex flex-col">
                            <a class="btn btn-outline" data-hx-get="simpleicons.html" data-hx-target="#simpleicons">Show Simple Icon Set Below ⬇️</a>
                        </div>
                </div>
                <button class="btn btn-primary" data-hx-indicator="#spinner">Submit <img class="htmx-indicator" src="images/bars.svg"/></button>
            <div class="flex flex-col">
                <input class="btn btn-info" type="button" onclick="copyTextToClipboard(badgeSource);"
                       value="Copy Badge As AsciiDoc">
            </div>
</form>


<div id="simpleicons"></div>


<script>
    $("#messageColor").spectrum({
        color: "#c90076"
    });
    $("#labelColor").spectrum({
        color: "#d9d2e9"
    });
</script>
