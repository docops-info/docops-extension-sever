<form class="pure-form pure-form-stacked" data-hx-put="api/timeline/" data-hx-target="#contentBox">
<fieldset id="response-div" >
    <legend>Timeline Builder</legend>
    <div class="join">
        <label for="scale" class="btn join-item">Scale</label>
        <input type="number" id="scale" name="scale" data-hx-put="api/timeline/" data-hx-target="#contentBox"
               value="1.0" step=".10" max="5" min=".1" class="input input-bordered input-primary w-full max-w-xs">
    </div>
    <div class="join">
        <label for="numChars" class="btn join-item">Number of Characters per Line</label>
        <input type="number" id="numChars" name="numChars" data-hx-put="api/timeline/" data-hx-target="#contentBox"
               value="24" step="1" max="60" min="10" class="input input-bordered input-primary w-full max-w-xs">
    </div>
<div>
    <div class="join">
        <label for="useDark" class="join-item">Use Dark Mode</label>
        <input type="checkbox" id="useDark" name="useDark" data-hx-put="api/timeline/" data-hx-target="#contentBox"
               checked class="join-item toggle toggle-primary">
    </div>
    <div class="join">
        <label for="outline" class="join-item">Outline Color</label>
        <input type="text" id="outline" name="outline" data-hx-put="api/timeline/" data-hx-target="#contentBox"
               class="join-item input input-bordered input-primary w-full max-w-x" value="#E9A6A6">
    </div>
</div>
    <div class="join">
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
</fieldset>
<fieldset>
    <button class="btn btn-primary">Submit</button>
    <input class="btn btn-secondary" type="button" onclick="copyTextToClipboard(adrSource);"
           value="Copy Source As AsciiDoc">
</fieldset>
</form>
<div class="divider"></div>
<div id="contentBox" class="contentBox"></div>
