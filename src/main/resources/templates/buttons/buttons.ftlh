<h1>Panels Creation UI</h1>
<div role="tablist" class="tabs tabs-boxed">
    <a role="tab" class="tab btnMenu REGULAR" hx-get="button/fromJson.html?type=REGULAR" data-hx-target="#buttonTarget"><i class="fad fa-tablet-button text-xs mr-2"></i> Regular</a>
    <a role="tab" class="tab btnMenu ROUND" hx-get="button/fromJson.html?type=ROUND" data-hx-target="#buttonTarget"><i class="fad fa-circle text-xs mr-2"></i> Round</a>
    <a role="tab" class="tab btnMenu LARGE" hx-get="button/fromJson.html?type=LARGE" data-hx-target="#buttonTarget"><i class="fad fa-square text-xs mr-2"></i> Large</a>
    <a role="tab" class="tab btnMenu PILL" hx-get="button/fromJson.html?type=PILL" data-hx-target="#buttonTarget"><i class="fad fa-pills text-xs mr-2"></i> Pill</a>
    <a role="tab" class="tab btnMenu RECTANGLE" hx-get="button/fromJson.html?type=RECTANGLE" data-hx-target="#buttonTarget"><i class="fad fa-rectangle-wide text-xs mr-2"></i> Rectangle</a>
    <a role="tab" class="tab btnMenu SLIM" hx-get="button/fromJson.html?type=SLIM" data-hx-target="#buttonTarget"><i class="fad fa-watch-fitness text-xs mr-2"></i> Slim</a>
    <a role="tab" class="tab btnMenu REGULAR-LABEL" hx-get="button/fromJson.html?type=REGULAR-LABEL" data-hx-target="#buttonTarget"><i class="fad fa-tablet-button text-xs mr-2"></i> Regular-Label Sorted</a>
    <a role="tab" class="tab btnMenu buttons-schema" hx-get="json/schema?name=buttons-schema" data-hx-target="#buttonTarget"><i class="fad fa-curling text-xs mr-2"></i> JSON Schema</a>
    <a role="tab" class="tab btnMenu PNG" hx-get="button/fromJsonToPng.html?TYPE=REGULAR" data-hx-target="#buttonTarget"><i class="fad fa-curling text-xs mr-2"></i> Convert To PNG</a>
    <a role="tab" class="tab btnMenu panel" hx-get="button/convert.html" data-hx-target="#buttonTarget"><i class="fad fa-truck-moving text-xs mr-2"></i> Convert from Panel</a>

</div>

<div id="buttonTarget"></div>
