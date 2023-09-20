<#-- @ftlvariable name="getUrl" type="java.lang.String" -->
<#-- @ftlvariable name="styles" type="java.util.HashMap" -->
<#-- @ftlvariable name="svg" type="java.lang.String" -->
<#-- @ftlvariable name="releaseStrategy" type="io.docops.docopsextensionssupport.releasestrategy.ReleaseStrategy" -->
<h1>Release Strategy Builder</h1>

<form data-hx-put="api/release/build" data-hx-target="#response-div">

<fieldset>
    <legend>Release Strategy</legend>
    <div class="pure-g">

        <div>
            <div class="join">
                <label for="title" class="btn join-item">Title</label>
                <input type="text" id="title" name="title" data-hx-put="api/release/build"
                       value="${releaseStrategy.title}" class="join-item input input-bordered input-primary w-full max-w-xs">
            </div>
        </div>
        <div>
            <div class="join">
                <label for="style" class="btn join-item">Display Style</label>
                <select id="style" name="style" data-hx-put="api/release/build"
                        class="join-item select select-primary w-full max-w-xs">
                    <#list styles?keys as style>
                        <#if releaseStrategy.style == style>
                            <option value="${style}" selected>${styles[style]}</option>
                        <#else>
                            <option value="${style}">${styles[style]}</option>
                        </#if>
                    </#list>
                </select>
            </div>
        </div>
            <#list releaseStrategy.releases as release>
                <div>
                    <div class="join">
                        <label for="type_${release_index}" class="btn join-item">Type</label>
                        <select id="type_${release_index}" name="type_${release_index}" data-hx-put="api/release/build"
                                class="join-item select select-primary w-full max-w-xs">
                            <#list releaseTypes as rel>
                                <#if rel == release.type>
                                    <option value="${rel}" selected>${rel}</option>
                                <#else>
                                    <option value="${rel}">${rel}</option>
                                </#if>
                            </#list>
                        </select>
                    </div>
                </div>
                <div>
                    <div class="join">
                        <label for="date_${release_index}" class="btn join-item">Goal</label>
                        <input type="text" id="goal_${release_index}" name="goal_${release_index}"
                               value="${release.goal}" class="join-item input input-bordered input-primary w-full max-w-xs">
                    </div>
                </div>
                <div>
                    <div class="join">
                        <label for="date_${release_index}" class="btn join-item">Target Date</label>
                        <input type="text" id="date_${release_index}" name="date_${release_index}"
                               value="${release.date}" class="join-item input input-bordered input-primary w-full max-w-xs">
                    </div>
                </div>
                <div >
                    <label class="btn join-item">Reason Line</label>
                <div >
                    <#list release.lines as line>
                        <div>
                        <input type="text" id="line_${release_index}" name="line_${release_index}"
                               value="${line}"  class="input input-bordered input-primary w-full max-w-xs">
                        </div>
                    </#list>
                </div>
                </div>
                <div>
                <div >
                    <button class="btn btn-outline" type="submit" name="addLine" value="line_${release_index}">+ New Line</button>
                </div>
                </div>
            </#list>
            <div>
                <div><br/>
                    <button class="btn btn-outline" type="submit" name="addType" value="increase">+ New Milestone</button>
                </div>
                <label for="logo">Build</label>
                <div>
                    <button class="btn btn-success" data-hx-indicator="#spinner" data-hx-target="#response-div">Submit <img class="htmx-indicator" src="images/bars.svg"/></button>
                    <button class="btn btn-secondary" data-hx-indicator="#spinner" data-hx-target="#response-div" type="button" onclick="downloadSVGAsText('${bsvg}')">Download <img class="htmx-indicator" src="images/bars.svg"/></button>
                </div>


        </div>
        <div class="pure-u-1-1">
            <hr/>
        </div>
        <div class="pure-u-1-1">
            <#noautoesc>
                <div class="divider"></div>
                <div id="contentBox">${svg}</div>
            </#noautoesc>
        </div>
        <div>
            <div>
                    <pre>
                        <code>
                        <#noautoesc>
                            <div class="divider"></div>
${sourceJson}
                        </#noautoesc>
                        </code>
                    </pre>

            </div>
            <div>
                Copy Url: <a class="btn btn-outline" href="${getUrl}" target="_blank">${getUrl}</a>
            </div>
            <div>
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
