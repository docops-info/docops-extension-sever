<div>
    <h1>Create Bar Charts</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="max-h-full mx-auto" data-hx-put="api/bar/barchart" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="mb-5">
            <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" cols="90" rows="20" placeholder="" data-hx-put="api/bar/barchart" data-hx-target="#contentBox">
{
  "title": "Favorite Movie Types",
  "yLabel": "People",
  "xLabel": "Movie Type",
  "series": [
    {
      "label": "Comedy",
      "value": 1300000
    },
    {
      "label": "Action",
      "value": 700000
    },
    {
      "label": "Romance",
      "value": 120000
    },
    {
      "label": "Drama",
      "value": 500000
    },
    {
      "label": "SciFi",
      "value": 180000
    }
  ],
  "display": {
    "baseColor": "#B43F3F",
    "vBar": true,
    "useDark": false,
    "sorted": false
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
</div>