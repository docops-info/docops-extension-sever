<#macro chartCommon chart treechart stacked mychart>
    <a id="chart" class="pure-button ${chart}" data-hx-get="chart.html" data-hx-target="#contentBoxPanel" data-hx-swap-oob="true">Bar Chart</a>
    <a id="treechart" class="pure-button ${treechart}" data-hx-get="treechart.html" data-hx-target="#contentBoxPanel" data-hx-swap-oob="true">Tree Chart</a>
    <a id="stacked" class="pure-button ${stacked}" data-hx-get="stacked.html" data-hx-target="#contentBoxPanel" data-hx-swap-oob="true">Stacked</a>
    <a id="mychart" class="pure-button ${mychart}" data-hx-get="mychart.html" data-hx-target="#contentBoxPanel" data-hx-swap-oob="true">My Custom EChart</a>
</#macro>