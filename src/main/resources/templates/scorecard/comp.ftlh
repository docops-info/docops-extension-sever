<div>
    <h1>Create Comparison Chart</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="grid grid-cols-1 gap-4" data-hx-put="api/comp/" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="mb-2">
            <label class="block text-gray-700 text-sm font-bold mb-2" for="content">
                JSON Input
            </label>
            <textarea id="content" name="content" rows="16" class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="">
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
  ], "display": {

             "leftColumnColor":   "#98E4FF",
             "rightColumnColor":   "#0079FF"
        }
}
        </textarea>
        </div>
        <div class="mb-2 flex items-center justify-between">
            <button type="submit" class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline">Submit</button>
            <input type="button" class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline" onclick="copyTextToClipboard(adrSource);" value="Copy Source As AsciiDoc"></input>
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
