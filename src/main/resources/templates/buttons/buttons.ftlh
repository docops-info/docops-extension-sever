<h1>Buttons/Panels Creation UI</h1>
<div>
    <article class="flex max-w-xl flex-col items-start justify-between">
        <p class="mt-5 text-sm leading-6 text-gray-600">
            This view allows developers to create buttons and panels in a flexible way that can be easily customized
            inside of AsciiDoctor.
        </p>
        <div data-hx-get="json/schema?name=buttons-schema" data-hx-target="#buttonTarget">
            Click here to view the JSON Schema for buttons
        </div>
    </article>
</div>

<ul class="flex flex-wrap text-sm font-medium text-center text-gray-500 border-b border-gray-200 dark:border-gray-700 dark:text-gray-400">
    <li class="me-2">
        <a href="#"  class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="button/fromJson.html?type=REGULAR" data-hx-target="#buttonTarget"><i class="fad fa-tablet-button text-xs mr-2"></i> Regular</a>
    </li>
    <li class="me-2">
        <a href="#"  class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="button/fromJson.html?type=OVAL" data-hx-target="#buttonTarget"><i class="fad fa-tablet-button text-xs mr-2"></i> OVAL</a>
    </li>
    <li class="me-2">
        <a href="#"  class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="button/fromJson.html?type=ROUND" data-hx-target="#buttonTarget"><i class="fad fa-circle text-xs mr-2"></i> Round</a>
    </li>

    <li class="me-2">
        <a href="#" class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="button/fromJson.html?type=CIRCLE" data-hx-target="#buttonTarget"><i class="fad fa-circle text-xs mr-2"></i> Circle</a>
    </li>
    <li class="me-2">
        <a href="#" class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="button/fromJson.html?type=LARGE" data-hx-target="#buttonTarget"><i class="fad fa-square text-xs mr-2"></i> Large</a>
    </li>
    <li class="me-2">
        <a href="#" class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="button/fromJson.html?type=HEX" data-hx-target="#buttonTarget"><i class="fad fa-square text-xs mr-2"></i> Honeycomb (Hex)</a>
    </li>
    <li class="me-2">
        <a href="#" class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="button/fromJson.html?type=PILL" data-hx-target="#buttonTarget"><i class="fad fa-pills text-xs mr-2"></i> Pill</a>
    </li>
    <li class="me-2">
        <a href="#" class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="button/fromJson.html?type=RECTANGLE" data-hx-target="#buttonTarget"><i class="fad fa-rectangle-wide text-xs mr-2"></i> Rectangle</a>
    </li>
    <li class="me-2">
        <a href="#" class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="button/fromJson.html?type=SLIM" data-hx-target="#buttonTarget"><i class="fad fa-watch-fitness text-xs mr-2"></i> Slim</a>
    </li>
    <li class="me-2">
        <a href="#" class="inline-block p-4 text-blue-600 bg-gray-100 rounded-t-lg active dark:bg-gray-800 dark:text-blue-500" hx-get="button/fromJson.html?type=REGULAR-LABEL" data-hx-target="#buttonTarget"><i class="fad fa-tablet-button text-xs mr-2"></i> Regular-Label Sorted</a>
    </li>
</ul>
<div id="buttonTarget"></div>
