<div>
    <h1>Create SVG Table</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="max-h-full mx-auto" data-hx-put="api/table/" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="mb-2">
            <label class="block text-gray-700 text-sm font-bold mb-2" for="content">
                JSON Input
            </label>
            <textarea id="content" name="content" rows="16" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" placeholder="">
{
  "display": {"cellWidths": [0.20,0.20,0.35,0.25]},
  "rows": [
    {
      "cells": [
        {
          "data": "Cell 1  Create header row and assign it to thead Create header row and assign it to thead"
        },
        {
          "data": "Cell 2"
        },
        {
          "data": "Cell 3"
        },
        {
          "data": "Cell 4"
        }
      ]
    },
    {
      "cells": [
        {
          "data": "Cell 4"
        },
        {
          "data": "Cell 5"
        },
        {
          "data": "Cell certbot automatically checks for expiring certificates and renews them if necessary. This command is typically run periodically, such as through a cron job, to ensure continuous certificate validity."
        },
        {
          "data": "Cell 7"
        }
      ]
    },
    {
      "cells": [
        {
          "data": "Row 3, Cell 1 with long text to test word wrapping and alignment"
        },
        {
          "data": "Row 3, Cell 2"
        },
        {
          "data": "Row 3, Cell 3"
        },
        {
          "data": "Row 3, Cell 4"
        }
      ]
    },
    {
      "cells": [
        {
          "data": "Row 4, Cell 1"
        },
        {
          "data": "Row 4, Cell 2 with even longer text to test behavior"
        },
        {
          "data": "Row 4, Cell 3"
        },
        {
          "data": "Row 4, Cell 4 with unique font size",
          "fontSize": 14,
          "display": {
            "fill": {
              "color": "#fcfcfc"
            },
            "fontColor": {
              "color": "#000000"
            }
          }
        }
      ],
      "display": {
        "fill": {
          "color": "#fcfcfc"
        },
        "fontColor": {
          "color": "#000000"
        }
      }
    },
    {
      "cells": [
        {
          "data": "Row 5, Cell 1"
        },
        {
          "data": "Row 5, Cell 2"
        },
        {
          "data": "Row 5, Cell 3"
        },
        {
          "data": "Row 5, Cell 4 with custom text"
        }
      ]

    }
  ],
  "thead": {
    "rows": [
      {
        "cells": [
          {
            "data": "Header 1"
          },
          {
            "data": "Header 2"
          },
          {
            "data": "Header 3"
          },
          {
            "data": "Header 4"
          }
        ]
      }
    ]
  }
}
                 </textarea>
        </div>
        <div class="mb-5">
            <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
            <input type="button" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" onclick="copyTextToClipboard(adrSource);" value="Copy Source As AsciiDoc"></input>
        </div>
    </form>
</div>
<div>

</div>