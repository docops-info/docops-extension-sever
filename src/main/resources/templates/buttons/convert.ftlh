<#-- @ftlvariable name="contentBox" type="java.lang.String" -->
<#-- @ftlvariable name="themeBox" type="java.lang.String" -->
<#-- @ftlvariable name="json" type="java.lang.String" -->

<div>
    <h1>Create SVG Buttons from JSON</h1>

    <form data-hx-put="api/panel/convert/" data-hx-target="#contentBox" >

        <fieldset id="response-div" >
            <legend>User created Panels</legend>
            <div class="join">
                <label for="payload" class="btn join-item">Insert Panel String</label>
        <textarea id="payload" name="payload"  class="join-item textarea textarea-primary" rows="20" cols="90" placeholder="" data-hx-target="#contentBox">
        </textarea>
            </div>

        </fieldset>
        <fieldset>
            <button class="btn btn-primary">Submit</button>
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
<div class="divider"></div>
<div id="contentBox" class="contentBox"></div>
