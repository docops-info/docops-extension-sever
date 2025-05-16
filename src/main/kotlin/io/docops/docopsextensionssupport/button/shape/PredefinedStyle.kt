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

package io.docops.docopsextensionssupport.button.shape

class PredefinedStyle {
}

fun glass () = """.glass:after,.glass:before{content:"";display:block;position:absolute}.glass{overflow:hidden;color:#fff;text-shadow:0 1px 2px rgba(0,0,0,.7);background-image:radial-gradient(circle at center,rgba(0,167,225,.25),rgba(0,110,149,.5));box-shadow:0 5px 10px rgba(0,0,0,.75),inset 0 0 0 2px rgba(0,0,0,.3),inset 0 -6px 6px -3px rgba(0,129,174,.2);position:relative}.glass:after{background:rgba(0,167,225,.2);z-index:0;height:100%;width:100%;top:0;left:0;backdrop-filter:blur(3px) saturate(400%);-webkit-backdrop-filter:blur(3px) saturate(400%)}.glass:before{width:calc(100% - 4px);height:35px;background-image:linear-gradient(rgba(255,255,255,.7),rgba(255,255,255,0));top:2px;left:2px;border-radius:30px 30px 200px 200px;opacity:.7}.glass:hover{text-shadow:0 1px 2px rgba(0,0,0,.9)}.glass:hover:before{opacity:1}.glass:active{text-shadow:0 0 2px rgba(0,0,0,.9);box-shadow:0 3px 8px rgba(0,0,0,.75),inset 0 0 0 2px rgba(0,0,0,.3),inset 0 -6px 6px -3px rgba(0,129,174,.2)}.glass:active:before{height:25px}"""

fun raise (strokeColor: String = "gold", opacity: Float = 0.9f) = """.raise {pointer-events: bounding-box;opacity: 1;filter: drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4));}.raise:hover {stroke: ${strokeColor};stroke-width: 3px; opacity: ${opacity};}"""

fun baseCard() = """.basecard { -webkit-filter: drop-shadow( 3px 3px 2px rgba(0, 0, 0, .3)); filter: drop-shadow( 3px 3px 2px rgba(0, 0, 0, .3)); } .basecard:hover { cursor: pointer; filter: grayscale(100%) sepia(100%); }"""

fun linkText() = """ .linkText { fill: #000000; font-size: 15px; font-family: "Inter var", system-ui, "Helvetica Neue", Helvetica, Arial, sans-serif; font-weight: normal; cursor: pointer; } .linkText:hover { fill: #ea0606; border: #d2ddec solid; }
    .linkTextDark { fill: #fcfcfc; font-size: 15px; font-family: "Inter var", system-ui, "Helvetica Neue", Helvetica, Arial, sans-serif; font-weight: normal; cursor: pointer; } .linkText:hover { fill: #ea0606; border: #d2ddec solid; }
""".trimMargin()

fun lightShadow() = """.light-shadow {filter: drop-shadow(0px 3px 3px rgba(0, 0, 0, 0.4));}"""
fun myBox() = """.shape { stroke: black; } .mybox:hover { -webkit-animation: 0.5s draw linear forwards; animation: 0.5s draw linear forwards; }"""

fun modernText() = """.modern-text {
  filter: drop-shadow(0px 2px 2px rgba(0, 0, 0, 0.3));
  transition: all 0.3s ease;
}
.modern-text:hover {
  filter: drop-shadow(0px 3px 5px rgba(0, 0, 0, 0.5));
  transform: translateY(-1px);
}"""

fun modernCard() = """.modern-card {
  transition: all 0.3s ease;
  transform-origin: center;
}
.modern-card:hover {
  transform: scale(1.02);
  filter: brightness(1.1);
  cursor: pointer;
}"""

