<#-- @ftlvariable name="contentBox" type="java.lang.String" -->
<#-- @ftlvariable name="themeBox" type="java.lang.String" -->
<#-- @ftlvariable name="json" type="java.lang.String" -->

<div>
    <h1>Create SVG Buttons from JSON</h1>

    <form class="grid grid-cols-2 gap-4" data-hx-put="api/panel/convert/" data-hx-target="#contentBox" >

            <div class="flex flex-col col-span-2">
                <label for="payload" class="mb-2">Insert Panel String</label>
        <textarea id="payload" name="payload"  class="textarea textarea-primary" rows="20" cols="90" placeholder="" data-hx-target="#contentBox">
        </textarea>
            </div>

        <div class="flex flex-col col-span-2">
            <button class="py-2 px-4 bg-blue-500 text-white rounded-md mt-4">Submit</button>
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
<div class="divider"></div>
<div id="contentBox" class="contentBox"></div>
