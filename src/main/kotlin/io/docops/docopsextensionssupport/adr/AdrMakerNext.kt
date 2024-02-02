package io.docops.docopsextensionssupport.adr

import io.docops.docopsextensionssupport.adr.model.Adr
import io.docops.docopsextensionssupport.adr.model.Status
import io.docops.docopsextensionssupport.support.hexToHsl

class AdrMakerNext {
    private val xIndent = 66

    fun makeAdrSvg(adr: Adr, dropShadow: Boolean = true, config: AdrParserConfig) : String {
        val width = 800
        val sb = StringBuilder()

        sb.append(title(adr.title, width))
        sb.append(status(adr, config))
        sb.append("""<text x="14" y="85" style="font-weight: normal; font-size: 14px;">""")
        sb.append(context(adr,config))
        sb.append(decision(adr,config))
        sb.append(consequences(adr,config))
        sb.append(participants(adr,config))
        sb.append("""</text>""")
        var iHeight = 550
        val count = adr.lineCount() - 15
        if(count>0) {
            iHeight += (count * 15)
        }
        return svg(sb.toString(), iWidth = width,  iHeight = iHeight, adr = adr, config=config)
    }

    fun title(title: String, width: Int): String {
        return  """
    <text x="${width/2}" y="30" text-anchor="middle" fill="#fcfcfc"  class="filtered glass boxText"  style="font-weight: bold; font-size: 24px;">$title</text>
         """.trimIndent()
    }

    fun status(adr: Adr, adrParserConfig: AdrParserConfig): String {
        //language=svg
        return """
            <text x="20" y="55" style="font-size: 18px;fill: #000000; font-variant: small-caps; font-weight: bold;">Status:</text>
            <text x="85" y="54" style="font-weight: normal; font-size: 14px;" fill="#fcfcfc">${adr.status}</text>
            <text x="200" y="55" style="font-size: 18px;fill: #000000; font-variant: small-caps; font-weight: bold;">Date:</text>
            <text x="250" y="54" style="font-size: 14px;" fill="#fcfcfc">${adr.date}</text>
        """
    }

