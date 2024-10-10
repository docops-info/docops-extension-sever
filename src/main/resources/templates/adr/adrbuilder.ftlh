<H1>ADR Builder</H1>
<div>
    <article class="flex max-w-xl flex-col items-start justify-between">
        <p class="mt-5 text-sm leading-6 text-gray-600">The ADR builder is a view to help users of AsciiDoctorJ
        learn the syntax of building a macro inside the AsciiDoctor Document. This form is intended for use by developers.
        Developers can leverage the form to fill out the ADR and the output will be reflected in the ADR builder as well
            as the custom DSL syntax
        </p>
    </article>
</div>
<div id="contentBox" class="contentBox"></div>
<div class="divider"></div>

<form class="max-w-sm mx-auto" data-hx-put="api/adr" data-hx-target="#contentBox" hx-trigger="submit, load">
                <div class="mb-5">
                    <label for="title" class="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Title</label>
                    <input type="text" id="title" name="title" data-hx-put="api/adr" data-hx-target="#contentBox"
                           value="Implement as shell scripts"
                           class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                </div>
                <div class="flex flex-col">
                    <label for="status" class="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Status</label>
                    <select id="status" name="status" data-hx-put="api/adr" data-hx-target="#contentBox"
                            class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                        <option value="Proposed">Proposed</option>
                        <option value="Accepted">Accepted</option>
                        <option value="Superseded">Superseded</option>
                        <option value="Deprecated">Deprecated</option>
                        <option value="Rejected">Rejected</option>
                    </select>
                </div>
                <div class="flex flex-col">
                    <label for="date" class="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Date</label>
                    <input type="text" id="date" name="date" data-hx-put="api/adr" data-hx-target="#contentBox"
                           value="" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                </div>
        <div class="mb-5"><label for="participants" class="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Participants(comma separated)</label>
            <input type="text" id="participants" name="participants" data-hx-put="api/adr" data-hx-target="#contentBox"
                   value="Author1 John,Author2 Doe" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
        </div>

        <div class="mb-5"><label for="context" class="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Context</label>
            <textarea id="context" name="context" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" cols="90" placeholder=""
                      data-hx-put="api/adr" data-hx-target="#contentBox">ADRs are plain text files stored in a subdirectory of the project.</textarea>
        </div>

        <div class="mb-5"><label for="decision" class="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Decision</label>
            <textarea id="decision" name="decision" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" cols="90" placeholder=""
                      data-hx-put="api/adr" data-hx-target="#contentBox">The tool is implemented as shell scripts that use standard Unix tools -- grep, sed, awk, etc.</textarea></div>
        <div class="mb-5">
            <label for="consequences" class="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Consequences</label>
            <textarea id="consequences" name="consequences" cols="90" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" placeholder=""
                      data-hx-put="api/adr" data-hx-target="#contentBox">The tool won't support Windows. Being plain text files, ADRs can be created by hand and edited in any text editor.
            This tool just makes the process more convenient. Development will have to cope with differences between Unix variants, particularly Linux
            and MacOS X.</textarea>
        </div>


    <div class="flex flex-col">
        <button class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
    </div>
    <div class="flex flex-col">
        <input class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4" type="button" onclick="copyTextToClipboard(adrSource);"
               value="Copy Source As AsciiDoc">
    </div>


</form>
