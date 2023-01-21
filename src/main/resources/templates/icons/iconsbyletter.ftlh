
<h3>Icons starting with: ${letter}</h3>

<#list icons as items>
<div>
    <#list items as icon>
    <span class="pure-u-1-5 pure-button" id="${icon}" data-hx-vals='{ "iconName": "${icon}" }' data-hx-target="#placeholder" data-hx-get="api/simple/icon" data-hx-trigger="click">${icon}</span>
    </#list>
</div>
</#list>
