
<h3>Icons starting with: ${letter}</h3>

<#list icons as items>
<div class="mb-5">
    <#list items as icon>
    <span class="text-white bg-gradient-to-r from-cyan-400 via-cyan-500 to-cyan-600 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-cyan-300 dark:focus:ring-cyan-800 shadow-lg shadow-cyan-500/50 dark:shadow-lg dark:shadow-cyan-800/80 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2" id="${icon}" data-hx-vals='{ "iconName": "${icon}" }' data-hx-target="#placeholder" data-hx-get="api/simple/icon" data-hx-trigger="click">${icon}</span>
    </#list>
</div>
</#list>