fun naturalShadow() = """<filter id="naturalShadow" x="0" y="0" width="2" height="2"><feOffset in="SourceGraphic" dx="3" dy="3" /><feGaussianBlur stdDeviation="5" result="blur" /><feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge></filter>"""
fun keyFrame() = """ @keyframes draw { 0% { stroke-dasharray: 140 540; stroke-dashoffset: -474; stroke-width: 3px; } 100% { stroke-dasharray: 760; stroke-dashoffset: 0; stroke-width: 5px; } }"""
fun filters() = """
       <filter id="Bevel2" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="0.5" result="blur"/>
            <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut" lighting-color="white">
                <fePointLight x="-5000" y="-10000" z="0000"/>
            </feSpecularLighting>
            <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
            <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
        </filter>
        <filter id="buttonBlur">
        <feGaussianBlur in="SourceAlpha" stdDeviation="2" result="blur"/>
        <feOffset in="blur" dy="2" result="offsetBlur"/>
        <feMerge>
            <feMergeNode in="offsetBlur"/>
            <feMergeNode in="SourceGraphic"/>
        </feMerge>
        </filter>

        <linearGradient id="overlayGrad" gradientUnits="userSpaceOnUse" x1="95" y1="-20" x2="95" y2="70">
            <stop offset="0" stop-color="#000000" stop-opacity="0.5"/>
            <stop offset="1" stop-color="#000000" stop-opacity="0"/>
        </linearGradient>

        <filter id="topshineBlur">
            <feGaussianBlur stdDeviation="0.93"/>
        </filter>

        <linearGradient id="topshineGrad" gradientUnits="userSpaceOnUse" x1="95" y1="0" x2="95" y2="40">
            <stop offset="0" stop-color="#ffffff" stop-opacity="1"/>
            <stop offset="1" stop-color="#ffffff" stop-opacity="0"/>
        </linearGradient>

        <filter id="bottomshine">
            <feGaussianBlur stdDeviation="0.95"/>
        </filter>

        <filter id="modernShadow" x="-10%" y="-10%" width="120%" height="120%">
            <feDropShadow dx="2" dy="2" stdDeviation="3" flood-color="rgba(0,0,0,0.3)" />
            <feGaussianBlur stdDeviation="1" result="blur" />
            <feFlood flood-color="rgba(255,255,255,0.1)" result="floodFill" />
            <feComposite in="floodFill" in2="blur" operator="in" result="coloredBlur" />
            <feComposite in="SourceGraphic" in2="coloredBlur" operator="over" />
        </filter>

        <filter id="softGlow" x="-10%" y="-10%" width="120%" height="120%">
            <feGaussianBlur stdDeviation="2" result="blur" />
            <feFlood flood-color="rgba(255,255,255,0.3)" result="glow" />
            <feComposite in="glow" in2="blur" operator="in" result="softGlow" />
            <feComposite in="SourceGraphic" in2="softGlow" operator="over" />
        </filter>

        <filter id="Bevel2" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="0.5" result="blur"/>
            <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut" lighting-color="white">
                <fePointLight x="-5000" y="-10000" z="0000"/>
            </feSpecularLighting>
            <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
            <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint" />
        </filter>
        <circle id="myCircle" cx="0" cy="0" r="60" class="card" stroke-width="1"/>
        """

fun DARK1() = mutableListOf("#080808", "#101010", "#181818","#202020","#282828", "#303030")

fun DARKREDLIGHT() = mutableListOf("#080000", "#100000", "#180000", "#200000", "#280000", "#300000", "#380000", "#400000")

fun REDLIGHT() = mutableListOf("#780000","#800000", "#880000","#900000", "#980000", "#A00000", "#A80000")

fun modernGradients() = """
    <linearGradient id="modernBlueGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#4facfe" />
        <stop offset="100%" stop-color="#00f2fe" />
    </linearGradient>
    <linearGradient id="modernPurpleGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#6a11cb" />
        <stop offset="100%" stop-color="#2575fc" />
    </linearGradient>
    <linearGradient id="modernGreenGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#11998e" />
        <stop offset="100%" stop-color="#38ef7d" />
    </linearGradient>
    <linearGradient id="modernOrangeGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#f6d365" />
        <stop offset="100%" stop-color="#fda085" />
    </linearGradient>
    <linearGradient id="modernRedGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#ff512f" />
        <stop offset="100%" stop-color="#dd2476" />
    </linearGradient>
"""

fun uses() =  """
            <path id="outerBox" fill="#ffffff"  d="M 0 18.0 A 18.0 18.0 0 0 1 18.0 0 L 282.0 0 A 18.0 18.0 0 0 1 300.0 18.0 L 300.0 382.0 A 18.0 18.0 0 0 1 282.0 400.0 L 18.0 400.0 A 18.0 18.0 0 0 1 0 382.0 Z">
                        <title>Title</title>
                    </path>
            <g id="topTextBox">
                <path fill="#ffffff" d="M 0 18.0 A 18.0 18.0 0 0 1 18.0 0 L 282.0 0 A 18.0 18.0 0 0 1 300.0 18.0 L 300.0 95.5 A 0.0 0.0 0 0 1 300.0 95.5 L 0.0 95.5 A 0.0 0.0 0 0 1 0 95.5 Z"/>
            </g>
            <path id="bottomTextBox" d="M 0 0.0 A 0.0 0.0 0 0 1 0.0 0 L 300.0 0 A 0.0 0.0 0 0 1 300.0 0.0 L 300.0 95.5 A 0.0 0.0 0 0 1 300.0 95.5 L 0.0 95.5 A 0.0 0.0 0 0 1 0 95.5 Z"/>
            <path id="singleBox" d="M 0 18.0 A 18.0 18.0 0 0 1 18.0 0 L 282.0 0 A 18.0 18.0 0 0 1 300.0 18.0 L 300.0 191.0 A 0.0 0.0 0 0 1 300.0 191.0 L 0.0 191.0 A 0.0 0.0 0 0 1 0 191.0 Z"/>
        """
