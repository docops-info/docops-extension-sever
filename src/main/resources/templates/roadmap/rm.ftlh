<div id="contentBox" class="contentBox"></div>
<div class="divider"></div>

<form class="grid grid-cols-2 gap-4" data-hx-put="api/roadmap/" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="flex flex-col col-span-2">
            <label for="title" class="mb-2">Title</label>
            <input type="text" id="title" name="title" data-hx-put="api/roadmap/" data-hx-target="#contentBox"
                   value="planner" class="p-2 border rounded-md">
        </div>
        <div class="flex flex-col">
            <label for="scale" class="mb-2">Scale</label>
            <input type="number" id="scale" name="scale" data-hx-put="api/roadmap/" data-hx-target="#contentBox"
                   value="1.0" step=".10" max="5" min=".1" class="range border rounded-md">
        </div>
        <div class="flex flex-col">
            <label for="numChars" class="mb-2">Number of Characters per Line</label>
            <input type="number" id="numChars" name="numChars" data-hx-put="api/roadmap/" data-hx-target="#contentBox"
                   value="32" step="1" max="60" min="10" class="range border rounded-md">
        </div>
        <div class="flex flex-col">
            <label for="index" class="mb-2">Color Index (Max 26)</label>
            <input type="number" id="index" name="index" data-hx-put="api/roadmap/" data-hx-target="#contentBox"
                   value="26" step="1" max="26" min="0" class="range border rounded-md">
        </div>
        <div class="flex flex-col col-span-2">
            <label class="mb-2">DocOps Ascii Macro [docops, roadmap,title="Steve's Work", useDark=false, role=left]</label>
        </div>

    <div class="flex flex-col col-span-2">
        <label for="content" class="mb-2">Simple DSL</label>
            <textarea class="textarea textarea-primary" id="content" name="content" rows="20" cols="90" placeholder="" data-hx-put="api/roadmap/" data-hx-target="#contentBox">
- now Audit
Audit Log Streaming
- now Repo
Fork the repository to Support Search
- later API
API versioning (GA)
- next Kotlin
Kotlin Coroutines support
- done Schema
JSON schema definition
- later Vulnerability
code scanning and app scanning
            </textarea>
        </div>
    <div class="flex flex-col">
        <button class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4">Submit</button>
    </div>
    <div class="flex flex-col">
        <input class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVG('contentBox', 'roadmap.svg')" value="Download "><img class="htmx-indicator" src="images/bars.svg"/></input>
    </div>
</form>


