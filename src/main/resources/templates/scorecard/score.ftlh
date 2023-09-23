<#-- @ftlvariable name="json" type="java.lang.String" -->
<div>
    <h1>Create Scorecard from JSON</h1>

    <form class="pure-form pure-form-stacked" data-hx-put="api/scorecard/form" data-hx-target="#contentBox">

        <fieldset id="response-div" >
            <legend>JSON</legend>
            <div class="pure-1-2">
                <label for="payload">Insert content here</label>
            <textarea id="payload" name="payload" class="textarea textarea-primary" rows="20" cols="90" placeholder="" data-hx-target="#contentBox">
${json}
            </textarea>
            </div>
        </fieldset>
        <fieldset>
            <button class="btn btn-primary">Submit</button>
            <button class="btn btn-secondary" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVG('contentBox', 'scorecard.svg')">Download <img class="htmx-indicator" src="images/bars.svg"/></button>
        </fieldset>
    </form>
</div>

<div class="divider"></div>
<div id="contentBox" class="contentBox"></div>
