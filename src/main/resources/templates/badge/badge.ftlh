<div>

    <section class="bg-white dark:bg-gray-900">
        <div class="py-8 px-4 mx-auto max-w-screen-xl text-center lg:py-16">
            <h1 class="mb-4 text-4xl font-extrabold tracking-tight leading-none text-gray-900 md:text-5xl lg:text-6xl dark:text-white">DocOps badge creator UI</h1>
            <p class="mb-8 text-lg font-normal text-gray-500 lg:text-xl sm:px-16 lg:px-48 dark:text-gray-400">
                The purpose of the DocOps badge creator UI is to streamline the process for developers
                to create badges efficiently and correctly.
                By providing a user-friendly interface and
                the necessary syntax, it aims to simplify the task and ensure that the output is
                syntactically correct and ready for use in Asciidoctor.
            </p>

        </div>
    </section>
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
                    <input type="color" id="fontColor" name="fontColor" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox'"
                           value="#fcfcfc" class="p-1 h-10 w-14 block bg-white border border-gray-200 cursor-pointer rounded-lg disabled:opacity-50 disabled:pointer-events-none dark:bg-neutral-900 dark:border-neutral-700">

                </div>
                <div class="flex flex-col">
                    <label for="labelColor" class="mb-2">Label Color</label>

                    <input type="color" id="labelColor" name="labelColor" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox"
                           value="#5b5b5b" class="p-1 h-10 w-14 block bg-white border border-gray-200 cursor-pointer rounded-lg disabled:opacity-50 disabled:pointer-events-none dark:bg-neutral-900 dark:border-neutral-700">

                </div>
                <div class="flex flex-col">
                    <label for="messageColor" class="mb-2">Message Color</label>
                    <input type="color" id="messageColor" name="messageColor" data-hx-put="api/badge/item"
                           data-hx-target="#contentBox'"
                           value="#00dd00" class="p-1 h-10 w-14 block bg-white border border-gray-200 cursor-pointer rounded-lg disabled:opacity-50 disabled:pointer-events-none dark:bg-neutral-900 dark:border-neutral-700">

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
