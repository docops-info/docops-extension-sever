<H1>Panel Generator With Generated Colors</H1>

<legend>Panel Generator Controls</legend>

<form data-hx-put="api/colorgen" data-hx-target="#contentBox">
    <fieldset>
        <legend>Buttons</legend>
        <div>
            <div class="join">
                <label for="points" class="btn join-item"># Of Buttons <span id="pointsValue"></span></label>
                <input type="number" id="points" name="points" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                       value="6" step="1" max="20" min="2" class="join-item input input-bordered input-primary w-full max-w-xs">
            </div>
            <div class="join">
                <label for="columns" class="btn join-item"># Of Columns <span id="colsValue"></span></label>
                <input type="number" id="columns" name="columns" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                       value="3" step="1" max="15" min="1" class="join-item input input-bordered input-primary w-full max-w-xs">
            </div>
        </div>
    </fieldset>
    <fieldset>
        <legend>Button Type & Sorting</legend>
        <details>
            <summary>Show/Hide</summary>
            <div>
                <div class="join">
                    <label for="buttonType" class="btn join-item">Button Type</label>
                    <select id="buttonType" name="buttonType" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                            class="join-item select select-primary w-full max-w-xs">
                        <option value="BUTTON">BUTTON</option>
                        <option value="ROUND">ROUND</option>
                        <option value="SLIM_CARD">SLIM</option>
                        <option value="LARGE_CARD">LARGE</option>
                        <option value="RECTANGLE">RECTANGLE</option>
                        <option value="PILL">PILL</option>
                    </select>
                </div>

                <div class="join">
                    <label for="sortBy" class="btn join-item">Sort By</label>
                    <select id="sortBy" name="sortBy" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                            class="join-item select select-primary w-full max-w-xs">
                        <option value="Grouping.ORDER">ORDER</option>
                        <option value="Grouping.TYPE">TYPE</option>
                        <option value="Grouping.TITLE">TITLE</option>
                        <option value="Grouping.AUTHOR">AUTHOR</option>
                        <option value="Grouping.DATE">DATE</option>
                    </select>
                </div>
                <div class="join">
                    <label for="order" class="btn join-item">Order</label>
                    <select id="order" name="order" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                            class="join-item select select-primary w-full max-w-xs">
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
            <div class="join">
                <label for="dropShadow" class="btn join-item">Drop Shadow <span id="shadowValue"></span></label>
                <input type="number" id="dropShadow" name="dropShadow" data-hx-put="api/colorgen"
                       data-hx-target="#contentBox" value="0" step="1" max="9" min="0" class="join-item input input-bordered input-primary w-full max-w-xs">
            </div>
            <div class="join">
                <label for="newWin" class="btn join-item">Open In New Window</label>
                <input type="checkbox" id="newWin" name="newWin" data-hx-put="api/colorgen"
                       data-hx-target="#contentBox" class="join-item toggle toggle-primary">
            </div>
        </details>

    </fieldset>
    <fieldset>
        <legend>Font</legend>
        <details>
            <summary>Show/Hide</summary>
            <div>
                <div class="btn" role="group" aria-label="bold italic underline">
                    <ul>
                        <li class="btn" id="boldI" data-toggle-id="bold" >
                            <input type="checkbox" id="bold" name="bold" style="display: none">
                            <label class="label-checkbox" for="bold"><i  class="fa fa-bold"></i></label>
                        </li>
                        <li class="btn" id="italicI" data-toggle-id="italic">
                            <input type="checkbox" id="italic" name="italic" style="display: none">
                            <label for="italic"><i class="fa fa-italic"></i></label>
                        </li>
                        <li class="btn" id="underlineI" data-toggle-id="underline">
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
            <div>
                <div class="join">
                    <label for="size" class="btn join-item">Font Size <span id="fSize"></span></label>
                        <input type="number" id="size" name="size" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                               value="8" step="1" max="36" min="0" class="join-item input input-bordered input-primary w-full max-w-xs">
                        <label for="fpoint" class="btn join-item"></label>
                        <select id="fpoint" name="fpoint" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                                class="join-item select select-primary w-full max-w-xs">
                            <option value="pt">pt</option>
                            <option value="px">px</option>
                            <option value="in">in</option>
                            <option value="pc">pc</option>
                            <option value="mm">mm</option>
                            <option value="cm">cm</option>
                        </select>
                    <label for="color" class="btn join-item">Color</label>
                    <input type="text" id="color" name="color" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                           value="#000000" class="join-item input input-bordered input-primary w-full max-w-xs">
                </div>
            </div>
        </details>
    </fieldset>
    <fieldset>
        <legend>Font Family</legend>
        <details>
            <summary>Show/Hide Font Family</summary>
            <div>

                <div class="join">
                    <label for="font" class="btn join-item">Font Family</label>
                    <input type="text" id="font" name="font" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                           value="Arial, Helvetica, sans-serif" class="join-item">
                </div>
                <div class="join">
                    <label for="case" class="btn join-item">Case</label>
                    <select id="case" name="case" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                            class="join-item select select-primary w-full max-w-xs">
                        <option value="Case.NONE">NONE</option>
                        <option value="Case.UPPER">UPPER</option>
                        <option value="Case.LOWER">LOWER</option>
                        <option value="Case.SENTENCE">SENTENCE</option>
                        <option value="Case.CAPITALIZE">CAPITALIZE</option>
                        <option value="Case.SMALLCAPS">SMALLCAPS</option>
                    </select>
                </div>
                <div class="join">
                    <label for="spacing" class="btn join-item">Letter Spacing</label>
                    <input type="text" id="spacing" name="spacing" data-hx-put="api/colorgen" data-hx-target="#contentBox"
                           value="normal" class="join-item input input-bordered input-primary w-full max-w-xs">
                </div>
            </div>
        </details>

    </fieldset>
    <fieldset>
        <button class="btn btn-success" data-hx-indicator="#spinner">Submit <img class="htmx-indicator" src="images/bars.svg" alt="busy"/></button>
        <input class="btn btn-secondary" type="button" onclick="copyTextToClipboard(txt);"
               value="Copy As ColorMap">
        <input class="btn btn-secondary" type="button" onclick="copyTextToClipboard(panelSource);"
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
<div class="divider"></div>
<div id="contentBox" class="contentBox"></div>
<#include "panelcommon.ftlh">
<@panelsMacro panelgenerator = "btn btn-outline btn-active" panelimagebuilder= "" twotoneimagebuilder= "" panelseditor= "" slimpanel=""/>
