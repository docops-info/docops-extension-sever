<form class="pure-form pure-form-stacked" data-hx-put="api/roadmap/" data-hx-target="#contentBox">
    <fieldset id="response-div" >
        <legend>Timeline Builder</legend>
        <div class="pure-1-2">
            <label for="title">Scale</label>
            <input type="text" id="title" name="title" data-hx-put="api/roadmap/" data-hx-target="#contentBox"
                   value="planner"  class="pure-u-1-4">
        </div>
        <div class="pure-1-2">
            <label for="scale">Scale</label>
            <input type="number" id="scale" name="scale" data-hx-put="api/roadmap/" data-hx-target="#contentBox"
                   value="1.0" step=".10" max="5" min=".1" class="pure-u-1-4">
        </div>
        <div class="pure-1-2">
            <label for="numChars">Number of Characters per Line</label>
            <input type="number" id="numChars" name="numChars" data-hx-put="api/roadmap/" data-hx-target="#contentBox"
                   value="32" step="1" max="60" min="10" class="pure-u-1-4">
        </div>
        <div class="pure-1-2">
            <textarea id="content" name="content" class="pure-input-1" rows="20" placeholder="" data-hx-put="api/roadmap/" data-hx-target="#contentBox">
- now
01/01/2024 First entry
- next
01/02/2024 Second entr
- later
do this later
            </textarea>
        </div>
    </fieldset>
    <fieldset>
        <button class="button-success pure-button">Submit</button>
        <button class="button-secondary pure-button" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVG('contentBox', 'roadmap.svg')">Download <img class="htmx-indicator" src="images/bars.svg"/></button>
    </fieldset>
</form>
<div id="contentBox" class="contentBox"></div>