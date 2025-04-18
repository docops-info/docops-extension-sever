
                        <h2 class="text-2xl font-bold mb-6 text-center text-gray-800">Create Badge</h2>

                        
                        <form hx-put="api/badge/item"
                              data-hx-target="#contentBox" data-hx-ext="json-enc" hx-trigger="submit, load"
                              class="space-y-6">

                            <!-- Label -->
                            <div>
                                <label for="label" class="block text-sm font-medium text-gray-700">Label</label>
                                <input type="text"
                                       id="label"
                                       name="label"
                                       required
                                       class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"/>
                            </div>

                            <!-- Message -->
                            <div>
                                <label for="message" class="text-gray-600">Message</label>
                                <input type="text"
                                       id="message"
                                       name="message"
                                       required
                                       class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"/>
                            </div>

                            <!-- URL -->
                            <div>
                                <label for="url" class="text-gray-600">URL</label>
                                <input type="url"
                                       id="url"
                                       name="url"
                                       class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"/>
                            </div>

                            <!-- Label Color -->
                            <div>
                                <label for="labelColor" class="text-gray-600">Label Color</label>
                                <div class="flex items-center">
                                    <input type="color"
                                           id="labelColor"
                                           name="labelColor"
                                           class="h-10 w-20"/>
                                    <input type="text"
                                           id="labelColorText"
                                           class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                                           placeholder="#000000"/>
                                </div>
                            </div>

                            <!-- Message Color -->
                            <div>
                                <label for="messageColor" class="text-gray-600">Message Color</label>
                                <div class="flex items-center">
                                    <input type="color"
                                           id="messageColor"
                                           name="messageColor"
                                           class="h-10 w-20"/>
                                    <input type="text"
                                           id="messageColorText"
                                           class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                                           placeholder="#000000"/>
                                </div>
                            </div>

                            <!-- Logo URL -->
                            <div>
                                <label for="logo" class="text-gray-600">Logo URL</label>
                                <input type="text"
                                       id="logo"
                                       name="logo"
                                       class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"/>
                            </div>

                            <!-- Font Color -->
                            <div>
                                <label for="fontColor" class="text-gray-600">Font Color</label>
                                <div class="flex items-center">
                                    <input type="color"
                                           id="fontColor"
                                           name="fontColor"
                                           value="#fcfcfc"
                                           class="h-10 w-20"/>
                                    <input type="text"
                                           id="fontColorText"
                                           value="#fcfcfc"
                                           class="ml-2 flex-1 rounded-md border-gray-300 shadow-sm focus:border-indigo-300 focus:ring focus:ring-indigo-200 focus:ring-opacity-50"/>
                                </div>
                            </div>

                            <!-- Submit Button -->
                            <div class="pt-4">
                                <button type="submit"
                                        class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-black bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
                                    Create Badge
                                </button>
                            </div>
                        </form>

    <div id="contentBox" class="mt-6"></div>




<script>
    // Sync color inputs with text inputs
    document.querySelectorAll('input[type="color"]').forEach(colorInput => {
        const textInput = document.getElementById(colorInput.id + 'Text');

        colorInput.addEventListener('input', (e) => {
            textInput.value = e.target.value;
        });

        textInput.addEventListener('input', (e) => {
            colorInput.value = e.target.value;
        });
    });
</script>