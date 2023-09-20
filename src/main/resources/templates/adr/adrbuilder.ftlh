<H1>ADR Builder</H1>
<form class="pure-form pure-form-stacked" data-hx-put="api/adr" data-hx-target="#contentBox">
    <fieldset>
        <legend>Architecture Decision Record</legend>
        <div>
            <div>
                <div class="join">
                    <label for="title" class="btn join-item">Title</label>
                    <input type="text" id="title" name="title" data-hx-put="api/adr" data-hx-target="#contentBox"
                           value="Implement as shell scripts"
                           class="join-item input input-bordered input-primary w-full max-w-xs">
                </div>
            </div>
            <div>
                <div class="join">
                    <label for="status" class="btn join-item">Status</label>
                    <select id="status" name="status" data-hx-put="api/adr" data-hx-target="#contentBox"
                            class="join-item select select-primary w-full max-w-xs">
                        <option value="Proposed">Proposed</option>
                        <option value="Accepted">Accepted</option>
                        <option value="Superseded">Superseded</option>
                        <option value="Deprecated">Deprecated</option>
                        <option value="Rejected">Rejected</option>
                    </select>
                </div>
            </div>
            <div>
                <div class="join">
                    <label for="date" class="btn join-item">Date</label>
                    <input type="text" id="date" name="date" data-hx-put="api/adr" data-hx-target="#contentBox"
                           value="" class="join-item input input-bordered input-primary w-full max-w-xs">
                </div>
            </div>
        </div>
    </fieldset>
    <fieldset class="pure-group">
        <div class="join"><label for="participants" class="btn join-item">Participants(comma separated)</label>
            <input type="text" id="participants" name="participants" data-hx-put="api/adr" data-hx-target="#contentBox"
                   value="Author1 John,Author2 Doe" class="join-item input input-bordered input-primary w-full max-w-xs">
        </div>

        <div class="join"><label for="context" class="btn join-item">Context</label>
            <textarea id="context" name="context" class="join-item textarea textarea-primary" cols="90" placeholder=""
                      data-hx-put="api/adr" data-hx-target="#contentBox">ADRs are plain text files stored in a subdirectory of the project.</textarea>
        </div>

        <div class="join"><label for="decision" class="btn join-item">Decision</label>
            <textarea id="decision" name="decision" class="join-item textarea textarea-primary" cols="90" placeholder=""
                      data-hx-put="api/adr" data-hx-target="#contentBox">The tool is implemented as shell scripts that use standard Unix tools -- grep, sed, awk, etc.</textarea></div>
        <div class="join">
            <label for="consequences" class="btn join-item">Consequences</label>
            <textarea id="consequences" name="consequences" cols="90" class="join-item textarea textarea-primary" placeholder=""
                      data-hx-put="api/adr" data-hx-target="#contentBox">he tool won't support Windows. Being plain text files, ADRs can be created by hand and edited in any text editor.
            This tool just makes the process more convenient.

Development will have to cope with differences between Unix variants, particularly Linux
            and MacOS X.</textarea></div>

    </fieldset>

    <fieldset>
        <button class="btn btn-primary">Submit</button>
        <input class="btn btn-secondary" type="button" onclick="copyTextToClipboard(adrSource);"
               value="Copy Panel As AsciiDoc">

    </fieldset>
</form>
<div class="divider"></div>
<div id="contentBox" class="contentBox"></div>