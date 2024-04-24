<div id="response-div">
<h1>Release Strategy Builder</h1>

<form class="grid grid-cols-2 gap-4" data-hx-put="api/release/build" data-hx-target="#response-div" data-hx-swap="innerHtml show:top">
                <div class="flex flex-col col-span-2">
                    <label for="title" class="mb-2">Title</label>
                    <input type="text" id="title" name="title" data-hx-put="api/release/build"
                           value="Release Strategy Builder"  class="p-2 border rounded-md">
                </div>

                <div class="flex flex-col">
                    <label for="style" class="mb-2">Display Style</label>
                    <select id="style" name="style" data-hx-put="api/release/build"
                            class="select select-info">
                        <option value="TLS">Timeline Summary</option>
                        <option value="R">Roadmap</option>
                        <option value="TLG">Timeline Grouped</option>
                    </select>
                </div>
                <div class="flex flex-col">
                    <label for="type_0" class="mb-2">Type</label>
                    <select id="type_0" name="type_0" data-hx-put="api/release/build"
                            class="select select-info">
                        <option value="M1">Milestone 1</option>
                        <option value="RC1">Release Candidate 1</option>
                        <option value="GA">General Availability</option>
                    </select>
                </div>
                <div class="flex flex-col col-span-2">
                    <label for="goal_0" class="mb-2">Goal</label>
                    <input type="text" id="goal_0" name="goal_0"
                           value="Our goal is to build a better cog..."  class="p-2 border rounded-md">
                </div>
                <div class="flex flex-col  col-span-2">
                    <label for="date_0" class="mb-2">Target Date</label>
                    <input type="text" id="date_0" name="date_0"
                           value="TBD"  class="p-2 border rounded-md">
                </div>
                <div class="flex flex-col  col-span-2">
                    <label for="line_0" class="mb-2">Line</label>
                    <input type="text" id="line_0" name="line_0" data-hx-put="api/release/build"
                           value="Reason for "  class="p-2 border rounded-md">
                </div>
                <div class="flex flex-col  col-span-2 divider"></div>
                <div class="flex flex-col">
                    <button class="btn btn-primary" data-hx-indicator="#spinner" data-hx-target="#response-div">Submit <img class="htmx-indicator" src="images/bars.svg"/></button>
                </div>
                <div>
                    <div id="contentBox"></div>
                    <input class="btn btn-info" type="button" onclick="copyTextToClipboard(badgeSource);"
                           value="Copy Badge As AsciiDoc">
                </div>
</form>
</div>
<#include "stratcommon.ftlh">
<@stratMacro builder = "btn btn-primary" fromJson= ""/>
