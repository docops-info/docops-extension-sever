<div>
    <h1>Create Timeline</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>
<form class="grid grid-cols-2 gap-4" data-hx-put="api/timeline/" data-hx-target="#contentBox" hx-trigger="submit, load">

    <div class="flex flex-col">
        <label for="scale" class="mb-2">Scale</label>
        <input type="number" id="scale" name="scale" data-hx-put="api/timeline/" data-hx-target="#contentBox"
               value="0.6" step=".10" max="5" min=".1" class="range border rounded-md">
    </div>
    <div class="flex flex-col">
        <label for="numChars" class="mb-2">Number of Characters per Line</label>
        <input type="number" id="numChars" name="numChars" data-hx-put="api/timeline/" data-hx-target="#contentBox"
               value="24" step="1" max="60" min="10" class="range border rounded-md">
    </div>

    <div class="flex flex-col">
        <label for="outline" class="mb-2">Outline Color</label>
        <input type="text" id="outline" name="outline" data-hx-put="api/timeline/" data-hx-target="#contentBox"
               class="p-2 border rounded-md" value="#9BCF53">
    </div>


    <div class="flex flex-col col-span-2">
        <label for="content" class="mb-2">DSL</label>
            <textarea id="content" name="content" class="textarea textarea-primary" cols="90" rows="20" placeholder="" data-hx-put="api/timeline/" data-hx-target="#contentBox">
-
date: 1891
text: Mailbox, invented by Phillip Downing
-
date: 1923
text: The Three-Light Traffic Signal, invented by Garrett Morgan
-
date: 1932
text: Automatic Gear Shift, invented by Richard Spikes
date: July 23rd, 2023
text: DocOps extension Server releases a new feature, Timeline Maker
for [[https://github.com/asciidoctor/asciidoctorj asciidoctorj]]. With a simple text markup block you can
create very powerful timeline images. Enjoy!
-
date: August 15th, 2023
text: DocOps.io revamping website with updated documentation. All
our work will be updated with latest documentation for Panels,
for extension server are the various plug-ing for asciidoctorj.
-
date: September 1st, 2023
text: DocOps.io will begin work on revamping the asciidoctorj converter.
With the asciidoctorj 3.0.0 release coming we will need to migrate
custom plug-ins to the new version.
-
date: October 18th, 2023
text: Time to reimagine the future. Is it possible
to write a lexer parser for custom language?
-
date: December 11th, 2023
text: Annual start of vacation, time to relax
and plugin the controller.
-
date: 01/01/2024
text: First entry where we show text is wrapping or not and the [[https://roach.gy roach.gy]] aligning properly
            </textarea>
        </div>

    <div class="flex flex-col">
        <button class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4">Submit</button>
    </div>
    <div class="flex flex-col">
        <input class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4" type="button" onclick="copyTextToClipboard(adrSource);"
               value="Copy Source As AsciiDoc">
    </div>
</form>
</div>