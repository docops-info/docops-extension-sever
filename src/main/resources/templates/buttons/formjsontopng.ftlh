<div>
    <h1>Create Buttons from JSON</h1>

    <div id="contentBox"></div>
    <div class="divider"></div>

    <form class="grid grid-cols-2 gap-4" data-hx-put="api/buttons/form/png" data-hx-target="#contentBox" hx-trigger="submit, load">

        <div class="flex flex-col col-span-2">
        <label for="payload" class="mb-2">JSON</label>
        <textarea id="payload" name="payload" class="join-item textarea textarea-primary" rows="20" cols="90" placeholder="" data-hx-target="#contentBox">
            {
  "buttons": [
    {
      "label": "Cookies & Cream",
      "link": "https://www.apple.com",
      "description": "Chocolate & Cheesecake Ice Creams with Chocolate Cookies & a Cheesecake Core",
      "type": "Cookies"
    },
    {
      "label": "Mint Chocolate Chance",
      "link": "https://www.google.com",
      "description": "Mint Ice Cream Loaded with Fudge Brownies",
      "type": "Mint"
    },
    {
      "label": "New York Super Fudge Chunk",
      "link": "https://www.microsoft.com",
      "description": "Chocolate Ice Cream with White & Dark Fudge Chunks, Pecans, Walnuts & Fudge-Covered Almonds",
      "type": "Fudge"
    },
    {
      "label": "Cherry Garcia",
      "link": "https://www.amazon.com",
      "description": "Cherry Ice Cream with Cherries & Fudge Flakes",
      "type": "Fruit",
      "author": [
        "Steve"
      ]
    },
    {
      "label": "Chunky Monkey",
      "link": "https://www.facebook.com",
      "description": "Banana Ice Cream with Fudge Chunks & Walnuts",
      "type": "FB",
      "author": [
        "Duffy",
        "Rose"
      ],
      "cardLine1": {
        "line": "Chunky",
        "size": "34px"
      },
      "cardLine2": {
        "line": "Monkey",
        "size": "34px"
      }
    }
  ],
  "buttonType": "REGULAR",
  "theme": {
    "colors": [
      "#01005E", "#22267B", "#28518A", "#04879C",
      "#0C3C78", "#639CD9", "#5454C5", "#5F85DB",
      "#576CBC", "#19376D", "#301E67", "#2F58CD"
    ],
    "scale": 1.0,
    "columns": 3,
    "sortBy": {"sort": "LABEL"},
    "buttonStyle": {
      "labelStyle": "font-family: Arial, Helvetica, sans-serif; font-size: 12px; fill: #fcfcfc; font-weight: 700; font-style: normal; font-variant: small-caps;"
    }
  }
}

        </textarea>
        </div>
        <div class="flex flex-col">
            <button class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4">Submit</button>
        </div>
    </form>
</div>
