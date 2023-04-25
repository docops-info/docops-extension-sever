<H1>Panel Generator With Generated Colors</H1>

<legend>Panel Generator Controls</legend>

<form class="pure-form pure-form-stacked" data-hx-put="api/colorgen" data-hx-target="#contentBox">
    <fieldset>
        <legend>Buttons</legend>
        <div class="pure-g">
            <div class="pure-u-1-4">
                <label for="points"># Of Buttons <span id="pointsValue"></span></label>
                <input type="number" id="points" name="points" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                       value="6" step="1" max="20" min="2" class="pure-u-1-4">
            </div>
            <div class="pure-u-1-4">
                <label for="columns"># Of Columns <span id="colsValue"></span></label>
                <input type="number" id="columns" name="columns" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                       value="3" step="1" max="15" min="1" class="pure-u-1-4">
            </div>
        </div>
    </fieldset>
    <fieldset>
        <legend>Button Type & Sorting</legend>
        <details>
            <summary>Show/Hide</summary>
            <div class="pure-g">
                <div class="pure-u-1-4">
                    <label for="buttonType">Button Type</label>
                    <select id="buttonType" name="buttonType" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                            class="pure-input-4-12">
                        <option value="BUTTON">BUTTON</option>
                        <option value="ROUND">ROUND</option>
                        <option value="SLIM_CARD">SLIM</option>
                        <option value="LARGE_CARD">LARGE</option>
                        <option value="RECTANGLE">RECTANGLE</option>
                    </select>
                </div>

                <div class="pure-u-1-4">
                    <label for="sortBy">Sort By</label>
                    <select id="sortBy" name="sortBy" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                            class="pure-input-4-12">
                        <option value="Grouping.ORDER">ORDER</option>
                        <option value="Grouping.TYPE">TYPE</option>
                        <option value="Grouping.TITLE">TITLE</option>
                        <option value="Grouping.AUTHOR">AUTHOR</option>
                        <option value="Grouping.DATE">DATE</option>
                    </select>
                </div>
                <div class="pure-u-1-4">
                    <label for="order">Order</label>
                    <select id="order" name="order" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                            class="pure-input-4-12">
                        <option value="GroupingOrder.ASCENDING">ASCENDING</option>
                        <option value="GroupingOrder.DESCENDING">DESCENDING</option>
                    </select>
                </div>
            </div>
        </details>
    </fieldset>
    <fieldset>
        <legend>Organization</legend>
        <details>
            <summary>Show/Hide Drop Shadow</summary>
            <div class="pure-u-4-24">
                <label for="dropShadow">Drop Shadow <span id="shadowValue"></span></label>
                <input type="number" id="dropShadow" name="dropShadow" data-hx-put="api/colorgen"
                       data-hx-target="#contentBox" value="0" step="1" max="9" min="0" class="pure-u-12-24">
            </div>
            <div class="pure-u-4-24">
                <label for="newWin">Open In New Window</label>
                <input type="checkbox" id="newWin" name="newWin" data-hx-put="api/colorgen"
                       data-hx-target="#contentBox" class="pure-u-12-24">
            </div>
        </details>

    </fieldset>
    <fieldset>
        <legend>Font</legend>
        <details>
            <summary>Show/Hide</summary>
            <div class="pure-g">
                <div class="pure-button-group pure-u-6-24" role="group" aria-label="bold italic underline">
                    <ul>
                        <li class="pure-button" id="boldI" data-toggle-id="bold" >
                            <input type="checkbox" id="bold" name="bold" style="display: none">
                            <label class="label-checkbox" for="bold"><i  class="fa fa-bold"></i></label>
                        </li>
                        <li class="pure-button" id="italicI" data-toggle-id="italic">
                            <input type="checkbox" id="italic" name="italic" style="display: none">
                            <label for="italic"><i class="fa fa-italic"></i></label>
                        </li>
                        <li class="pure-button" id="underlineI" data-toggle-id="underline">
                            <input type="checkbox" id="underline" name="underline" style="display: none">
                            <label for="underline" ><i class="fa fa-underline"></i></label>
                        </li>
                    </ul>
                </div>

            </div>
        </details>
    </fieldset>
    <fieldset>
        <legend>Font Size & Color</legend>
        <details>
            <summary>Show/Hide</summary>
            <div class="pure-g">
                <div class="pure-u-6-24">
                    <label for="size">Font Size <span id="fSize"></span></label>
                    <div class="pure-g">
                        <input type="number" id="size" name="size" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                               value="8" step="1" max="36" min="0" class="pure-u-12-24">
                        <label for="fpoint"></label>
                        <select id="fpoint" name="fpoint" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                                class="pure-input-11-24">
                            <option value="pt">pt</option>
                            <option value="px">px</option>
                            <option value="in">in</option>
                            <option value="pc">pc</option>
                            <option value="mm">mm</option>
                            <option value="cm">cm</option>
                        </select>
                    </div>
                </div>
                <div class="pure-u-6-24">
                    <label for="color">Color</label>
                    <input type="text" id="color" name="color" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                           value="#000000" class="pure-u-8-24">
                </div>
            </div>
        </details>
    </fieldset>
    <fieldset>
        <legend>Font Family</legend>
        <details>
            <summary>Show/Hide Font Family</summary>
            <div class="pure-g">

                <div class="pure-u-8-24">
                    <label for="font">Font Family</label>
                    <input type="text" id="font" name="font" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                           value="Arial, Helvetica, sans-serif" class="pure-u-20-24">
                </div>
                <div class="pure-u-4-24">
                    <label for="case">Case</label>
                    <select id="case" name="case" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                            class="pure-input-18-24">
                        <option value="Case.NONE">NONE</option>
                        <option value="Case.UPPER">UPPER</option>
                        <option value="Case.LOWER">LOWER</option>
                        <option value="Case.SENTENCE">SENTENCE</option>
                        <option value="Case.CAPITALIZE">CAPITALIZE</option>
                        <option value="Case.SMALLCAPS">SMALLCAPS</option>
                    </select>
                </div>
                <div class="pure-u-4-24">
                    <label for="spacing">Letter Spacing</label>
                    <input type="text" id="spacing" name="spacing" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                           value="normal" class="pure-u-22-24">
                </div>
            </div>
        </details>

    </fieldset>
    <fieldset>
        <button class="button-success pure-button" data-hx-indicator="#spinner">Submit <img class="htmx-indicator" src="images/bars.svg" alt="busy"/></button>
        <input class="button-secondary pure-button" type="button" onclick="copyTextToClipboard(txt);"
               value="Copy As ColorMap">
        <input class="button-secondary pure-button" type="button" onclick="copyTextToClipboard(panelSource);"
               value="Copy Panel As AsciiDoc">
    </fieldset>
</form>
<div id="errormessages">
</div>
<script>
    $("#color").spectrum({
        color: "#000"
    });
</script>
<div id="contentBox" class="contentBox"></div>
<#include "panelcommon.ftlh">
<@panelsMacro panelgenerator = "pure-button-primary pure-button-active" panelimagebuilder= "" twotoneimagebuilder= "" panelseditor= "" slimpanel=""/>