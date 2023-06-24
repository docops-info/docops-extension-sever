<h1>Release Strategy Builder</h1>

<form class="pure-form pure-form-stacked" data-hx-put="api/release/build" data-hx-target="#response-div">
    <fieldset id="response-div" >
        <legend>Release Strategy</legend>
        <div class="pure-g">
            <div class="pure-u-1-1">
                <div class="pure-u-1">
                    <label for="title">Title</label>
                    <input type="text" id="title" name="title" data-hx-put="api/release/build"
                           value="Release Strategy Builder"  class="pure-u-23-24">
                </div>

                <div class="pure-u-1">
                    <label for="style">Display Style</label>
                    <select id="style" name="style" data-hx-put="api/release/build"
                            class="pure-input-1-2">
                        <option value="TL">Timeline</option>
                        <option value="R">Roadmap</option>
                        <option value="TLG">Timeline Grouped</option>
                    </select>
                </div>
                <div class="pure-u-1">
                    <label for="type_0">Type</label>
                    <select id="type_0" name="type_0" data-hx-put="api/release/build"
                            class="pure-input-1-2">
                        <option value="M1">Milestone 1</option>
                        <option value="RC1">Release Candidate 1</option>
                        <option value="GA">General Availability</option>
                    </select>
                </div>
                <div class="pure-u-1">
                    <label for="date_0">Goal</label>
                    <input type="text" id="goal_0" name="goal_0"
                           value="Our goal is to ..."  class="pure-u-23-24">
                </div>
                <div class="pure-u-1">
                    <label for="date_0">Target Date</label>
                    <input type="text" id="date_0" name="date_0"
                           value="TBD"  class="pure-u-23-24">
                </div>
                <div class="pure-u-1">
                    <label for="line_0">Line</label>
                    <input type="text" id="line_0" name="line_0" data-hx-put="api/release/build"
                           value="Reason for "  class="pure-u-23-24">
                </div>
                <div class="pure-u-1">
                    <label for="logo">Build</label>
                    <div class="pure-g">
                        <button class="button-success pure-button" data-hx-indicator="#spinner" data-hx-target="#response-div">Submit <img class="htmx-indicator" src="images/bars.svg"/></button>
                    </div>
                </div>
            </div>

            <div class="pure-u-1-1">
                <div class="pure-u-1-2">
                    <div id="contentBox"></div>
                    <input class="button-secondary pure-button" type="button" onclick="copyTextToClipboard(badgeSource);"
                           value="Copy Badge As AsciiDoc">
                </div>
            </div>
        </div>
    </fieldset>
</form>