<div>
    <h1>Create Place Mat Diagrams</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>
  <button class="btn btn-active btn-secondary" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVG('imageblock', 'placemat.svg')">Download <img class="htmx-indicator" src="images/bars.svg"/></button>
<form class="grid grid-cols-2 gap-4" data-hx-put="api/placemat/" data-hx-target="#contentBox" hx-trigger="submit, load">
<div>
  <div class="flex flex-col">
    <label for="useDark" class="mb-2">Toggle Dark Mode</label>
    <input type="checkbox" id="useDark" name="useDark" data-hx-put="api/placemat/" data-hx-target="#contentBox"
           class="toggle toggle-primary">
  </div>
</div>

  <div class="flex flex-col col-span-2">
    <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="textarea textarea-primary" cols="90" rows="20" placeholder="" data-hx-put="api/placemat/" data-hx-target="#contentBox">
{
  "title": "Observability",
  "scale": 1.0,
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
        "color": "#FF5BAE",
        "legend": "Business Capability",
        "style":"fill:#fcfcfc"
      },
      {
        "color": "#003b6b",
        "legend": "Engineering",
        "style":"fill:#fcfcfc"
      },
      {
        "color": "#C40C0C",
        "legend": "Both",
        "style":"fill:#fcfcfc"
      }
    ]
  }
}

            </textarea>
        </div>
  <div class="flex flex-col">
    <button class="btn btn-primary">Submit</button>
  </div>
  <div class="flex flex-col">
    <input class="btn btn-secondary" type="button" onclick="copyTextToClipboard(adrSource);"
           value="Copy Source As AsciiDoc">
  </div>
</form>
</div>