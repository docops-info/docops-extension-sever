<div class="max-w-2xl mx-auto bg-white rounded-xl shadow-md p-8">
    <h1 class="text-2xl font-bold mb-6 text-gray-800">Create Architecture Decision Record</h1>
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
    <form hx-put="api/adr"
          hx-target="#result"
          class="space-y-6">

        <!-- Title and Date Row -->
        <div class="grid grid-cols-2 gap-4">
            <div>
                <label for="title" class="block text-sm font-medium text-gray-700">Title</label>
                <input type="text"
                       id="title"
                       name="title"
                       required
                       class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500">
            </div>
            <div>
                <label for="date" class="block text-sm font-medium text-gray-700">Date</label>
                <input type="date"
                       id="date"
                       name="date"
                       required
                       class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500">
            </div>
        </div>

        <!-- Status -->
        <div>
            <label for="status" class="block text-sm font-medium text-gray-700">Status</label>
            <select id="status"
                    name="status"
                    required
                    class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500">
                <option value="Proposed">Proposed</option>
                <option value="Accepted">Accepted</option>
                <option value="Superseded">Superseded</option>
                <option value="Deprecated">Deprecated</option>
                <option value="Rejected">Rejected</option>
            </select>
        </div>

        <!-- Context -->
        <div>
            <label for="context" class="block text-sm font-medium text-gray-700">Context</label>
            <textarea id="context"
                      name="context"
                      rows="3"
                      placeholder="Enter each context item on a new line"
                      class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"></textarea>
        </div>

        <!-- Decision -->
        <div>
            <label for="decision" class="block text-sm font-medium text-gray-700">Decision</label>
            <textarea id="decision"
                      name="decision"
                      rows="3"
                      placeholder="Enter each decision point on a new line"
                      class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"></textarea>
        </div>

        <!-- Consequences -->
        <div>
            <label for="consequences" class="block text-sm font-medium text-gray-700">Consequences</label>
            <textarea id="consequences"
                      name="consequences"
                      rows="3"
                      placeholder="Enter each consequence on a new line"
                      class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"></textarea>
        </div>

        <!-- Participants -->
        <div>
            <label for="participants" class="block text-sm font-medium text-gray-700">Participants</label>
            <textarea id="participants"
                      name="participants"
                      rows="2"
                      placeholder="Enter each participant on a new line"
                      class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"></textarea>
        </div>

        <!-- Submit Button -->
        <div class="flex justify-end">
            <button type="submit"
                    class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700  font-bold py-2 px-4 rounded-md transition duration-150 ease-in-out">
                Create ADR
            </button>
        </div>
    </form>

    <!-- Result Display -->
    <div id="result" class="mt-6"></div>

    <div class="mb-5">
        <input class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" type="button" onclick="copyTextToClipboard(adrSource);"
               value="Copy Source As AsciiDoc">
    </div>
</div>
