<div>
    <h1>Create Bar Charts</h1>
    <a class="tab btnMenu buttons-schema" hx-get="json/schema?name=bargroup-schema" data-hx-target="#contentBox"><i class="fad fa-curling text-xs mr-2"></i> JSON Schema</a>

    <div id="contentBox" ></div>
    <div class="divider"></div>
    <form class="grid grid-cols-2 gap-4" data-hx-put="api/bar/groupbarchart" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="flex flex-col col-span-2">
            <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="textarea textarea-primary" cols="90" rows="20" placeholder="" data-hx-put="api/bar/groupbarchart" data-hx-target="#contentBox">
{
  "title": "Show Me the Money",
  "yLabel": "Number of Sales",
  "xLabel": "Month",
  "groups": [
    {
      "label": "2024",
      "series": [
        {
          "label": "Jan",
          "value": 5000
        },
        {
          "label": "Feb",
          "value": 7000
        },
        {
          "label": "Mar",
          "value": 4000
        },
        {
          "label": "Apr",
          "value": 15000
        },
        {
          "label": "May",
          "value": 5700
        },
        {
          "label": "Jun",
          "value": 6400
        },
        {
          "label": "Jul",
          "value": 8322
        },
        {
          "label": "Aug",
          "value": 4400
        },
        {
          "label": "Sept",
          "value": 4520
        },
        {
          "label": "Oct",
          "value": 3200
        },
        {
          "label": "Nov",
          "value": 5025
        },
        {
          "label": "Dec",
          "value": 6000
        }
      ]
    }
  ],
  "display": {
    "baseColor": "#0bb4ff",
"barFontValueStyle": "font-family: Arial,Helvetica, sans-serif; fill: #fcfcfc; font-size:9px;"
  }
}
            </textarea>
        </div>
        <div class="flex flex-col">
            <button type="submit" class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4">Submit</button>
        </div>
        <div class="flex flex-col">
            <input type="button" class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4" onclick="copyTextToClipboard(adrSource);" value="Copy Source As AsciiDoc"></input>
        </div>
    </form>
    <div class="flex flex-col">
        Here is a list of display style attributes and the defaults
        <pre><code>
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