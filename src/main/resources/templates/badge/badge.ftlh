<div>
    <p>Welcome to DocOps badge creator UI.
        This view helps developers create a badge and provide the asciidoctor syntax to use.
        The form helps build out the dsl syntax.
    </p>
</div>
<div id="contentBox"></div>
<div class="divider"></div>
<form class="max-w-sm mx-auto" data-hx-put="api/badge/item" data-hx-target="#contentBox" data-hx-ext="json-enc" hx-trigger="submit, load">
                <div class="mb-5">
                    <label for="label" class="mb-2">Label</label>
                    <input type="text" id="label" name="label" data-hx-put="api/badge/item" data-hx-target="#contentBox"
                           value="Powered By" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                </div>
                <div class="mb-5">
                    <label for="message" class="mb-2">Message</label>
                    <input type="text" id="message" name="message" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox"
                           value="Kotlin" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                </div>
                <div class="flex flex-col">
                    <label for="url" class="mb-2">Url</label>
                    <input type="text" id="url" name="url" data-hx-put="api/badge/item" data-hx-target="#contentBox"
                           value="" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                </div>
                <div class="flex flex-col">
                    <label for="fontColor" class="mb-2">Font Color</label>
                    <input type="text" id="fontColor" name="fontColor" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox'"
                           value="#fcfcfc" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">

                </div>
                <div class="flex flex-col">
                    <label for="labelColor" class="mb-2">Label Color</label>

                    <input type="text" id="labelColor" name="labelColor" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox"
                           value="#5b5b5b" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">

                </div>
                <div class="flex flex-col">
                    <label for="messageColor" class="mb-2">Message Color</label>
                    <input type="text" id="messageColor" name="messageColor" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox'"
                           value="#00dd00" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">

                </div>
                <div class="mb-5">
                    <span>Optionally add an icon. <a href="https://simpleicons.org">Simple Icons</a> are supported, use '&lt; &gt;' around the name. Custom icons are supported via url like(https://docops.io/images/docops.svg) to an SVG image</span>
                        <div class="flex flex-col">
                            <label for="logo" class="mb-2">Logo</label>
                            <input type="text" id="logo" name="logo" data-hx-put="api/badge/item" data-hx-target="#contentBox"
                                   value="<Kotlin>"  class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">

                        </div>
                        <div class="flex flex-col">
                            <a href="#" class="btn btn-outline" data-hx-get="simpleicons.html" data-hx-target="#simpleicons">Click here to show Simple Icon Set by letter below ⬇️. </a>
                        </div>
                </div>
                <button class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" data-hx-indicator="#spinner">Submit <img class="htmx-indicator" src="images/bars.svg"/></button>
            <div class="flex flex-col">
                <input class="text-gray-900 bg-gradient-to-r from-lime-200 via-lime-400 to-lime-500 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-lime-300 dark:focus:ring-lime-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" type="button" onclick="copyTextToClipboard(badgeSource);"
                       value="Copy Badge As AsciiDoc">
            </div>
</form>


<div id="simpleicons"></div>


<script>
    $("#messageColor").spectrum({
        color: "#00dd00"
    });
    $("#labelColor").spectrum({
        color: "#008800"
    });
</script>
