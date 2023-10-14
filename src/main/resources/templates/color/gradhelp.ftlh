<div>
    <h1>Create Scorecard from JSON</h1>

    <form class="pure-form pure-form-stacked" data-hx-put="api/grad" data-hx-target="#contentBox">

        <fieldset id="response-div" >
            <legend>Color</legend>
            <div class="pure-1-2">
                <label for="payload">Insert Color Here</label>
                <input type="text" id="gradColor" name="gradColor"
                       value="#" class="join-item input input-bordered input-primary w-full max-w-xs">

            </div>
        </fieldset>
        <fieldset>
            <button class="btn btn-primary" data-hx-indicator="#spinner">Submit</button>
        </fieldset>
    </form>
</div>

<div class="divider"></div>
<div id="contentBox" class="contentBox"></div>