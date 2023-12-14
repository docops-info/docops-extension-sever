<#-- @ftlvariable name="contentBox" type="java.lang.String" -->
<#-- @ftlvariable name="themeBox" type="java.lang.String" -->
<#-- @ftlvariable name="json" type="java.lang.String" -->

<div>
<h1>Create SVG Buttons from JSON</h1>
<div id="contentBox" ></div>
<div class="divider"></div>
<form class="grid grid-cols-2 gap-4" data-hx-put="api/buttons/form" data-hx-target="#contentBox" hx-trigger="submit, load">

    <div class="flex flex-col col-span-2">
        <label for="payload" class="mb-2">Insert JSON here</label>
        <textarea id="payload" name="payload"  class="textarea textarea-primary" rows="20" cols="90" placeholder="" data-hx-target="#contentBox">
        ${json}
        </textarea>
    </div>

<div class="flex flex-col col-span-2">
    <button class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4">Submit</button>
</div>
<fieldset>
    <legend>List of Predefined Themes</legend>
    <div class="flex flex-col col-span-2">
        <label for="theme" class="mb-2">Theme</label>
        <select name="theme" id="theme" class="select select-primary w-full max-w-xs" data-hx-put="api/buttons/form" data-hx-target="#contentBox">
            <option value="">Select</option>
            <#list themes as themeItem>
                <option value="${themeItem}">${themeItem}</option>
            </#list>
        </select>
    </div>
</fieldset>
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

