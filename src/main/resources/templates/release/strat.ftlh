<div id="response-div">
<h1>Release Strategy Builder</h1>

<form data-hx-put="api/release/build" data-hx-target="#response-div">
    <fieldset id="response-div" >
        <legend>Release Strategy</legend>
        <div>
            <div>
                <div class="join">
                    <label for="title" class="btn join-item">Title</label>
                    <input type="text" id="title" name="title" data-hx-put="api/release/build"
                           value="Release Strategy Builder"  class="join-item input input-bordered input-primary w-full max-w-xs">
                </div>

            </div>
            <div>
                <div class="join">
                    <label for="style" class="btn join-item">Display Style</label>
                    <select id="style" name="style" data-hx-put="api/release/build"
                            class="join-item select select-primary w-full max-w-xs">
                        <option value="TLS">Timeline Summary</option>
                        <option value="R">Roadmap</option>
                        <option value="TLG">Timeline Grouped</option>
                    </select>
                </div>
            </div>
            <div>
                <div class="join">
                    <label for="type_0" class="btn join-item">Type</label>
                    <select id="type_0" name="type_0" data-hx-put="api/release/build"
                            class="join-item select select-primary w-full max-w-xs">
                        <option value="M1">Milestone 1</option>
                        <option value="RC1">Release Candidate 1</option>
                        <option value="GA">General Availability</option>
                    </select>
                </div>
            </div>
            <div>
                <div class="join">
                    <label for="date_0" class="btn join-item">Goal</label>
                    <input type="text" id="goal_0" name="goal_0"
                           value="Our goal is to ..."  class="join-item input input-bordered input-primary w-full max-w-xs">
                </div>
            </div>
            <div>
                <div class="join">
                    <label for="date_0" class="btn join-item">Target Date</label>
                    <input type="text" id="date_0" name="date_0"
                           value="TBD"  class="join-item input input-bordered input-primary w-full max-w-xs">
                </div>
            </div>
            <div>
                <div class="join">
                    <label for="line_0" class="btn join-item">Line</label>
                    <input type="text" id="line_0" name="line_0" data-hx-put="api/release/build"
                           value="Reason for "  class="join-item input input-bordered input-primary w-full max-w-xs">
                </div>
            </div>
            <div>
                <div class="divider"></div>
                <div>
                    <div>
                        <button class="btn btn-primary" data-hx-indicator="#spinner" data-hx-target="#response-div">Submit <img class="htmx-indicator" src="images/bars.svg"/></button>
                    </div>
                </div>
            </div>

            <div>
                <div>
                    <div id="contentBox"></div>
                    <input class="btn btn-secondary" type="button" onclick="copyTextToClipboard(badgeSource);"
                           value="Copy Badge As AsciiDoc">
                </div>
            </div>
        </div>
    </fieldset>
</form>
</div>
<#include "stratcommon.ftlh">
<@stratMacro builder = "btn btn-primary" fromJson= ""/>
