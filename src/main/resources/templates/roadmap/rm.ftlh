<h1>Planner Visualizer</h1>
<div>
    <article class="flex max-w-xl flex-col items-start justify-between">
        <p class="mt-5 text-sm leading-6 text-gray-600">This is a view to allow developers to learn
            the custom DSL for creating the Planner Visualizer. The data can be updated in the text box and the visualizer is updated preceding it.
        </p>
    </article>
</div>
<div id="contentBox" class="contentBox"></div>
<div class="divider"></div>

<form class="max-h-full mx-auto" data-hx-put="api/roadmap/" data-hx-target="#contentBox" hx-trigger="submit, load">
        <div class="mb-5">
            <label for="title" class="mb-2">Title</label>
            <input type="text" id="title" name="title" data-hx-put="api/roadmap/" data-hx-target="#contentBox"
                   value="Planner Visualizer" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">
        </div>

        <div class="mb-5">
            <label class="mb-2">DocOps Ascii Macro [docops, roadmap,title="Steve's Work", useDark=false, role=left]</label>
        </div>

    <div class="flex flex-col col-span-2">
        <label for="content" class="mb-2">Simple DSL</label>
            <textarea class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" id="content" name="content" rows="20" cols="90" placeholder="" data-hx-put="api/roadmap/" data-hx-target="#contentBox">
- now Audit
Audit Log Streaming
- now Repo
Fork the repository to Support Search
- next Kotlin
Kotlin Coroutines support
- later API
API versioning (GA)
- done Schema
JSON schema definition
- later Vulnerability
code scanning and app scanning
            </textarea>
        </div>
    <div class="mb-5">
        <button class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
    </div>

</form>


