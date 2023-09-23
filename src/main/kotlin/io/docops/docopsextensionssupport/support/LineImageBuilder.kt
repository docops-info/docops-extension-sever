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



    fun makeLineImage(fillColor: String, fontColor: String, line1: String, line2: String?, line3: String?): String {
        val now = System.currentTimeMillis()
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg id="panelText" width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg"
                 xmlns:xlink="http://www.w3.org/1999/xlink">
                <path id="Color-Fill-$now" fill="$fillColor" stroke="none" d="M 0 0 L 512 0 L 512 512 L 0 512 Z"/>
                <g id="text-$now">
                    <text id="text1-$now" text-anchor="middle">
                        <tspan text-anchor="middle" x="50%" y="42%" font-family="Rubik Mono One" font-size="48" fill="$fontColor">${line1.uppercase()}</tspan>
                        <tspan text-anchor="middle" x="50%" y="54%" font-family="Rubik Mono One" font-size="48" fill="$fontColor">${line2?.uppercase()}</tspan>
                        <tspan text-anchor="middle" x="50%" y="66%" font-family="Rubik Mono One" font-size="48" fill="$fontColor">${line3?.uppercase()}</tspan>
                        </text>
                </g>
                <g id="Dividers">
                    <path id="Bottom-Divider-" fill="none" stroke="#3f4652" stroke-width="2.56" stroke-linecap="round"
                          stroke-linejoin="round" d="M 60.16 362.23999 L 451.839996 362.23999"/>
                    <path id="Top-Divider-" fill="none" stroke="#3f4652" stroke-width="2.56" stroke-linecap="round"
                          stroke-linejoin="round" d="M 60.16 149.76001 L 451.839996 149.76001"/>
                </g>
            </svg>

        """.trimIndent()
    }
