<#-- @ftlvariable name="styles" type="java.util.HashMap" -->
<#-- @ftlvariable name="svg" type="java.lang.String" -->
<#-- @ftlvariable name="releaseStrategy" type="io.docops.docopsextensionssupport.releasestrategy.ReleaseStrategy" -->

<fieldset id="response-div" >
    <legend>Release Strategy</legend>
    <div class="pure-g">
        <div class="pure-u-1-2">
            <div class="pure-u-1">
                <label for="title">Title</label>
                <input type="text" id="title" name="title" data-hx-put="api/release/build"
                       value="${releaseStrategy.title}"  class="pure-u-23-24">
            </div>
            <div class="pure-u-1">
                <label for="style">Display Style</label>
                <select id="style" name="style" data-hx-put="api/release/build"
                        class="pure-input-1-2">
                    <#list styles?keys as style>
                        <#if releaseStrategy.style == style>
                        <option value="${style}" selected>${styles[style]}</option>
                        <#else>
                            <option value="${style}">${styles[style]}</option>
                        </#if>
                    </#list>
                </select>
            </div>
            <#list releaseStrategy.releases as release>
                <div class="pure-u-1">
                    <label for="type_${release_index}">Type</label>
                    <select id="type_${release_index}" name="type_${release_index}" data-hx-put="api/release/build"
                            class="pure-input-1-2">
                        <#list releaseTypes as rel>
                            <#if rel == release.type>
                                <option value="${rel}" selected>${rel}</option>
                            <#else>
                                <option value="${rel}">${rel}</option>
                            </#if>
                        </#list>
                    </select>
                </div>
                <div class="pure-u-1">
                    <label for="date_${release_index}">Goal</label>
                    <input type="text" id="goal_${release_index}" name="goal_${release_index}"
                           value="${release.goal}"  class="pure-u-23-24">
                </div>
                <div class="pure-u-1">
                    <label for="date_${release_index}">Target Date</label>
                    <input type="text" id="date_${release_index}" name="date_${release_index}"
                           value="${release.date}"  class="pure-u-23-24">
                </div>
                <div class="pure-u-1">
                    <label for="line_${release_index}">Line</label>
                    <#list release.lines as line>
                        <input type="text" id="line_${release_index}" name="line_${release_index}"
                               value="${line}"  class="pure-u-23-24">
                    </#list>
                    <button type="submit" name="addLine" value="line_${release_index}">+ New Line</button>
                </div>
            </#list>
            <div class="pure-u-1">
                <div>
                    <button class="button-success pure-button" type="submit" name="addType" value="increase">+ New Milestone</button>
                </div>
                <label for="logo">Build</label>
                <div class="pure-g">
                    <button class="button-success pure-button" data-hx-indicator="#spinner" data-hx-target="#response-div">Submit <img class="htmx-indicator" src="images/bars.svg"/></button>
                    <button class="button-secondary pure-button" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVGAsText('${bsvg}')">Download <img class="htmx-indicator" src="images/bars.svg"/></button>
                </div>
            </div>

        </div>
        <div class="pure-u-1-1">
            <hr/>
        </div>
        <div class="pure-u-1-1">
            <#noautoesc>
                <div id="contentBox"> ${svg}</div>
            </#noautoesc>
        </div>
        <div class="pure-u-1-1">
            <div class="pure-u-1-2">
                    <pre>
                        <code>
                        <#noautoesc>
                            ${sourceJson}
                        </#noautoesc>
                        </code>
                    </pre>

            </div>
        </div>
    </div>
</fieldset>
<script>
    document.querySelectorAll('pre code').forEach((el) => {
        hljs.highlightElement(el);
    });
</script>