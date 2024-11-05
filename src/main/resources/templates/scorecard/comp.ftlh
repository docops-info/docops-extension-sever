<div>
    <h1>Create Comparison Chart</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="max-h-full mx-auto" data-hx-put="api/comp/" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="mb-2">
            <label class="block text-gray-700 text-sm font-bold mb-2" for="content">
                JSON Input
            </label>
            <textarea id="content" name="content" rows="16" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" placeholder="">
            {
  "title": "Spring Boot 3 Version Upgrade",
  "colHeader": [
    "Started from the bottom",
    "Now we are here 🎧"
  ],
  "rows": [
    {
      "title": "Spring Boot 3 Version",
      "original": "2.7.0",
      "next": "3.3.3"
    },
    {
      "title": "Admin DashBoard?",
      "original": "No",
      "next": "Yes"
    },
    {
      "title": "Spring Security Version",
      "original": "5.7.2",
      "next": "5.8.2"
    },

    {
      "title": "Commons Http Client migrate 4 to 5?",
      "original": "4.5.13",
      "next": "5.2.1"
    },
    {
      "title": "High or Critical Vulnerabilities",
      "original": "12 High and 2 Critical",
      "next": "0 High and 0 Critical"
    }
  ],
    "display": {
          "itemColumnColor": "#0D92F4"
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
    <h1>List of Display Properties</h1>
    <div id="adrSource" class="text-sm">
        <pre><code>
        "display": {
             "scale": 1,
             "titleFontStyle":   "font-family: Arial,Helvetica, sans-serif; fill: #111111; font-size:24px; text-anchor:middle; font-weight: bold; font-variant: small-caps;",
             "leftColumnHeaderFontStyle":   "font-family: Arial,Helvetica, sans-serif; fill: #111111; font-size:20px; text-anchor:middle; font-weight: bold;",
             "rightColumnHeaderFontStyle":   "font-family: Arial,Helvetica, sans-serif; fill: #111111; font-size:20px; text-anchor:middle; font-weight: bold;",
             "leftColumnColor":   "#F73D93",
             "leftColumnFontColor":   "#fcfcfc",
             "rightColumnColor":   "#03C988",
             "rightColumnFontColor":   "#fcfcfc"
        }
        </code></pre>
    </div>
</div>
