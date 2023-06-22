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
                        <button type="submit" name="addType" value="increase">+ New Milestone</button>
                    </div>
                    <label for="logo">Build</label>
                    <div class="pure-g">
                        <button class="button-success pure-button" data-hx-indicator="#spinner" data-hx-target="#response-div">Submit <img class="htmx-indicator" src="images/bars.svg"/></button>
                    </div>
                </div>

            </div>

            <div class="pure-u-1-2">
                <div class="pure-u-1-2">
                    <#noautoesc>
                    <div id="contentBox"> ${svg}</div>
                    </#noautoesc>
                </div>
            </div>
        </div>
    </fieldset>
