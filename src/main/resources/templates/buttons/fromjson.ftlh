<#-- @ftlvariable name="contentBox" type="java.lang.String" -->
<#-- @ftlvariable name="themeBox" type="java.lang.String" -->
<#-- @ftlvariable name="json" type="java.lang.String" -->

<div>
<h1>Create SVG Buttons from JSON</h1>
<div id="contentBox" ></div>
<div class="divider"></div>
<form class="max-h-full mx-auto" data-hx-put="api/buttons/form" data-hx-target="#contentBox" hx-trigger="submit, load">

    <div class="mb-5">
        <label for="payload" class="mb-2">Insert JSON here</label>
        <textarea id="payload" name="payload" class="block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" rows="20" cols="90" placeholder="" data-hx-target="#contentBox">
        ${json}
        </textarea>
    </div>

<div class="mb-5">
    <button class="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">Submit</button>
</div>
    <div class="mb-5">
        <fieldset>
            <legend>List of Predefined Themes</legend>
                <label for="theme" class="mb-2">Theme</label>
                <select name="theme" id="theme"
                        class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                        data-hx-put="api/buttons/form" data-hx-target="#contentBox">
                    <option value="">Select</option>
                    <#list themes as themeItem>
                        <option value="${themeItem}">${themeItem}</option>
                    </#list>
                </select>
        </fieldset>
    </div>
</form>

</div>
<div>
    <pre>
        <code id="themeBox"></code>
    </pre>
</div>
<script>
    document.querySelectorAll('pre code').forEach((el) => {
        hljs.highlightElement(el);
    });
</script>

