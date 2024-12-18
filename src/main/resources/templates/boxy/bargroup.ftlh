<div>
    <h1>Create Bar Charts</h1>
    <a class="tab btnMenu buttons-schema" hx-get="json/schema?name=bargroup-schema" data-hx-target="#contentBox"><i class="fad fa-curling text-xs mr-2"></i> JSON Schema</a>

    <div id="contentBox" ></div>
    <div class="divider"></div>
    <form class="max-h-full mx-auto" data-hx-put="api/bar/groupbarchart" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="mb-5">
            <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" cols="90" rows="20" placeholder="" data-hx-put="api/bar/groupbarchart" data-hx-target="#contentBox">
{
  "title": "New Revenue",
  "yLabel": "Revenue",
  "xLabel": "Quarter",
  "groups": [
    {
      "label": "2020-Q1",
      "series": [
        {
          "label": "Kent",
          "value": 44700
        },
        {
          "label": "Lincoln",
          "value": 52800
        },
        {
          "label": "Mersey",
          "value": 43500
        },
        {
          "label": "York",
          "value": 38800
        }
      ]
    },
    {
      "label": "2020-Q2",
      "series": [
        {
          "label": "Kent",
          "value": 45000
        },
        {
          "label": "Lincoln",
          "value": 36500
        },
        {
          "label": "Mersey",
          "value": 41000
        },
        {
          "label": "York",
          "value": 34100
        }
      ]
    },
    {
      "label": "2020-Q3",
      "series": [
        {
          "label": "Kent",
          "value": 51200
        },
        {
          "label": "Lincoln",
          "value": 44200
        },
        {
          "label": "Mersey",
          "value": 39700
        },
        {
          "label": "York",
          "value": 27000
        }
      ]
    },
    {
      "label": "2020-Q4",
      "series": [
        {
          "label": "Kent",
          "value": 56500
        },
        {
          "label": "Lincoln",
          "value": 45300
        },
        {
          "label": "Mersey",
          "value": 41200
        },
        {
          "label": "York",
          "value": 48900
        }
      ]
    }
  ],
  "display": {
    "baseColor": "#F4F6FF",
    "scale": 1.0,
    "condensed": false,
    "useDark": false,
    "lineColor": "#00bfa0"
  }
}
            </textarea>
        </div>
        <div class="flex flex-col">
            <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
        </div>
        <div class="flex flex-col">
            <input type="button" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" onclick="copyTextToClipboard(adrSource);" value="Copy Source As AsciiDoc"></input>
        </div>
    </form>
    <div class="flex flex-col">
        Here is a list of display style attributes and the defaults
        <pre class="scrollbar-none overflow-x-auto p-6 text-sm leading-snug text-white bg-black bg-opacity-75 json hljs language-json"><code lang="json">
        "barSeriesFontStyle":"font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:9px; text-anchor:middle"
        "barSeriesLabelFontStyle":"font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:9px; text-anchor:start;"
        "barFontValueStyle":"font-family: Arial,Helvetica, sans-serif; fill: #111111; font-size:9px;"
        "titleStyle":"font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:14px; text-anchor:middle"
        "xLabelStyle":"font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:12px; text-anchor:middle"
        "yLabelStyle": "font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:12px; text-anchor:middle"
        "lineColor": "#FFBB5C"
        </code>
            </pre>
    </div>
</div>