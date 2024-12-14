<div>
    <h1>Color Gradient Utility</h1>

    <form class="max-h-full mx-auto" data-hx-put="api/grad" data-hx-target="#contentBox">

        <fieldset id="response-div" >
            <legend>Color</legend>
            <div class="mb-5">
                <label for="payload" class="mb-2">Insert Color Here</label>
                <input type="text" id="gradColor" name="gradColor"
                       value="#" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500">

            </div>
        </fieldset>
        <fieldset>
            <button class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" data-hx-indicator="#spinner">Submit</button>
        </fieldset>
    </form>
</div>

<button class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" data-hx-indicator="#spinner" data-hx-get="api/grad" data-hx-target="#contentBox">Colors</button>

<div class="divider"></div>
<div id="contentBox" class="contentBox"></div>