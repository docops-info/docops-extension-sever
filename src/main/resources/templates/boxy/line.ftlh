<div>
    <h1>Create Line Charts</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="max-h-full mx-auto" data-hx-put="api/linechart/" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="mb-5">
            <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" cols="90" rows="20" placeholder="" data-hx-put="api/linechart/" data-hx-target="#contentBox">
               {
  "id": "d31d2484-f343-4241-ae7f-22f20c57c5fb",
  "title": "Product",
  "points": [
    {
      "label": "Affordable Segment",
      "points": [
        {
          "label": "Jan",
          "y": 173
        },
        {
          "label": "Feb",
          "y": 153
        },
        {
          "label": "Mar",
          "y": 195
        },
        {
          "label": "Apr",
          "y": 147
        },
        {
          "label": "May",
          "y": 120
        },
        {
          "label": "Jun",
          "y": 144
        },
        {
          "label": "Jul",
          "y": 148
        },
        {
          "label": "Aug",
          "y": 109
        },
        {
          "label": "Sept",
          "y": 174
        },
        {
          "label": "Oct",
          "y": 130
        },
        {
          "label": "Nov",
          "y": 172
        },
        {
          "label": "Dec",
          "y": 132
        }
      ]
    },
    {
      "label": "Luxury Segment",
      "points": [
        {
          "label": "Jan",
          "y": 189.0
        },
        {
          "label": "Feb",
          "y": 189.0
        },
        {
          "label": "Mar",
          "y": 105.0
        },
        {
          "label": "Apr",
          "y": 112.0
        },
        {
          "label": "May",
          "y": 173.0
        },
        {
          "label": "Jun",
          "y": 109.0
        },
        {
          "label": "Jul",
          "y": 151.0
        },
        {
          "label": "Aug",
          "y": 197.0
        },
        {
          "label": "Sept",
          "y": 174.0
        },
        {
          "label": "Oct",
          "y": 145
        },
        {
          "label": "Nov",
          "y": 177
        },
        {
          "label": "Dec",
          "y": 167
        }
      ]
    },
    {
      "label": "Super Luxery Segment",
      "points": [
        {
          "label": "Jan",
          "y": 185.0
        },
        {
          "label": "Feb",
          "y": 185.0
        },
        {
          "label": "Mar",
          "y": 126.0
        },
        {
          "label": "Apr",
          "y": 134.0
        },
        {
          "label": "May",
          "y": 196.0
        },
        {
          "label": "Jun",
          "y": 153.0
        },
        {
          "label": "Jul",
          "y": 112.0
        },
        {
          "label": "Aug",
          "y": 133.0
        },
        {
          "label": "Sept",
          "y": 200.0
        },
        {
          "label": "Oct",
          "y": 145
        },
        {
          "label": "Nov",
          "y": 167
        },
        {
          "label": "Dec",
          "y": 110
        }
      ]
    }
  ]
}
            </textarea>
        </div>
        <div class="mb-5">
            <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
        </div>
        <div class="mb-5">
            <input type="button" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" onclick="copyTextToClipboard(adrSource);" value="Copy Source As AsciiDoc"></input>
        </div>
    </form>
</div>