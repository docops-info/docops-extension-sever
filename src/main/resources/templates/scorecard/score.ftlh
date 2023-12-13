<#-- @ftlvariable name="json" type="java.lang.String" -->
<div>
    <h1>Create Scorecard from JSON</h1>
<div id="contentBox" ></div>
<div class="divider"></div>
    <form class="grid grid-cols-2 gap-4" data-hx-put="api/scorecard/form" data-hx-target="#contentBox" hx-trigger="submit, load">

        <div class="flex flex-col col-span-2">
            <label for="payload" class="mb-2">JSON</label>
            <textarea id="payload" name="payload" class="textarea textarea-primary" rows="20" cols="90" placeholder="" data-hx-target="#contentBox">
${json}
            </textarea>
            </div>

        <div class="flex flex-col">
            <button type="submit" class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4">Submit</button>
        </div>
        <div class="flex flex-col">
            <input class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVG('contentBox', 'scorecard.svg')" value="Download"><img class="htmx-indicator" src="images/bars.svg"/></input>
        </div>

    </form>
</div>


