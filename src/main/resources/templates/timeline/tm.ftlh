<form class="pure-form pure-form-stacked" data-hx-put="api/timeline/" data-hx-target="#contentBox">
<fieldset id="response-div" >
    <legend>Timeline Builder</legend>
    <div class="pure-1-2">
        <label for="scale">Scale</label>
        <input type="number" id="scale" name="scale" data-hx-put="api/timeline/" data-hx-target="#contentBox"
               value="1.0" step=".10" max="5" min=".1" class="pure-u-1-4">
    </div>
        <div class="pure-1-2">
            <textarea id="content" name="content" class="pure-input-1" rows="20" placeholder="" data-hx-put="api/timeline/" data-hx-target="#contentBox">
-
date: 01/01/2024
text: First entry
-
date: 01/02/2024
text: Second entry
            </textarea>
        </div>
</fieldset>
<fieldset>
    <button class="button-success pure-button">Submit</button>
</fieldset>
</form>
<div id="contentBox" class="contentBox"></div>