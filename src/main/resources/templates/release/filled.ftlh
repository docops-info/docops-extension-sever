<#-- @ftlvariable name="getUrl" type="java.lang.String" -->
<#-- @ftlvariable name="styles" type="java.util.HashMap" -->
<#-- @ftlvariable name="svg" type="java.lang.String" -->
<#-- @ftlvariable name="releaseStrategy" type="io.docops.docopsextensionssupport.releasestrategy.ReleaseStrategy" -->
<h1>Release Strategy Builder</h1>

<form class="grid grid-cols-2 gap-4" data-hx-put="api/release/build" data-hx-target="#response-div" data-hx-swap="innerHTML scroll:#contentBox:bottom">
            <div class="flex flex-col col-span-2">
                <label for="title" class="mb-2">Title</label>
                <input type="text" id="title" name="title" data-hx-put="api/release/build"
                       value="${releaseStrategy.title}" class="p-2 border rounded-md">
            </div>
            <div class="flex flex-col">
                <label for="style" class="mb-2">Display Style</label>
                <select id="style" name="style" data-hx-put="api/release/build"
                        class="select select-info">
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
                    <div class="flex flex-col">
                        <label for="type_${release_index}" class="mb-2">Type</label>
                        <select id="type_${release_index}" name="type_${release_index}" data-hx-put="api/release/build"
                                class="select select-info">
                            <#list releaseTypes as rel>
                                <#if rel == release.type>
                                    <option value="${rel}" selected>${rel}</option>
                                <#else>
                                    <option value="${rel}">${rel}</option>
                                </#if>
                            </#list>
                        </select>
                    </div>
                    <div class="flex flex-col col-span-2">
                        <label for="goal_${release_index}" class="mb-2">Goal</label>
                        <input type="text" id="goal_${release_index}" name="goal_${release_index}"
                               value="${release.goal}" class="p-2 border rounded-md">
                    </div>
                    <div class="flex flex-col  col-span-2">
                        <label for="date_${release_index}" class="mb-2">Target Date</label>
                        <input type="text" id="date_${release_index}" name="date_${release_index}"
                               value="${release.date}" class="p-2 border rounded-md">
                    </div>
                <div class="flex flex-col  col-span-2">
                    <label class="mb-2">Reason Line</label>
                    </div>
                    <#list release.lines as line>
                        <div class="flex flex-col col-span-2">
                        <input type="text" id="line_${release_index}" name="line_${release_index}"
                               value="${line}"  class="p-2 border rounded-md">
                        </div>
                    </#list>
                <div class="flex flex-col">
                    <button class="py-2 px-4 btn btn-outline btn-info text-white rounded-md mt-4" type="submit" name="addLine" value="line_${release_index}">+ New Line</button>
                </div>
            </#list>
                <div class="flex flex-col">
                    <button class="py-2 px-4 btn btn-outline btn-info text-white rounded-md mt-4" type="submit" name="addType" value="increase">+ New Milestone</button>
                </div>
                <div class="flex flex-col">
                    <button class="py-2 px-4 btn btn-outline btn-info text-white rounded-md mt-4" data-hx-indicator="#spinner" data-hx-target="#response-div">Submit</button>
                </div>
                <div class="flex flex-col">
                    <input type="button" class="py-2 px-4 btn btn-outline btn-success text-white rounded-md mt-4" data-hx-indicator="#spinner" data-hx-target="#response-div" onclick="downloadSVGAsText('${bsvg}')" value="Download "></input>
                </div>
        <div class="flex flex-col col-span-2">
            <#noautoesc>
                <div class="divider"></div>
                <div id="contentBox">${svg}</div>
            </#noautoesc>
        </div>
        <div class="flex flex-col col-span-2">
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
</form>
<script>
    document.querySelectorAll('pre code').forEach((el) => {
        hljs.highlightElement(el);
    });
</script>
<#include "stratcommon.ftlh">
<@stratMacro builder = "btn btn-primary btn-active" fromJson= ""/>
