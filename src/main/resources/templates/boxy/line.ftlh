<div>
    <h1>Create Line Charts</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="grid grid-cols-2 gap-4" data-hx-put="api/linechart/" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="flex flex-col col-span-2">
            <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="textarea textarea-primary" cols="90" rows="20" placeholder="" data-hx-put="api/linechart/" data-hx-target="#contentBox">
               {
  "id": "d31d2484-f343-4241-ae7f-22f20c57c5fb",
  "title": "Point on graph",
  "points": [
    [
      {
        "label": "Jan",
        "y": 40.0
      },
      {
        "label": "Feb",
        "y": 70.0
      },
      {
        "label": "Mar",
        "y": 90.0
      },
      {
        "label": "Apr",
        "y": 70.0
      },
      {
        "label": "May",
        "y": 40.0
      },
      {
        "label": "Jun",
        "y": 30.0
      },
      {
        "label": "Jul",
        "y": 60.0
      },
      {
        "label": "Aug",
        "y": 90.0
      },
      {
        "label": "Sept",
        "y": 70.0
      }
    ],
    [
      {
        "label": "Jan",
        "y": 22.0
      },
      {
        "label": "Feb",
        "y": 33.0
      },
      {
        "label": "Mar",
        "y": 44.0
      },
      {
        "label": "Apr",
        "y": 55.0
      },
      {
        "label": "May",
        "y": 66.0
      },
      {
        "label": "Jun",
        "y": 77.0
      },
      {
        "label": "Jul",
        "y": 88.0
      },
      {
        "label": "Aug",
        "y": 109.0
      },
      {
        "label": "Sept",
        "y": 110.0
      }
    ],
    [
      {
        "label": "Jan",
        "y": 56.0
      },
      {
        "label": "Feb",
        "y": 65.0
      },
      {
        "label": "Mar",
        "y": 78.0
      },
      {
        "label": "Apr",
        "y": 22.0
      },
      {
        "label": "May",
        "y": 160.0
      },
      {
        "label": "Jun",
        "y": 94.0
      },
      {
        "label": "Jul",
        "y": 56.0
      },
      {
        "label": "Aug",
        "y": 23.0
      },
      {
        "label": "Sept",
        "y": 201.0
      }
    ]
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