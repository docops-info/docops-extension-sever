<h1>Release Strategies</h1>

<div role="tablist" class="tabs tabs-boxed">
    <a role="tab" class="tab btnMenu strat" hx-get="strat.html" data-hx-target="#releaseTarget"><i class="fad fa-link text-xs mr-2"></i> Builder</a>
    <a role="tab" class="tab btnMenu fromJson" hx-get="fromJson.html" data-hx-target="#releaseTarget"><i class="fad fa-table text-xs mr-2"></i> From JSON</a>
    <a role="tab" class="tab btnMenu release-strategy-schema" hx-get="json/schema?name=release-strategy-schema" data-hx-target="#releaseTarget"><i class="fad fa-link text-xs mr-2"></i> JSON Schema</a>

</div>

<div id="releaseTarget">
    <div id="contentBox">
        <h1>Load Builder from JSON</h1>

        <form data-hx-put="api/release/prefill" data-hx-target="#contentBox" hx-trigger="submit, load">

            <fieldset id="response-div" >
                <legend>JSON</legend>
                <div class="join">
                    <label for="payload" class="btn join-item">Insert JSON here</label>
                    <textarea id="payload" name="payload" class="join-item textarea textarea-primary" rows="20" cols="90" placeholder="" data-hx-put="api/release/prefill" data-hx-target="#contentBox">
{
    "title": "Release Strategy Builder",
    "releases": [
        {
            "type": "M1",
            "lines": [
                "Reason for "
            ],
            "date": "TBD",
            "selected": true,
            "goal": "Our goal is to build a better cog..."
        }
    ],
    "style": "TLS",
    "displayConfig": {}
}

                    </textarea>
                </div>
            </fieldset>
            <fieldset>
                <button class="btn btn-primary">Submit</button>
            </fieldset>
        </form>
    </div>
</div>