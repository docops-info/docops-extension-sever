<#-- @ftlvariable name="gradientSource" type="java.lang.String" -->
<h1>Slim Panel Editor</h1>

<legend>Slim Panel Generator</legend>

<div class="pure-g">
    <form class="pure-form pure-form-stacked" data-hx-put="api/slimpanel" data-hx-target="#contentBox">
        <fieldset>
            <legend>Theme Selector</legend>
            <div class="pure-u-16-24">
                <label for="theme">Predefined Themes</label>
                <select id="theme" name="theme" data-hx-put="api/slimpanel" data-hx-target="#contentBox" data-hx-trigger="change[target.selectedIndex != 0]">
                    <option>----</option>
                    <option value="BluesTheme">Blue Theme</option>
                    <option value="RedsTheme">Red Theme</option>
                    <option value="GreensTheme">Green Theme</option>
                    <option value="PurplesTheme">Purple Theme</option>
                    <option value="LightPurpleTheme">Light Purple Theme</option>
                    <option value="MagentasTheme">Magenta Theme</option>
                    <option value="DarkTheme">Dark1 Theme</option>
                    <option value="DarkTheme2">Dark2 Theme</option>
                    <option value="LightGreysTheme">LightGrey Theme</option>
                    <option value="OrangesTheme">Oranges Theme</option>
                </select>
            </div>
        </fieldset>

        <fieldset>
            <button class="button-success pure-button">Submit</button>
            <input class="button-secondary pure-button" type="button" onclick="copyTextToClipboard(panelSource);"
                   value="Copy Panel As AsciiDoc">
            <a id="customslim" data-hx-get="panels/customslim.html" class="button-secondary pure-button" data-hx-target="#contentBoxPanel" hx-include="[name='theme']">Customize</a>

        </fieldset>
    </form>



</div>
<div class="pure-g">
    <div class="pure-u-22-24">
        <section id="contentBox" class="tool-bar">

        </section>
    </div>
</div>

<#include "panelcommon.ftlh">
<@panelsMacro panelgenerator = "" panelimagebuilder= "" twotoneimagebuilder= "" panelseditor= "" slimpanel="pure-button-primary pure-button-active"/>
