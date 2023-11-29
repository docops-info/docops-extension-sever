<div>
    <h1>Create Place Mat Diagrams</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>
<form class="pure-form pure-form-stacked" data-hx-put="api/placemat/" data-hx-target="#contentBox" hx-trigger="submit, load">
<fieldset id="response-div" >
    <legend>Place Mat Builder</legend>
    <div class="join">
        <label for="scale" class="btn join-item">Scale</label>
        <input type="number" id="scale" name="scale" data-hx-put="api/placemat/" data-hx-target="#contentBox"
               value="0.5" step=".10" max="5" min=".1" class="input input-bordered input-primary w-full max-w-xs">
    </div>
<div>
    <div class="join">
        <label for="useDark" class="join-item">Use Dark Mode</label>
        <input type="checkbox" id="useDark" name="useDark" data-hx-put="api/placemat/" data-hx-target="#contentBox"
               checked class="join-item toggle toggle-primary">
    </div>
</div>

    <div class="join">
            <textarea id="content" name="content" class="textarea textarea-primary" cols="90" rows="20" placeholder="" data-hx-put="api/placemat/" data-hx-target="#contentBox">
{
  "placemats": [
    {
      "name": "Engineer",
      "colorIndex": 1
    },
    {
      "name": "Unit Tests",
      "colorIndex": 1
    },
    {
      "name": "Microsoft Excel",
      "colorIndex": 2
    },
    {
      "name": "GitHub",
      "colorIndex": 4
    },
    {
      "name": "Test Engine",
      "colorIndex": 3
    },
    {
      "name": "GitHub",
      "colorIndex": 6
    },
    {
      "name": "API Documentation",
      "colorIndex": 5
    }
  ],
  "config": {
    "baseColors": [
      "#CDF5FD",
      "#9AD0C2",
      "#FFEBD8",
      "#9EB8D9",
      "#E14D2A",
      "#82CD47",
      "#687EFF",
      "#C02739",
      "#FEC260",
      "#e9d3ff",
      "#7fc0b7"
    ]
  }
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