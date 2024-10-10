<div>
    <h1>Create Place Mat Diagrams</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>
  <button class="btn btn-active btn-secondary" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVG('imageblock', 'placemat.svg')">Download <img class="htmx-indicator" src="images/bars.svg"/></button>
<form class="max-h-full mx-auto" data-hx-put="api/placemat/" data-hx-target="#contentBox" hx-trigger="submit, load">
<div>
  <div class="mb-5">
    <label for="useDark" class="inline-flex items-center cursor-pointer">
    <input type="checkbox" id="useDark" name="useDark" data-hx-put="api/placemat/" data-hx-target="#contentBox"
           class="sr-only peer">
      <div class="relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
      <span class="ms-3 text-sm font-medium text-gray-900 dark:text-gray-300">Toggle Dark Mode</span>
    </label>
  </div>
</div>

  <div class="mb-5">
    <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" cols="90" rows="20" placeholder="" data-hx-put="api/placemat/" data-hx-target="#contentBox">
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
  <div class="mb-5">
    <button class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
  </div>
  <div class="mb-5">
    <input class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" type="button" onclick="copyTextToClipboard(adrSource);"
           value="Copy Source As AsciiDoc">
  </div>
</form>
</div>