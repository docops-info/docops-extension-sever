<H1>Badge Builder</H1>

<div>
    <p>Welcome to badge creator UI. It will help you create a badge and provide the asciidoctor syntax to use.</p>
</div>
<form class="pure-form pure-form-stacked" data-hx-put="api/badge/item" data-hx-target="#contentBox" data-hx-ext="json-enc">
    <fieldset>
        <legend>Create Badge</legend>
        <div>
            <div>
                <div class="join">
                    <label for="label" class="btn join-item">Label</label>
                    <input type="text" id="label" name="label" data-hx-put="api/badge/item" data-hx-target="#contentBox"
                           value="Build" class="join-item input input-bordered input-primary w-full max-w-xs">
                </div>
            </div>
            <div>
                <div class="join">
                    <label for="message" class="btn join-item">Message</label>
                    <input type="text" id="message" name="message" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox"
                           value="Failed" class="join-item input input-bordered input-primary w-full max-w-xs">
                </div>
            </div>
            <div>
                <div class="join">
                    <label for="url" class="btn join-item">Url</label>
                    <input type="text" id="url" name="url" data-hx-put="api/badge/item" data-hx-target="#contentBox"
                           value="" class="join-item input input-bordered input-primary w-full max-w-xs">
                </div>
            </div>
            <div>
                <div class="join">
                    <label for="labelColor" class="btn join-item">Label Color</label>

                    <input type="text" id="labelColor" name="labelColor" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox"
                           value="#000000" class="join-item input input-bordered input-primary w-full max-w-xs">

                </div>
            </div>
            <div>
                <div class="join">
                    <label for="messageColor" class="btn join-item">Message Color</label>
                    <input type="text" id="messageColor" name="messageColor" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox'"
                           value="#000000" class="join-item input input-bordered input-primary w-full max-w-xs">

                </div>
            </div>
                <div>
                    <span>A simpleicon with '&lt; &gt;' around the name or an url like(https://docops.io/images/docops.svg) to an SVG image</span>
                    <div>
                        <div class="join">
                            <label for="logo" class="btn join-item">Logo</label>
                            <input type="text" id="logo" name="logo" data-hx-put="api/badge/item" data-hx-target="#contentBox"
                                   value="<Tekton>"  class="join-item input input-bordered input-primary w-full max-w-xs">

                        </div>
                        <div class="pure-u-8-24">
                            <a class="btn btn-outline" data-hx-get="simpleicons.html" data-hx-target="#simpleicons">Show Simple Icon Set</a>
                        </div>
                    </div>
                </div>
                <button class="btn btn-primary" data-hx-indicator="#spinner">Submit <img class="htmx-indicator" src="images/bars.svg"/></button>
            <div class="pure-u-1-2">
                <input class="btn btn-secondary" type="button" onclick="copyTextToClipboard(badgeSource);"
                       value="Copy Badge As AsciiDoc">
            </div>
        </div>
    </fieldset>
</form>

<div class="divider"></div>
<div id="contentBox"></div>
<div id="simpleicons"></div>


<script>
    $("#messageColor").spectrum({
        color: "#c90076"
    });
    $("#labelColor").spectrum({
        color: "#d9d2e9"
    });
</script>
