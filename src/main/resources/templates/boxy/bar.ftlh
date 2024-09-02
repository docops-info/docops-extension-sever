<div>
    <h1>Create Bar Charts</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="grid grid-cols-2 gap-4" data-hx-put="api/bar/barchart" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="flex flex-col col-span-2">
            <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="textarea textarea-primary" cols="90" rows="20" placeholder="" data-hx-put="api/bar/barchart" data-hx-target="#contentBox">
{
  "title": "Favorite Type of Movies",
  "yLabel": "People",
  "xLabel": "Movie Type",
  "series": [
    {
      "label": "Comedy",
      "value": 4,
      "itemDisplay": {
        "baseColor": "#5FBDFF",
        "barFontColor": "#fcfcfc"
      }
    },
    {
      "label": "Action",
      "value": 4
    },
    {
      "label": "Romance",
      "value": 6
    },
    {
      "label": "Drama",
      "value": 1,
      "itemDisplay": {
        "baseColor": "#B931FC",
        "barFontColor": "#111111"
      }
    },
    {
      "label": "SciFi",
      "value": 4
    }
  ],
  "display": {
    "showGrid": true,
    "baseColor": "#C21292",
    "barFontColor": "#fcfcfc"
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
</div>