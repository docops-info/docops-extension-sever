<div>
    <h1>Create Connector Diagrams</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>
<form class="pure-form pure-form-stacked" data-hx-put="api/connector/" data-hx-target="#contentBox" hx-trigger="submit, load">
<fieldset id="response-div" >
    <legend>Connector Builder</legend>
    <div class="join">
        <label for="scale" class="btn join-item">Scale</label>
        <input type="number" id="scale" name="scale" data-hx-put="api/connector/" data-hx-target="#contentBox"
               value="0.5" step=".10" max="5" min=".1" class="input input-bordered input-primary w-full max-w-xs">
    </div>
<div>
    <div class="join">
        <label for="useDark" class="join-item">Use Dark Mode</label>
        <input type="checkbox" id="useDark" name="useDark" data-hx-put="api/connector/" data-hx-target="#contentBox"
               checked class="join-item toggle toggle-primary">
    </div>
</div>

    <div class="join">
            <textarea id="content" name="content" class="textarea textarea-primary" cols="90" rows="20" placeholder="" data-hx-put="api/connector/" data-hx-target="#contentBox">
{
  "connectors": [
    {
      "text": "Engineer"
    },
    {
      "text": "Unit Tests"
    },
    {
      "text": "Microsoft Excel"
    },
    {
      "text": "GitHub"
    },
    {
      "text": "Test Engine"
    },
    {
      "text": "GitHub"
    },
    {
      "text": "API Documentation"
    }
  ]
}

            </textarea>
        </div>
</fieldset>
<fieldset>
    <button class="btn btn-primary">Submit</button>
    <input class="btn btn-secondary" type="button" onclick="copyTextToClipboard(adrSource);"
           value="Copy Source As AsciiDoc">
</fieldset>
</form>
</div>