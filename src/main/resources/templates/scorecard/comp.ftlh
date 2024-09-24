<div>
    <h1>Create Comparison Chart</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="grid grid-cols-2 gap-4" data-hx-put="api/comp/" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="flex flex-col col-span-2">
            <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="textarea textarea-primary" cols="90" rows="20" placeholder="" data-hx-put="api/comp/" data-hx-target="#contentBox">
{
  "title": "Spring Boot 3 Version Upgrade",
  "colHeader": [
    "Started from the bottom",
    "Now we are here🎧"
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
  ]
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