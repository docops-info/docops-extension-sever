<div>
    <h1>Create Pie Charts</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>

    <form class="grid grid-cols-2 gap-4" data-hx-put="api/pieslice/" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="flex flex-col col-span-2">
            <label for="content" class="mb-2">JSON</label>
            <textarea id="content" name="content" class="textarea textarea-primary" cols="90" rows="20" placeholder="" data-hx-put="api/pieslice/" data-hx-target="#contentBox">
                {
  "title": "Favorite Anime",
  "slices": [
    {
      "label": "Naruto",
      "amount": 5.0
    },
    {
      "label": "Bleach",
      "amount": 4.0
    },
    {
      "label": "One Piece",
      "amount": 9.0
    },
    {
      "label": "One Punch Man",
      "amount": 7.0
    },
    {
      "label": "My Hero Academia",
      "amount": 6.0
    },
    {
      "label": "Demon Slayer",
      "amount": 10.0
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