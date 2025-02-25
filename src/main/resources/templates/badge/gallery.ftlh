<#-- @ftlvariable name="imageUrlGenerator" type="io.docops.docopsextensionssupport.gallery.ImageUrlGenerator" -->
<div>
    <h3>Chart Gallery</h3>

    <div class="flex space-x-4 overflow-x-auto pb-4">
        <div class="flex-shrink-0 w-64 bg-white rounded-lg shadow overflow-hidden">
        <button data-hx-get="galleryItem.html?name=docops.txt&kind=badge&type=badge" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border-t border-b border-gray-200 hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            DocOps
        </button>
        <button data-hx-get="galleryItem.html?name=badge1.txt&kind=badge&type=badge" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-s-lg hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Badge 1
        </button>
        <button data-hx-get="galleryItem.html?name=badge2.txt&kind=badge&type=badge" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border-t border-b border-gray-200 hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Badge2
        </button>
        <button data-hx-get="galleryItem.html?name=naruto.txt&kind=badge&type=badge" data-hx-target="#gallery-chart-container" type="button" class="px-4 py-2 text-sm font-medium text-gray-900 bg-white border-t border-b border-gray-200 hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-2 focus:ring-blue-700 focus:text-blue-700 dark:bg-gray-800 dark:border-gray-700 dark:text-white dark:hover:text-white dark:hover:bg-gray-700 dark:focus:ring-blue-500 dark:focus:text-white">
            Naruto
        </button>
        </div>
    </div>
    <figure>
        <figcaption class="figure-key">
            Badge Gallery
        </figcaption>
        <div class="figure-content">
            <div id="gallery-chart-container" class="flex-shrink-0 w-64 bg-white rounded-lg shadow overflow-hidden">
                <object type="image/svg+xml" data="${imageUrlGenerator.getUrl("badge/badge1.txt", "badge")}">
                    <img src="${imageUrlGenerator.getUrl("badge/badge1.txt", "badge")}" alt="gallery item badge1"/>
                </object>
            </div>
        </div>
    </figure>
    <a id="targetJsonView" href="#" data-hx-get="galleryJsonView.html?name=badge1.txt&type=badge" data-hx-target="#json-target">View JSON</a>
    <div id="json-target"></div>
</div>