<!-- src/main/resources/templates/icons/icon-details.ftlh -->
<div class="text-center">
    <#if hasIcon!false>
        <div class="icon-large mb-4 flex justify-center">
            <div style="width: 96px; height: 96px;">${svg?no_esc}</div>
        </div>
        
        <h3 class="text-xl font-semibold mb-2">${title}</h3>
        <p class="text-gray-600 mb-4">${slug}</p>
        
        <div class="mb-4 flex items-center justify-center">
            <div class="w-6 h-6 rounded mr-2" style="background-color: #${hex}"></div>
            <code class="bg-gray-100 px-2 py-1 rounded">#${hex}</code>
        </div>
        
        <div class="grid grid-cols-2 gap-2 mb-4">
            <button 
                class="w-full bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md"
                onclick="copyToClipboard(document.getElementById('svg-code').innerText)">
                Copy SVG
            </button>
            <a 
                href="api/simpleicon/${slug}/download"
                download="${slug}.svg"
                class="block w-full bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-md text-center">
                Download
            </a>
        </div>
        
        <div class="mt-4">
            <pre id="svg-code" class="bg-gray-100 p-2 rounded-md text-xs text-left overflow-auto max-h-40">${svg}</pre>
        </div>
    <#else>
        <div class="text-center py-8">
            <p class="text-red-500 mb-2">Error: ${error!"Icon not available"}</p>
            <p class="text-gray-600">The icon "${slug}" could not be loaded.</p>
        </div>
    </#if>
</div>
