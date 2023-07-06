<div id="contentBox">
<h1>Load Builder from JSON</h1>

<form class="pure-form pure-form-stacked" data-hx-put="api/release/prefill" data-hx-target="#contentBox">

<fieldset id="response-div" >
    <legend>JSON</legend>
    <div class="pure-1-2">
        <label for="payload">Insert content here</label><textarea id="payload" name="payload" class="pure-input-1" rows="20" placeholder="" data-hx-put="api/release/prefill" data-hx-target="#contentBox">
            </textarea>
    </div>
</fieldset>
<fieldset>
    <button class="button-success pure-button">Submit</button>
</fieldset>
</form>
</div>
<#include "stratcommon.ftlh">
<@stratMacro builder = "" fromJson= "pure-button-primary pure-button-active"/>