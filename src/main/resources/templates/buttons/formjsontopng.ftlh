<div>
    <h1>Create Buttons from JSON</h1>

    <form class="pure-form pure-form-stacked" data-hx-put="api/buttons/form/png" data-hx-target="#contentBox" >

        <fieldset id="response-div" >
            <legend>JSON</legend>
            <div class="pure-1-2">
                <label for="payload">Insert content here</label>
                <textarea id="payload" name="payload" class="pure-input-1" rows="20" placeholder="" data-hx-target="#contentBox"></textarea>
            </div>
        </fieldset>
        <fieldset>
            <button class="button-success pure-button">Submit</button>
        </fieldset>
    </form>
</div>
<div id="contentBox" class="contentBox"></div>