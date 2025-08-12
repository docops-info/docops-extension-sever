<!-- src/main/resources/templates/icons/search-results.ftlh -->
<#if query?? && query?length gt 0>
    <div class="bg-white rounded-lg shadow-md p-4 mb-8">
        <h2 class="text-xl font-semibold mb-2">Search Results for "${query}"</h2>
        <p class="text-gray-600 mb-4">${resultCount} icons found</p>
        
        <#if resultCount gt 0>
            <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
                <#list icons as icon>
                    <div class="icon-card bg-gray-50 p-4 rounded-lg shadow hover:shadow-md">
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
        <#else>
            <div class="text-center py-8">
                <p class="text-gray-500">No icons found matching your search.</p>
            </div>
        </#if>
    </div>
</#if>
