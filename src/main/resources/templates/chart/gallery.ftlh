<#-- @ftlvariable name="imageUrlGenerator" type="io.docops.docopsextensionssupport.gallery.ImageUrlGenerator" -->
<div>
    <h3>Chart Gallery</h3>
    <figure>
        <figcaption class="figure-key">
            Charts Gallery
        </figcaption>
        <div class="figure-content">
            <div id="gallery-chart-container">
            <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("chart/bar1.json", "bar")}">
                <img src="${imageUrlGenerator.getUrl("chart/bar1.json", "bar")}" alt="gallery item bar1"/>
            </object>
            </div>
        </div>
    </figure>
    <a id="targetJsonView" href="#" data-hx-get="galleryJsonView.html?name=bar2.json" data-hx-target="#json-target">View JSON</a>
    <div id="json-target"></div>
    <div class="inline-flex rounded-md shadow-sm" role="group">
        <button data-hx-get="galleryItem.html?name=bar1.json&kind=bar&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-s-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Bar Chart Dark
        </button>
        <button data-hx-get="galleryItem.html?name=bar2.json&kind=bar&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border-t border-b border-gray-200 hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Bar Chart
        </button>
        <button data-hx-get="galleryItem.html?name=group1.json&kind=bargroup&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Group Chart
        </button>
        <button data-hx-get="galleryItem.html?name=group2.json&kind=bargroup&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Group Chart Dark
        </button>
        <button data-hx-get="galleryItem.html?name=group3.json&kind=bargroup&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Group Chart
        </button>
        <button data-hx-get="galleryItem.html?name=pie1.json&kind=pieslice&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Pie Chart 1
        </button>
        <button data-hx-get="galleryItem.html?name=pie2.json&kind=pieslice&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Donut View
        </button>
        <button data-hx-get="galleryItem.html?name=pie3.json&kind=pie&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Percentages 1
        </button>
        <button data-hx-get="galleryItem.html?name=pie4.json&kind=pie&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Percentages 2
        </button>
        <button data-hx-get="galleryItem.html?name=pie5.json&kind=pie&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Percentages 3
        </button>
        <button data-hx-get="galleryItem.html?name=pie6.json&kind=pie&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Percentages 4
        </button>
        <button data-hx-get="galleryItem.html?name=line1.json&kind=line&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Line 1
        </button>
        <button data-hx-get="galleryItem.html?name=line2.json&kind=line&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Line 2
        </button>
        <button data-hx-get="galleryItem.html?name=line3.json&kind=line&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-e-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Line 3
        </button>
    </div>
</div>