<div id="contentBox" class="contentBox"></div>
<div class="divider"></div>

<form class="pure-form pure-form-stacked" data-hx-put="api/roadmap/" data-hx-target="#contentBox" hx-trigger="submit, load">
    <fieldset id="response-div" >
        <legend>Timeline Builder</legend>
        <div class="join">
            <label for="title" class="btn join-item">Title</label>
            <input type="text" id="title" name="title" data-hx-put="api/roadmap/" data-hx-target="#contentBox"
                   value="planner"  class="input input-bordered input-primary w-full max-w-xs">
        </div>
        <div class="join">
            <label for="scale" class="btn join-item">Scale</label>
            <input type="number" id="scale" name="scale" data-hx-put="api/roadmap/" data-hx-target="#contentBox"
                   value="1.0" step=".10" max="5" min=".1" class="join-item input input-bordered input-primary w-full max-w-xs">
        </div>
        <div class="join">
            <label for="numChars" class="btn join-item">Number of Characters per Line</label>
            <input type="number" id="numChars" name="numChars" data-hx-put="api/roadmap/" data-hx-target="#contentBox"
                   value="32" step="1" max="60" min="10" class="join-item input input-bordered input-primary w-full max-w-xs">
        </div>
        <div class="join"><label for="useDark" class="join-item">Use Dark Mode</label>
            <input type="checkbox" id="useDark" name="useDark" data-hx-put="api/roadmap/" data-hx-target="#contentBox"
                   checked class="join-item toggle toggle-primary"></div>
        <div >
            <textarea class="join-item  textarea textarea-primary" id="content" name="content" class="pure-input-1" rows="20" cols="90" placeholder="" data-hx-put="api/roadmap/" data-hx-target="#contentBox">
- now
Audit Log Streaming
- now
Fork the repository to Support Search
- later
API versioning (GA)
- next
Kotlin Coroutines support
- done
JSON schema definition
- later
code scanning and app scanning
            </textarea>
        </div>
    </fieldset>
    <fieldset>
        <button class="btn btn-active btn-primary">Submit</button>
        <button class="btn btn-active btn-secondary" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVG('contentBox', 'roadmap.svg')">Download <img class="htmx-indicator" src="images/bars.svg"/></button>
    </fieldset>
</form>


