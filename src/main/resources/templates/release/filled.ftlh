<#-- @ftlvariable name="getUrl" type="java.lang.String" -->
<#-- @ftlvariable name="styles" type="java.util.HashMap" -->
<#-- @ftlvariable name="svg" type="java.lang.String" -->
<#-- @ftlvariable name="releaseStrategy" type="io.docops.docopsextensionssupport.releasestrategy.ReleaseStrategy" -->
<h1>Release Strategy Builder</h1>

<form class="pure-form pure-form-stacked" data-hx-put="api/release/build" data-hx-target="#response-div">

<fieldset id="response-div" >
    <legend>Release Strategy</legend>
    <div class="pure-g">
        <div class="pure-u-1-1">
            <div class="pure-u-1">
                <label for="title">Title</label>
                <input type="text" id="title" name="title" data-hx-put="api/release/build"
                       value="${releaseStrategy.title}"  class="select select-primary w-full max-w-xs">
            </div>
            <div class="pure-u-1">
                <label for="style">Display Style</label>
                <select id="style" name="style" data-hx-put="api/release/build"
                        class="select select-primary w-full max-w-xs">
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
                            class="select select-primary w-full max-w-xs">
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
                           value="${release.goal}"  class="select select-primary w-full max-w-xs">
                </div>
                <div class="pure-u-1-2">
                    <label for="date_${release_index}">Target Date</label>
                    <input type="text" id="date_${release_index}" name="date_${release_index}"
                           value="${release.date}"  class="select select-primary w-full max-w-xs">
                </div>
                <div class="pure-g">
                <div class="pure-u-1-2">
                    <#list release.lines as line>
                        <input type="text" id="line_${release_index}" name="line_${release_index}"
                               value="${line}"  class="select select-primary w-full max-w-xs">
                    </#list>
                </div>
                <div class="pure-u-1-2">
                    <button class="btn btn-outline" type="submit" name="addLine" value="line_${release_index}">+ New Line</button>
                </div>
                </div>
            </#list>
            <div class="pure-u-1">
                <div><br/>
                    <button class="btn btn-outline" type="submit" name="addType" value="increase">+ New Milestone</button>
                </div>
                <label for="logo">Build</label>
                <div class="pure-g">
                    <button class="btn btn-success" data-hx-indicator="#spinner" data-hx-target="#response-div">Submit <img class="htmx-indicator" src="images/bars.svg"/></button>
                    <button class="btn btn-secondary" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVGAsText('${bsvg}')">Download <img class="htmx-indicator" src="images/bars.svg"/></button>
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
            <div class="pure-u-1">
                Copy Url: <a href="${getUrl}" target="_blank">${getUrl}</a>
            </div>
            <div class="pure-u-1">
                <button class="btn btn-primary" data-hx-indicator="#spinner" data-hx-target="#response-div"  data-hx-get="${prefill}">Prefill <img class="htmx-indicator" src="images/bars.svg"/></button>
            </div>
        </div>
    </div>
</fieldset>
</form>
<script>
    document.querySelectorAll('pre code').forEach((el) => {
        hljs.highlightElement(el);
    });
</script>
<#include "stratcommon.ftlh">
<@stratMacro builder = "btn btn-primary btn-active" fromJson= ""/>
