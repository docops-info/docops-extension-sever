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

package io.docops.docopsextensionssupport.diagram

import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.logging.LogFactory
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.nio.charset.Charset
import kotlin.time.measureTimedValue

@Controller
@RequestMapping("/api/connector")
class BoxyController {

    private val log = LogFactory.getLog(BoxyController::class.java)

    @PutMapping("/")
    @ResponseBody
    @Counted()
    @Timed(value = "docops.boxy.put.html", histogram = true, percentiles = [0.5, 0.95])
    fun makeDiag(httpServletRequest: HttpServletRequest): ResponseEntity<ByteArray> {
        val timings = measureTimedValue {
            var contents = httpServletRequest.getParameter("content")
            var title = "title"
            if (contents.isNullOrEmpty()) {
                contents = StreamUtils.copyToString(httpServletRequest.inputStream, Charset.defaultCharset())
                title = httpServletRequest.getParameter("title")
            }
            val scale = httpServletRequest.getParameter("scale")
            val parser = ConnectorParser()
            val connectors = parser.parse(contents)
            val maker = ConnectorMaker(connectors = connectors)
            val svg = maker.makeConnectorImage(scale = scale.toFloat())
            val headers = HttpHeaders()
            headers.cacheControl = CacheControl.noCache().headerValue
            headers.contentType = MediaType.parseMediaType("text/html")
            val div = """
        <div class="collapse collapse-arrow border-base-300">
            <input type="radio" name="my-accordion-2" checked="checked" />
            <div class="collapse-title text-xl font-small">
                Image
            </div>
            <div class="collapse-content">
                <div id='imageblock'>
                $svg
                </div>
            </div>
        </div>
        <div class="collapse collapse-arrow border-base-300">
            <input type="radio" name="my-accordion-2" />
            <div class="collapse-title text-xl font-small">
                Click to View Source
            </div>
            <div class="collapse-content">
                <h3>Adr Source</h3>
                <div>
                <pre>
                <code class="kotlin">
                 $contents
                </code>
                </pre>
                </div>
                <script>
                var adrSource = `[diag,scale="0.7",role="center"]\n----\n${contents}\n----`;
                document.querySelectorAll('pre code').forEach((el) => {
                    hljs.highlightElement(el);
                });
                </script>
            </div>
        </div>
        """.trimIndent()
            ResponseEntity(div.toByteArray(), headers, HttpStatus.OK)
        }
        log.info("makeDiag executed in ${timings.duration.inWholeMilliseconds}ms ")
        return timings.value
    }
    @GetMapping("/static", produces = ["image/svg+xml"])
    @ResponseBody
    fun getBoxy(): ResponseEntity<String> {
        //language=svg
        return ResponseEntity.ok(
        """
<svg xmlns="http://www.w3.org/2000/svg" width="580" height="550"
     viewBox="0 0 580 550" xmlns:xlink="http://www.w3.org/1999/xlink">

    <defs>
        <marker id="arrowhead" markerWidth="10" markerHeight="7"
                refX="0" refY="3.5" orient="auto">
            <polygon points="0 0, 10 3.5, 0 7" fill-opacity="0.5" />
        </marker>
        <linearGradient id="grad0" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#f0a694"/>
            <stop class="stop2" offset="50%" stop-color="#e8795f"/>
            <stop class="stop3" offset="100%" stop-color="#E14D2A"/>
        </linearGradient>
        <linearGradient id="grad1" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#c0e6a3"/>
            <stop class="stop2" offset="50%" stop-color="#a1d975"/>
            <stop class="stop3" offset="100%" stop-color="#82CD47"/>
        </linearGradient>
        <linearGradient id="grad2" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#b3beff"/>
            <stop class="stop2" offset="50%" stop-color="#8d9eff"/>
            <stop class="stop3" offset="100%" stop-color="#687EFF"/>
        </linearGradient>
        <linearGradient id="grad3" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#df939c"/>
            <stop class="stop2" offset="50%" stop-color="#cf5d6a"/>
            <stop class="stop3" offset="100%" stop-color="#C02739"/>
        </linearGradient>
        <linearGradient id="grad4" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#fee0af"/>
            <stop class="stop2" offset="50%" stop-color="#fed187"/>
            <stop class="stop3" offset="100%" stop-color="#FEC260"/>
        </linearGradient>
        <linearGradient id="grad5" x2="0%" y2="100%">
            <stop class="stop1" offset="0%" stop-color="#f4e9ff"/>
            <stop class="stop2" offset="50%" stop-color="#eedeff"/>
            <stop class="stop3" offset="100%" stop-color="#e9d3ff"/>
        </linearGradient>
        <filter id="filter">
            <feMorphology in="SourceAlpha" operator="dilate" radius="2" result="OUTLINE"/>
            <feComposite operator="out" in="OUTLINE" in2="SourceAlpha"/>
        </filter>
        <filter id="poly" x="0" y="0" width="200%" height="200%">
            <feOffset result="offOut" in="SourceGraphic" dx="10" dy="15" />
            <feGaussianBlur result="blurOut" in="offOut" stdDeviation="5" />
            <feBlend in="SourceGraphic" in2="blurOut" mode="normal" />
        </filter>
        <filter id="shadow2">
            <feDropShadow
                    dx="-0.8"
                    dy="-0.8"
                    stdDeviation="0"
                    flood-color="pink"
                    flood-opacity="0.5" />
        </filter>
        <style>
            .shadowed {
                filter: url(#shadow2);
            }
            .filtered {
                filter: url(#filter);
                fill: black;
                font-family: 'Ultra', serif;
                font-size: 100px;
            }
            .glass:after,.glass:before{content:"";display:block;position:absolute}.glass{overflow:hidden;color:#fff;text-shadow:0
            1px 2px rgba(0,0,0,.7);background-image:radial-gradient(circle at
            center,rgba(0,167,225,.25),rgba(0,110,149,.5));box-shadow:0 5px 10px rgba(0,0,0,.75),inset 0 0 0 2px
            rgba(0,0,0,.3),inset 0 -6px 6px -3px
            rgba(0,129,174,.2);position:relative}.glass:after{background:rgba(0,167,225,.2);z-index:0;height:100%;width:100%;top:0;left:0;backdrop-filter:blur(3px)
            saturate(400%);-webkit-backdrop-filter:blur(3px) saturate(400%)}.glass:before{width:calc(100% -
            4px);height:35px;background-image:linear-gradient(rgba(255,255,255,.7),rgba(255,255,255,0));top:2px;left:2px;border-radius:30px
            30px 200px 200px;opacity:.7}.glass:hover{text-shadow:0 1px 2px
            rgba(0,0,0,.9)}.glass:hover:before{opacity:1}.glass:active{text-shadow:0 0 2px rgba(0,0,0,.9);box-shadow:0
            3px 8px rgba(0,0,0,.75),inset 0 0 0 2px rgba(0,0,0,.3),inset 0 -6px 6px -3px
            rgba(0,129,174,.2)}.glass:active:before{height:25px}
        </style>
    </defs>
    <g transform="translate(0,0)" >
        <rect class="shadowed" x="10" y="10" width="250" height="90" fill="url(#grad0)" ry="5" rx="5" stroke="#FFAC41" stroke-width="1" />
        <line marker-end="url(#arrowhead)" x1="220" y1="100" x2="220" y2="120" stroke="#800000" stroke-width="3" />
        <text x="135" y="65" text-anchor="middle" style="font-size:24px;font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif; font-variant: small-caps" font-weight="bold" class="filtered glass">User</text>
    </g>
    <g transform="translate(0,140)" >
        <rect class="shadowed" x="10" y="10" width="250" height="90" fill="url(#grad1)" ry="5" rx="5" stroke="#FFAC41" stroke-width="1"/>
        <line marker-end="url(#arrowhead)" x1="220" y1="100" x2="220" y2="120" stroke="#800000" stroke-width="3" />
        <line marker-end="url(#arrowhead)" x1="50" y1="10" x2="50" y2="-10" stroke="#800000" stroke-width="3" />
        <text x="135" y="65" text-anchor="middle" style="font-size:24px;font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif; font-variant: small-caps" font-weight="bold" class="filtered glass">Application</text>

    </g>

    <g transform="translate(0,280)">
        <rect  class="shadowed" x="10" y="10" width="250" height="90" fill="url(#grad2)" ry="5" rx="5" stroke="#FFAC41" stroke-width="1"/>
        <line marker-end="url(#arrowhead)" x1="220" y1="100" x2="220" y2="120" stroke="#800000" stroke-width="3" />
        <line marker-end="url(#arrowhead)" x1="50" y1="10" x2="50" y2="-10" stroke="#800000" stroke-width="3" />
        <text x="135" y="65" text-anchor="middle" style="font-size:24px;font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif; font-variant: small-caps" font-weight="bold" class="filtered glass">Operating System</text>

    </g>
    <g transform="translate(0,420)" >
        <rect class="shadowed" x="10" y="10" width="250" height="90" fill="url(#grad3)" ry="5" rx="5" stroke="#FFAC41" stroke-width="1"/>
        <line marker-end="url(#arrowhead)" x1="50" y1="10" x2="50" y2="-10" stroke="#800000" stroke-width="3" />
        <text x="135" y="65" text-anchor="middle" style="font-size:24px;font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif; font-variant: small-caps; fill: #fcfcfc" font-weight="bold" class="filtered glass">Hardware</text>
    </g>
    <g transform="translate(300,0)" >
        <rect class="shadowed" x="10" y="10" width="250" height="90" fill="url(#grad4)" ry="5" rx="5" stroke="#FFAC41" stroke-width="1" />
        <line marker-end="url(#arrowhead)" x1="-40" y1="50" x2="-20" y2="50" stroke="#800000" stroke-width="3" />
        <text x="135" y="65" text-anchor="middle" style="font-size:24px;font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif; font-variant: small-caps" font-weight="bold" class="filtered glass">Chrome</text>
    </g>
    <g transform="translate(300,140)" >
        <rect class="shadowed" x="10" y="10" width="250" height="90" fill="url(#grad5)" ry="5" rx="5" stroke="#FFAC41" stroke-width="1"/>
        <line marker-end="url(#arrowhead)" x1="220" y1="100" x2="220" y2="120" stroke="#800000" stroke-width="3" />
        <line marker-end="url(#arrowhead)" x1="50" y1="10" x2="50" y2="-10" stroke="#800000" stroke-width="3" />
        <text x="135" y="65" text-anchor="middle" style="font-size:24px;font-family: 'Inter var', system-ui, 'Helvetica Neue', Helvetica, Arial, sans-serif; font-variant: small-caps" font-weight="bold" class="filtered glass">Interactive UI</text>

    </g>
</svg>
        """.trimIndent())
    }
}