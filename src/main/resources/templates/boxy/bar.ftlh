<div>
    <h1>Create Bar Charts</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="max-h-full mx-auto" data-hx-put="api/bar/barchart" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="mb-5">
            <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" cols="90" rows="20" placeholder="" data-hx-put="api/bar/barchart" data-hx-target="#contentBox">
{
  "title": "Show Me the Money",
  "yLabel": "Number of Sales",
  "xLabel": "Month",

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
      ],
  "display": {
"baseColor": "#D9B650", "vBar": true
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