<#ftl output_format="XML">
<#noautoesc>
${src}
</#noautoesc>
<input type="text" id="logo" name="logo" data-hx-put="api/badge/item" data-hx-target="#contentBox"
       value="${iconName}"   class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5  dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" data-hx-swap-oob="true">