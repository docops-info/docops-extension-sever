<#-- @ftlvariable name="json" type="java.lang.String" -->
<div>
    <h1>Create Scorecard from JSON</h1>
<div id="contentBox" ></div>
<div class="divider"></div>
    <form class="max-h-full mx-auto" data-hx-put="api/scorecard/form" data-hx-target="#contentBox" hx-trigger="submit, load">

        <div class="mb-5">
            <label for="payload" class="mb-2">JSON</label>
            <textarea id="payload" name="payload" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" rows="20" cols="90" placeholder="" data-hx-target="#contentBox">
${json}
            </textarea>
            </div>

        <div class="mb-5">
            <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
        </div>
        <div class="mb-5">
            <input class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVG('contentBox', 'scorecard.svg')" value="Download"><img class="htmx-indicator" src="images/bars.svg"/></input>
        </div>

    </form>
</div>


