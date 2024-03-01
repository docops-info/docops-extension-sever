<div>
    <h1>Create Connector Diagrams</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="grid grid-cols-2 gap-4" data-hx-put="api/connector/" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="flex flex-col">
            <label for="scale" class="mb-2">Scale</label>
            <input type="number" id="scale" name="scale" data-hx-put="api/connector/" data-hx-target="#contentBox"
                   value="0.5" step=".10" max="5" min=".1" class="range border rounded-md">
        </div>
        <div class="flex flex-col">
            <label for="useDark" class="mb-2">Toggle Dark Mode</label>
            <input type="checkbox" id="useDark" name="useDark" data-hx-put="api/connector/" data-hx-target="#contentBox"
                   class="toggle toggle-primary">
        </div>
        <div class="flex flex-col col-span-2">
            <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="textarea textarea-primary" cols="90" rows="20" placeholder="" data-hx-put="api/connector/" data-hx-target="#contentBox">
{
  "connectors": [
    {
      "text": "Engineer",
      "description": "Creates tests"
    },
    {
      "text": "Unit Tests",
      "description": "Run Unit Tests"
    },
    {
      "text": "Microsoft Excel",
      "description": "Add test cases to excel"
    },
    {
      "text": "GitHub",
      "description": "Upload to Github"
    },
    {
      "text": "Test Engine",
      "description": "GitHub webhook plugged into engine"
    },
    {
      "text": "GitHub",
      "description": "Results stored in Github and triggers webhook for documentation"
    },
    {
      "text": "API Documentation",
      "description": "API documentation ready for consumption"
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