<div>
    <h1>Create Bar Charts</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="grid grid-cols-2 gap-4" data-hx-put="api/barchart/" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="flex flex-col col-span-2">
            <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="textarea textarea-primary" cols="90" rows="20" placeholder="" data-hx-put="api/barchart/" data-hx-target="#contentBox">
{
"title": "Show Me the Money",
"yLabel": "Number of Sales",
"xLabel": "Month",
"series": [
{"label": "Jan", "value":5000},
{"label": "Feb", "value":7000},
{"label": "Mar", "value":4000},
{"label": "Apr", "value":15000},
{"label": "May", "value":5700},
{"label": "Jun", "value":6400},
{"label": "Jul", "value":8322},
{"label": "Aug", "value":4400},
{"label": "Sept", "value":4520},
{"label": "Oct", "value":3200},
{"label": "November", "value":5025},
{"label": "December", "value":6000}
],
"display": {"showGrid": true, "baseColor": "#B80000", "barFontColor": "#fcfcfc"}
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