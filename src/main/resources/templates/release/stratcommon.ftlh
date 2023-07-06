<#macro stratMacro builder fromJson>
    <a id="strat" data-hx-get="strat.html" class="pure-button ${builder}" data-hx-target="#contentBoxPanel" data-hx-swap-oob="true">Builder</a>
    <a id="fromjson" data-hx-get="fromJson.html" class="pure-button ${fromJson}" data-hx-target="#contentBoxPanel" data-hx-swap-oob="true">From JSON</a>
</#macro>