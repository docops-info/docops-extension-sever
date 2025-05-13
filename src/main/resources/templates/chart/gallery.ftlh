<#-- @ftlvariable name="imageUrlGenerator" type="io.docops.docopsextensionssupport.gallery.ImageUrlGenerator" -->
<div>
    <h3>Chart Gallery</h3>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8">
        <div class="chart-card bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
            <div class="p-4">
                <h4 class="font-medium text-lg mb-2">Bar Chart Dark</h4>
                <div class="chart-preview">
                    <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("chart/bar1.json", "bar")}">
                        <img src="${imageUrlGenerator.getUrl("chart/bar1.json", "bar")}" alt="Bar Chart Dark" class="w-full h-40 object-contain"/>
                    </object>
                </div>
                <div class="mt-4 flex justify-between">
                    <button data-hx-get="galleryItem.html?name=bar1.json&kind=bar&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-3 py-1.5 text-sm font-medium text-gray-100 bg-blue-600 rounded hover:bg-blue-700 focus:ring-2 focus:ring-blue-500">
                        View Full Size
                    </button>
                    <a href="#" data-hx-get="galleryJsonView.html?name=bar1.json&type=chart" data-hx-target="#json-target" class="px-3 py-1.5 text-sm font-medium text-blue-600 border border-blue-600 rounded hover:bg-blue-50">
                        View JSON
                    </a>
                </div>
            </div>
        </div>

        <div class="chart-card bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
            <div class="p-4">
                <h4 class="font-medium text-lg mb-2">Bar Chart</h4>
                <div class="chart-preview">
                    <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("chart/bar2.json", "bar")}">
                        <img src="${imageUrlGenerator.getUrl("chart/bar2.json", "bar")}" alt="Bar Chart" class="w-full h-40 object-contain"/>
                    </object>
                </div>
                <div class="mt-4 flex justify-between">
                    <button data-hx-get="galleryItem.html?name=bar2.json&kind=bar&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-3 py-1.5 text-sm font-medium text-gray-100 bg-blue-600 rounded hover:bg-blue-700 focus:ring-2 focus:ring-blue-500">
                        View Full Size
                    </button>
                    <a href="#" data-hx-get="galleryJsonView.html?name=bar2.json&type=chart" data-hx-target="#json-target" class="px-3 py-1.5 text-sm font-medium text-blue-600 border border-blue-600 rounded hover:bg-blue-50">
                        View JSON
                    </a>
                </div>
            </div>
        </div>

        <div class="chart-card bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
            <div class="p-4">
                <h4 class="font-medium text-lg mb-2">Group Chart</h4>
                <div class="chart-preview">
                    <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("chart/group1.json", "bargroup")}">
                        <img src="${imageUrlGenerator.getUrl("chart/group1.json", "bargroup")}" alt="Group Chart" class="w-full h-40 object-contain"/>
                    </object>
                </div>
                <div class="mt-4 flex justify-between">
                    <button data-hx-get="galleryItem.html?name=group1.json&kind=bargroup&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-3 py-1.5 text-sm font-medium text-gray-100 bg-blue-600 rounded hover:bg-blue-700 focus:ring-2 focus:ring-blue-500">
                        View Full Size
                    </button>
                    <a href="#" data-hx-get="galleryJsonView.html?name=group1.json&type=chart" data-hx-target="#json-target" class="px-3 py-1.5 text-sm font-medium text-blue-600 border border-blue-600 rounded hover:bg-blue-50">
                        View JSON
                    </a>
                </div>
            </div>
        </div>

        <div class="chart-card bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
            <div class="p-4">
                <h4 class="font-medium text-lg mb-2">Group Chart Dark</h4>
                <div class="chart-preview">
                    <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("chart/group2.json", "bargroup")}">
                        <img src="${imageUrlGenerator.getUrl("chart/group2.json", "bargroup")}" alt="Group Chart Dark" class="w-full h-40 object-contain"/>
                    </object>
                </div>
                <div class="mt-4 flex justify-between">
                    <button data-hx-get="galleryItem.html?name=group2.json&kind=bargroup&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-3 py-1.5 text-sm font-medium text-gray-100 bg-blue-600 rounded hover:bg-blue-700 focus:ring-2 focus:ring-blue-500">
                        View Full Size
                    </button>
                    <a href="#" data-hx-get="galleryJsonView.html?name=group2.json&type=chart" data-hx-target="#json-target" class="px-3 py-1.5 text-sm font-medium text-blue-600 border border-blue-600 rounded hover:bg-blue-50">
                        View JSON
                    </a>
                </div>
            </div>
        </div>

        <div class="chart-card bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
            <div class="p-4">
                <h4 class="font-medium text-lg mb-2">Pie Chart 1</h4>
                <div class="chart-preview">
                    <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("chart/pie1.json", "pieslice")}">
                        <img src="${imageUrlGenerator.getUrl("chart/pie1.json", "pieslice")}" alt="Pie Chart 1" class="w-full h-40 object-contain"/>
                    </object>
                </div>
                <div class="mt-4 flex justify-between">
                    <button data-hx-get="galleryItem.html?name=pie1.json&kind=pieslice&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-3 py-1.5 text-sm font-medium text-gray-100 bg-blue-600 rounded hover:bg-blue-700 focus:ring-2 focus:ring-blue-500">
                        View Full Size
                    </button>
                    <a href="#" data-hx-get="galleryJsonView.html?name=pie1.json&type=chart" data-hx-target="#json-target" class="px-3 py-1.5 text-sm font-medium text-blue-600 border border-blue-600 rounded hover:bg-blue-50">
                        View JSON
                    </a>
                </div>
            </div>
        </div>

        <div class="chart-card bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
            <div class="p-4">
                <h4 class="font-medium text-lg mb-2">Donut View</h4>
                <div class="chart-preview">
                    <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("chart/pie2.json", "pieslice")}">
                        <img src="${imageUrlGenerator.getUrl("chart/pie2.json", "pieslice")}" alt="Donut View" class="w-full h-40 object-contain"/>
                    </object>
                </div>
                <div class="mt-4 flex justify-between">
                    <button data-hx-get="galleryItem.html?name=pie2.json&kind=pieslice&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-3 py-1.5 text-sm font-medium text-gray-100 bg-blue-600 rounded hover:bg-blue-700 focus:ring-2 focus:ring-blue-500">
                        View Full Size
                    </button>
                    <a href="#" data-hx-get="galleryJsonView.html?name=pie2.json&type=chart" data-hx-target="#json-target" class="px-3 py-1.5 text-sm font-medium text-blue-600 border border-blue-600 rounded hover:bg-blue-50">
                        View JSON
                    </a>
                </div>
            </div>
        </div>

        <div class="chart-card bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
            <div class="p-4">
                <h4 class="font-medium text-lg mb-2">Line Chart 1</h4>
                <div class="chart-preview">
                    <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("chart/line1.json", "line")}">
                        <img src="${imageUrlGenerator.getUrl("chart/line1.json", "line")}" alt="Line Chart 1" class="w-full h-40 object-contain"/>
                    </object>
                </div>
                <div class="mt-4 flex justify-between">
                    <button data-hx-get="galleryItem.html?name=line1.json&kind=line&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-3 py-1.5 text-sm font-medium text-gray-100 bg-blue-600 rounded hover:bg-blue-700 focus:ring-2 focus:ring-blue-500">
                        View Full Size
                    </button>
                    <a href="#" data-hx-get="galleryJsonView.html?name=line1.json&type=chart" data-hx-target="#json-target" class="px-3 py-1.5 text-sm font-medium text-blue-600 border border-blue-600 rounded hover:bg-blue-50">
                        View JSON
                    </a>
                </div>
            </div>
        </div>

        <div class="chart-card bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
            <div class="p-4">
                <h4 class="font-medium text-lg mb-2">Line Chart 2</h4>
                <div class="chart-preview">
                    <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("chart/line2.json", "line")}">
                        <img src="${imageUrlGenerator.getUrl("chart/line2.json", "line")}" alt="Line Chart 2" class="w-full h-40 object-contain"/>
                    </object>
                </div>
                <div class="mt-4 flex justify-between">
                    <button data-hx-get="galleryItem.html?name=line2.json&kind=line&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-3 py-1.5 text-sm font-medium text-gray-100 bg-blue-600 rounded hover:bg-blue-700 focus:ring-2 focus:ring-blue-500">
                        View Full Size
                    </button>
                    <a href="#" data-hx-get="galleryJsonView.html?name=line2.json&type=chart" data-hx-target="#json-target" class="px-3 py-1.5 text-sm font-medium text-blue-600 border border-blue-600 rounded hover:bg-blue-50">
                        View JSON
                    </a>
                </div>
            </div>
        </div>

        <div class="chart-card bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
            <div class="p-4">
                <h4 class="font-medium text-lg mb-2">Percentage Graphs</h4>
                <div class="chart-preview">
                    <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("chart/pie6.json", "pie")}">
                        <img src="${imageUrlGenerator.getUrl("chart/pie6.json", "pie")}" alt="Line Chart 2" class="w-full h-40 object-contain"/>
                    </object>
                </div>
                <div class="mt-4 flex justify-between">
                    <button data-hx-get="galleryItem.html?name=pie6.json&kind=pie&type=chart" data-hx-target="#gallery-chart-container" type="button" class="px-3 py-1.5 text-sm font-medium text-gray-100 bg-blue-600 rounded hover:bg-blue-700 focus:ring-2 focus:ring-blue-500">
                        View Full Size
                    </button>
                    <a href="#" data-hx-get="galleryJsonView.html?name=pie6.json&type=chart" data-hx-target="#json-target" class="px-3 py-1.5 text-sm font-medium text-blue-600 border border-blue-600 rounded hover:bg-blue-50">
                        View JSON
                    </a>
                </div>
            </div>
        </div>
    </div>

    <figure>
        <figcaption class="figure-key">
            Selected Chart
        </figcaption>
        <div class="figure-content">
            <div id="gallery-chart-container" class="w-full bg-white rounded-lg shadow-lg overflow-hidden p-4">
                <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("chart/bar1.json", "bar")}">
                    <img src="${imageUrlGenerator.getUrl("chart/bar1.json", "bar")}" alt="gallery item bar1" class="mx-auto"/>
                </object>
            </div>
        </div>
    </figure>

    <div id="json-target" class="mt-6 bg-gray-50 rounded-lg p-4"></div>
</div>

