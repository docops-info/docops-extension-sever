/*
 * Copyright (c) 2023. The DocOps Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.docops.docopsextensionssupport.support



fun makePanelRoundMiddleImage (fillColor: String, fontColor: String, line1: String, line2: String?, line3: String?): String  {
    val now = System.currentTimeMillis()
    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <svg id="panelText" width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg"
             xmlns:xlink="http://www.w3.org/1999/xlink">
            <path id="Color-Fill" fill="$fillColor" stroke="none" d="M 0 0 L 512 0 L 512 512 L 0 512 Z"/>
            <g id="Logo-$now">
                <path id="Logo-Shape" fill="#020303" fill-rule="evenodd" stroke="none"
                      d="M 426.23999 256 C 426.23999 161.979065 350.020935 85.76001 256 85.76001 C 161.97905 85.76001 85.760002 161.979065 85.760002 256 C 85.760002 350.020935 161.97905 426.23999 256 426.23999 C 350.020935 426.23999 426.23999 350.020935 426.23999 256 Z"/>
                <text id="Beyond-The-Idea" text-anchor="middle">
                    <tspan x="50%" y="42%" font-family="Helvetica Neue" font-size="48" font-stretch="condensed" 
                           font-weight="700" fill="$fontColor">${line1.uppercase()}</tspan>
                    <tspan x="50%" y="54%" font-family="Helvetica Neue" font-size="48" font-stretch="condensed"
                           font-weight="700" fill="$fontColor">${line2?.uppercase()}</tspan>
                    <tspan x="50%" y="66%" font-family="Helvetica Neue" font-size="48" font-stretch="condensed"
                           font-weight="700" fill="$fontColor">${line3?.uppercase()}</tspan>
                </text>
            </g>
        </svg>
    """.trimIndent()
}