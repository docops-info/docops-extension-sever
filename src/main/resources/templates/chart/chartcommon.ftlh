<#macro chartCommon chart treechart stacked mychart>
    <a id="chart" class="btn ${chart}" data-hx-get="chart.html" data-hx-target="#contentBoxPanel" data-hx-swap-oob="true">Bar Chart</a>
    <a id="treechart" class="btn ${treechart}" data-hx-get="treechart.html" data-hx-target="#contentBoxPanel" data-hx-swap-oob="true">Tree Chart</a>
    <a id="stacked" class="btn ${stacked}" data-hx-get="stacked.html" data-hx-target="#contentBoxPanel" data-hx-swap-oob="true">Stacked</a>
    <a id="mychart" class="btn ${mychart}" data-hx-get="mychart.html" data-hx-target="#contentBoxPanel" data-hx-swap-oob="true">My Custom EChart</a>
</#macro>