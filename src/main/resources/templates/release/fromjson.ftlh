<div id="contentBox">
<h1>Load Builder from JSON</h1>

<form data-hx-put="api/release/prefill" data-hx-target="#contentBox">

<fieldset id="response-div" >
    <legend>JSON</legend>
    <div class="join">
        <label for="payload" class="btn join-item">Insert JSON here</label>
        <textarea id="payload" name="payload" class="join-item textarea textarea-primary" rows="20" cols="90" placeholder="" data-hx-put="api/release/prefill" data-hx-target="#contentBox">
        </textarea>
    </div>
</fieldset>
<fieldset>
    <button class="btn btn-primary">Submit</button>
</fieldset>
</form>
</div>
<#include "stratcommon.ftlh">
<@stratMacro builder = "" fromJson= "btn btn-primary btn-active"/>