<!-- src/main/resources/templates/icons/icon-grid.ftlh -->
<div>
    <h2 class="text-2xl font-bold mb-4">Icons - ${currentLetter}</h2>
    <!-- Hidden span to track current letter for JavaScript -->
    <span id="current-letter" class="hidden">${currentLetter}</span>
    
    <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
        <#list icons as icon>
            <div class="icon-card bg-white p-4 rounded-lg shadow hover:shadow-md">
                <button 
                    class="show-icon-details flex flex-col items-center w-full"
                    data-icon-slug="${icon}"
                    hx-get="icons/details/${icon}"
                    hx-target="#icon-details-content"
                    hx-trigger="click">
                    
                    <div class="icon-preview h-16 w-16 flex items-center justify-center mb-2">
                        <img src="api/simpleicon/${icon}" alt="${icon}" class="h-12 w-12" onerror="this.style.display='none'; this.nextElementSibling.style.display='block';">
                        <div class="h-12 w-12 bg-gray-200 rounded flex items-center justify-center text-xs text-gray-500" style="display:none;">
                            No Icon
                        </div>
                    </div>
                    <span class="text-sm text-center truncate w-full">${icon}</span>
                </button>
            </div>
        </#list>
    </div>
</div>
