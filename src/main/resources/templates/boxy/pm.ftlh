<div>
    <h1>Create Place Mat Diagrams</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>
  <button class="btn btn-active btn-secondary" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVG('imageblock', 'placemat.svg')">Download <img class="htmx-indicator" src="images/bars.svg"/></button>
<form class="pure-form pure-form-stacked" data-hx-put="api/placemat/" data-hx-target="#contentBox" hx-trigger="submit, load">
<fieldset id="response-div" >
    <legend>Place Mat Builder</legend>
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
  "title": "Observability",
  "scale": 0.8,
  "placeMats": [
    {
      "name": "Audit",
      "legend": "Engineering"
    },
    {
      "name": "Traceability",
      "legend": "Engineering"
    },
    {
      "name": "Subscription",
      "legend": "Both"
    },
    {
      "name": "Automation",
      "legend": "Both"
    },
    {
      "name": "Metrics",
      "legend": "Engineering"
    },
    {
      "name": "Software Milestone",
      "legend": "Business Capability"
    },
    {
      "name": "Monitoring",
      "legend": "Engineering"
    },
    {
      "name": "Flakiness",
      "legend": "Engineering"
    },
    {
      "name": "Performance Regression",
      "legend": "Engineering"
    },
    {
      "name": "Misconfigurations",
      "legend": "Engineering"
    },
    {
      "name": "MTTR",
      "legend": "Both"
    },
    {
      "name": "Change Failure Rate",
      "legend": "Both"
    },
    {
      "name": "Deployment Frequency",
      "legend": "Both"
    }
  ],
  "useDark": false,
  "config": {
    "style": "font-size: 1em;",
    "legend": [
      {
        "color": "#b7b7a4",
        "legend": "Business Capability",
        "style":"fill:#111"
      },
      {
        "color": "#c77dff",
        "legend": "Engineering",
        "style":"fill:#111"
      },
      {
        "color": "#98c1d9",
        "legend": "Both",
        "style":"fill:#111"
      }
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