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
        <div class="flex flex-col">
            <button type="submit" class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4">Submit</button>
        </div>
        <div class="flex flex-col">
            <input type="button" class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4" onclick="copyTextToClipboard(adrSource);" value="Copy Source As AsciiDoc"></input>
        </div>
    </form>
</div>