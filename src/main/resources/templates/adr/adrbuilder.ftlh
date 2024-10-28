<H1>ADR Builder</H1>
<div>
    <section class="bg-white dark:bg-gray-900">
        <div class="py-8 px-4 mx-auto max-w-screen-xl text-center lg:py-16">
            <h1 class="mb-4 text-4xl font-extrabold tracking-tight leading-none text-gray-900 md:text-5xl lg:text-6xl dark:text-white">ADR builder UI</h1>
            <p class="mb-8 text-lg font-normal text-gray-500 lg:text-xl sm:px-16 lg:px-48 dark:text-gray-400">
                The ADR builder provides this visual interface that
                guides users through the process of constructing the AsciiDoctor macro.
                By allowing developers to fill out the ADR form, the tool demonstrates how the input
                into the proper ADR builder syntax and custom DSL (Domain-Specific Language) syntax,
                thereby teaching them the correct macro syntax within the AsciiDoctor context.
            </p>

        </div>
    </section>
</div>
<div id="contentBox" class="contentBox"></div>
<div class="divider"></div>

<form class="max-h-full mx-auto" data-hx-put="api/adr" data-hx-target="#contentBox" hx-trigger="submit, load">
                <div class="mb-5">
                    <label for="title" class="mb-2">Title</label>
                    <input type="text" id="title" name="title" data-hx-put="api/adr" data-hx-target="#contentBox"
                           value="Implement as shell scripts"
                           class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                </div>
                <div class="mb-5">
                    <label for="status" class="mb-2">Status</label>
                    <select id="status" name="status" data-hx-put="api/adr" data-hx-target="#contentBox"
                            class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                        <option value="Proposed">Proposed</option>
                        <option value="Accepted">Accepted</option>
                        <option value="Superseded">Superseded</option>
                        <option value="Deprecated">Deprecated</option>
                        <option value="Rejected">Rejected</option>
                    </select>
                </div>
                <div class="mb-5">
                    <label for="date" class="mb-2">Date</label>
                    <input type="text" id="date" name="date" data-hx-put="api/adr" data-hx-target="#contentBox"
                           value="" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
                </div>
        <div class="mb-5"><label for="participants" class="mb-2">Participants(comma separated)</label>
            <input type="text" id="participants" name="participants" data-hx-put="api/adr" data-hx-target="#contentBox"
                   value="Author1 John,Author2 Doe" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
        </div>

        <div class="mb-5"><label for="context" class="mb-2">Context</label>
            <textarea id="context" name="context" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" cols="90" placeholder=""
                      data-hx-put="api/adr" data-hx-target="#contentBox">ADRs are plain text files stored in a subdirectory of the project.</textarea>
        </div>

        <div class="mb-5"><label for="decision" class="mb-2">Decision</label>
            <textarea id="decision" name="decision" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" cols="90" placeholder=""
                      data-hx-put="api/adr" data-hx-target="#contentBox">The tool is implemented as shell scripts that use standard Unix tools -- grep, sed, awk, etc.</textarea></div>
        <div class="mb-5">
            <label for="consequences" class="mb-2">Consequences</label>
            <textarea id="consequences" name="consequences" cols="90" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" placeholder=""
                      data-hx-put="api/adr" data-hx-target="#contentBox">The tool won't support Windows. Being plain text files, ADRs can be created by hand and edited in any text editor.
            This tool just makes the process more convenient. Development will have to cope with differences between Unix variants, particularly Linux
            and MacOS X.</textarea>
        </div>


    <div class="mb-5">
        <button class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
    </div>
    <div class="mb-5">
        <input class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" type="button" onclick="copyTextToClipboard(adrSource);"
               value="Copy Source As AsciiDoc">
    </div>


</form>
