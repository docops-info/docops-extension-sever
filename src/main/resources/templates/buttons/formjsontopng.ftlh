<div>
    <h1>Create Buttons from JSON</h1>

    <form class="pure-form pure-form-stacked" data-hx-put="api/buttons/form/png" data-hx-target="#contentBox" >

        <fieldset id="response-div" >
            <legend>JSON</legend>
            <div class="join">
                <label for="payload" class="btn join-item">Insert JSON here</label>
                <textarea id="payload" name="payload" class="join-item textarea textarea-primary" rows="20" cols="90" placeholder="" data-hx-target="#contentBox"></textarea>
            </div>
        </fieldset>
        <fieldset>
            <button class="btn btn-primary">Submit</button>
        </fieldset>
    </form>
</div>
<div id="contentBox" class="contentBox"></div>