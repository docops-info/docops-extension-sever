<div>
    <h1>Create Timeline</h1>
    <div id="contentBox" ></div>
    <div class="divider"></div>
<form class="max-h-full mx-auto" data-hx-put="api/timeline/" data-hx-target="#contentBox" hx-trigger="submit, load">

    <div class="mb-5">
        <label for="scale" class="mb-2">Scale</label>
        <input type="number" id="scale" name="scale" data-hx-put="api/timeline/" data-hx-target="#contentBox"
               value="0.6" step=".10" max="5" min=".1" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
    </div>

    <div class="mb-5">
        <label for="outline" class="mb-2">Outline Color</label>
        <input type="text" id="outline" name="outline" data-hx-put="api/timeline/" data-hx-target="#contentBox"
               class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" value="#9BCF53">
    </div>


    <div class="mb-5">
        <label for="content" class="mb-2">DSL</label>
            <textarea id="content" name="content" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" cols="90" rows="20" placeholder="" data-hx-put="api/timeline/" data-hx-target="#contentBox">
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

    <div class="mb-5">
        <button class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
    </div>
    <div class="mb-5">
        <input class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" type="button" onclick="copyTextToClipboard(adrSource);"
               value="Copy Source As AsciiDoc">
    </div>
</form>
</div>