<#ftl output_format="XML">
<#noautoesc>
${src}
</#noautoesc>
<input type="text" id="logo" name="logo" data-hx-put="api/badge/item" data-hx-target="#contentBox"
       value="${iconName}"   class="pure-u-1-1" data-hx-swap-oob="true">