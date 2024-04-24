<H1>ADR Builder</H1>

<div id="contentBox" class="contentBox"></div>
<div class="divider"></div>

<form class="grid grid-cols-2 gap-4" data-hx-put="api/adr" data-hx-target="#contentBox" hx-trigger="submit, load" data-hx-swap="innerHtml show:top">
                <div class="flex flex-col col-span-2">
                    <label for="title" class="mb-2">Title</label>
                    <input type="text" id="title" name="title" data-hx-put="api/adr" data-hx-target="#contentBox"
                           value="Implement as shell scripts"
                           class="p-2 border rounded-md">
                </div>
                <div class="flex flex-col">
                    <label for="status" class="mb-2">Status</label>
                    <select id="status" name="status" data-hx-put="api/adr" data-hx-target="#contentBox"
                            class="select select-info">
                        <option value="Proposed">Proposed</option>
                        <option value="Accepted">Accepted</option>
                        <option value="Superseded">Superseded</option>
                        <option value="Deprecated">Deprecated</option>
                        <option value="Rejected">Rejected</option>
                    </select>
                </div>
                <div class="flex flex-col">
                    <label for="date" class="mb-2">Date</label>
                    <input type="text" id="date" name="date" data-hx-put="api/adr" data-hx-target="#contentBox"
                           value="" class="p-2 border rounded-md">
                </div>
        <div class="flex flex-col col-span-2"><label for="participants" class="mb-2">Participants(comma separated)</label>
            <input type="text" id="participants" name="participants" data-hx-put="api/adr" data-hx-target="#contentBox"
                   value="Author1 John,Author2 Doe" class="p-2 border rounded-md">
        </div>

        <div class="flex flex-col col-span-2"><label for="context" class="mb-2">Context</label>
            <textarea id="context" name="context" class="textarea textarea-primary" cols="90" placeholder=""
                      data-hx-put="api/adr" data-hx-target="#contentBox">ADRs are plain text files stored in a subdirectory of the project.</textarea>
        </div>

        <div class="flex flex-col col-span-2"><label for="decision" class="mb-2">Decision</label>
            <textarea id="decision" name="decision" class="textarea textarea-primary" cols="90" placeholder=""
                      data-hx-put="api/adr" data-hx-target="#contentBox">The tool is implemented as shell scripts that use standard Unix tools -- grep, sed, awk, etc.</textarea></div>
        <div class="flex flex-col col-span-2">
            <label for="consequences" class="mb-2">Consequences</label>
            <textarea id="consequences" name="consequences" cols="90" class="textarea textarea-primary" placeholder=""
                      data-hx-put="api/adr" data-hx-target="#contentBox">he tool won't support Windows. Being plain text files, ADRs can be created by hand and edited in any text editor.
            This tool just makes the process more convenient.

Development will have to cope with differences between Unix variants, particularly Linux
            and MacOS X.</textarea>
        </div>


    <div class="flex flex-col">
        <button class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4">Submit</button>
    </div>
    <div class="flex flex-col">
        <input class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4" type="button" onclick="copyTextToClipboard(adrSource);"
               value="Copy Source As AsciiDoc">
    </div>


</form>