    fun context(adr: Adr, config: AdrParserConfig): StringBuilder {
        val sb = StringBuilder("""<tspan x="14" dy="20" style="font-size: 18px;fill: #512B81; font-variant: small-caps; font-weight: bold;"  text-decoration="underline">Context</tspan>""")
        adr.context.forEach {  s ->
            if(s.isEmpty()) {
                sb.append("""<tspan x="14" dy="20">&#160;</tspan>""")
            } else {
                sb.append("""<tspan x="14" dy="20">$s</tspan>""")
            }
        }
        return sb
    }
    fun decision(adr: Adr, config: AdrParserConfig): StringBuilder {
        val sb = StringBuilder("""<tspan x="14" dy="20"  style="font-size: 18px;fill: #512B81; font-variant: small-caps; font-weight: bold;"  text-decoration="underline">Decision</tspan>""")
        adr.decision.forEach {  s ->
            if(s.isEmpty()) {
                sb.append("""<tspan x="14" dy="20">&#160;</tspan>""")
            } else {
                sb.append("""<tspan x="14" dy="20">$s</tspan>""")
            }
        }
        return sb
    }
    fun consequences(adr: Adr, config: AdrParserConfig): StringBuilder {
        val sb = StringBuilder("""<tspan x="14" dy="20" style="font-size: 18px;fill: #512B81; font-variant: small-caps; font-weight: bold;"  text-decoration="underline">Consequences</tspan>""")
        adr.consequences.forEach {  s ->
            if(s.isEmpty()) {
                sb.append("""<tspan x="14" dy="20">&#160;</tspan>""")
            } else {
                sb.append("""<tspan x="14" dy="20">$s</tspan>""")
            }
        }
        return sb
    }
    fun participants(adr: Adr, config: AdrParserConfig): StringBuilder {
        val sb = StringBuilder("""<tspan x="14" dy="20"  style="font-size: 18px;fill: #512B81; font-variant: small-caps; font-weight: bold;"  text-decoration="underline">Participants</tspan>""")
        adr.participants.forEach {  s ->
            sb.append("""<tspan x="14" dy="20">$s</tspan>""")
        }
        return sb
    }
    fun bgColorMap(adr: Adr): String {
        when {
            Status.Proposed == adr.status -> return "#e0edfc"
            Status.Accepted == adr.status -> return "#dbf1e1"
            Status.Superseded == adr.status -> return "#fde29c"
            Status.Deprecated == adr.status -> return "#ffe4e4"
            Status.Rejected == adr.status -> return "#fcc7c9"
        }
        return "#fcfcfc"
    }
    fun svg(body: String, iHeight: Int = 550, iWidth: Int, adr: Adr, config: AdrParserConfig): String {
        val height = maxOf(iHeight, 500)
        val width = 800
        val prop = hexToHsl("#2986cc")
        //language=svg
        return """
<?xml version="1.0" standalone="no"?>
<svg id="adr" xmlns="http://www.w3.org/2000/svg" width='${width}' height='${height}'
     xmlns:xlink="http://www.w3.org/1999/xlink" font-family="arial" viewBox="0 0 ${(width)} ${height}"
     >
    <defs>
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Proposed-gradient" x2="0%" y2="100%">
            <stop offset="0%" stop-color="${hexToHsl("#5ea4d8")}"/>
            <stop offset="100%" stop-color="$prop"/>
        </linearGradient>
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Accepted-gradient" x2="0%" y2="100%">
            <stop offset="0%" stop-color="${hexToHsl("#699855")}"/>
            <stop offset="100%" stop-color="${hexToHsl("#38761d")}"/>
        </linearGradient>
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Superseded-gradient" x2="0%" y2="100%">
            <stop offset="0%" stop-color="${hexToHsl("#f7d272")}"/>
            <stop offset="100%" stop-color="${hexToHsl("#F5C344")}"/>
        </linearGradient>        
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Deprecated-gradient" x2="0%" y2="100%">
            <stop offset="0%" stop-color="${hexToHsl("#efb2b2")}"/>
            <stop offset="100%" stop-color="${hexToHsl("#EA9999")}"/>
        </linearGradient>        
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Rejected-gradient" x2="0%" y2="100%">
            <stop offset="0%" stop-color="${hexToHsl("#d87277")}"/>
            <stop offset="100%" stop-color="${hexToHsl("#CB444A")}"/>
        </linearGradient>
        <filter id="dropshadow" height="130%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="3"/> <!-- stdDeviation is how much to blur -->
            <feOffset dx="2" dy="2" result="offsetblur"/> <!-- how much to offset -->
            <feComponentTransfer>
                <feFuncA type="linear" slope="0.5"/> <!-- slope is the opacity of the shadow -->
            </feComponentTransfer>
            <feMerge>
                <feMergeNode/> <!-- this contains the offset blurred image -->
                <feMergeNode in="SourceGraphic"/> <!-- this contains the element that the filter is applied to -->
            </feMerge>
        </filter>
<filter xmlns="http://www.w3.org/2000/svg" id="MyFilter">
            <feGaussianBlur in="SourceAlpha" stdDeviation="4" result="blur"/>
            <feOffset in="blur" dx="4" dy="4" result="offsetBlur"/>
            <feSpecularLighting in="blur" surfaceScale="5" specularConstant="1" specularExponent="10"
                                lighting-color="white" result="specOut">
                <fePointLight x="-5000" y="-10000" z="20000"/>
            </feSpecularLighting>
            <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut"/>
            <feComposite in="SourceGraphic" in2="specOut" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                         result="litPaint"/>
            <feMerge>
                <feMergeNode in="offsetBlur"/>
                <feMergeNode in="litPaint"/>
            </feMerge>
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
        <filter id="Bevel2" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="0.5" result="blur"/>
            <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut"
                                lighting-color="white">
                <fePointLight x="-5000" y="-10000" z="0000"/>
            </feSpecularLighting>
            <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
            <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                         result="litPaint"/>
        </filter>
        <filter id="filter">
            <feMorphology in="SourceAlpha" operator="dilate" radius="2" result="OUTLINE"/>
            <feComposite operator="out" in="OUTLINE" in2="SourceAlpha"/>
        </filter>
    </defs>
    <style>
    .adrlink { fill: blue; text-decoration: underline; }
    .adrlink:hover, .adrlink:active { outline: dotted 1px blue; }
        
    ${glassStyle()}
    .boxText {
        font-size: 24px;
        font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif;
        font-variant: small-caps;
        font-weight: bold;
    }
    .filtered {
        filter: url(#filter);
        fill: #fcfcfc;
        font-family: 'Ultra', serif;
        font-size: 100px;
    }
    </style>
    <g transform='translate(5,5),scale(${config.scale})'>
    <rect width="100%" height="100%"  fill="${bgColorMap(adr)}" stroke-width="7" fill-opacity='0.4'/>

    <rect fill="url(#${adr.status}-gradient)" height="70" width="100%"/>
    
    $body
    </g>
</svg>
        """.trimIndent()
    }

    fun glassStyle() = """
        .glass {
            overflow: hidden;
            color: white;
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.7);
            background-image: radial-gradient(circle at center, rgba(0, 167, 225, 0.25), rgba(0, 110, 149, 0.5));
            box-shadow: 0 5px 10px rgba(0, 0, 0, 0.75), inset 0 0 0 2px rgba(0, 0, 0, 0.3), inset 0 -6px 6px -3px rgba(0, 129, 174, 0.2);
            position: relative;
        }

        .glass:after {
            content: "";
            background: rgba(0, 167, 225, 0.2);
            display: block;
            position: absolute;
            z-index: 0;
            height: 100%;
            width: 100%;
            top: 0;
            left: 0;
            backdrop-filter: blur(3px) saturate(400%);
            -webkit-backdrop-filter: blur(3px) saturate(400%);
        }

        .glass:before {
            content: "";
            display: block;
            position: absolute;
            width: calc(100% - 4px);
            height: 35px;
            background-image: linear-gradient(rgba(255, 255, 255, 0.7), rgba(255, 255, 255, 0));
            top: 2px;
            left: 2px;
            border-radius: 30px 30px 200px 200px;
            opacity: 0.7;
        }

        .glass:hover {
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.9);
        }

        .glass:hover:before {
            opacity: 1;
        }

        .glass:active {
            text-shadow: 0 0 2px rgba(0, 0, 0, 0.9);
            box-shadow: 0 3px 8px rgba(0, 0, 0, 0.75), inset 0 0 0 2px rgba(0, 0, 0, 0.3), inset 0 -6px 6px -3px rgba(0, 129, 174, 0.2);
        }

        .glass:active:before {
            height: 25px;
        }
    """.trimIndent()
}