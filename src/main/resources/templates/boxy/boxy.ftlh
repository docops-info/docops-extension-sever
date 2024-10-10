<div>
    <h1>Create Connector Diagrams</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="max-h-full mx-auto" data-hx-put="api/connector/" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="mb-5">
            <label for="scale" class="mb-2">Scale</label>
            <input type="number" id="scale" name="scale" data-hx-put="api/connector/" data-hx-target="#contentBox"
                   value="0.5" step=".10" max="5" min=".1" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
        </div>
        <div class="mb-5">
            <label for="useDark" class="inline-flex items-center cursor-pointer">
            <input type="checkbox" id="useDark" name="useDark" data-hx-put="api/connector/" data-hx-target="#contentBox"
                   class="sr-only peer">
            <div class="relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
            <span class="ms-3 text-sm font-medium text-gray-900 dark:text-gray-300">Toggle Dark Mode</span>
            </label>
        </div>
        <div class="mb-5">
            <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" cols="90" rows="20" placeholder="" data-hx-put="api/connector/" data-hx-target="#contentBox">
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
            <button type="submit" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
        </div>
        <div class="flex flex-col">
            <input type="button" class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" onclick="copyTextToClipboard(adrSource);" value="Copy Source As AsciiDoc"></input>
        </div>
    </form>
</div>