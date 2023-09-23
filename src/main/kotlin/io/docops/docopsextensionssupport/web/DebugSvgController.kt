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

package io.docops.docopsextensionssupport.web

import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.nio.charset.StandardCharsets

@Controller
@RequestMapping("/api/svg")
class DebugSvgController {

    @GetMapping("/debug")
    fun debug(): ResponseEntity<ByteArray> {
         val svg =  """<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN"
        "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<svg width="1200" height="400" viewBox="0 0 600 200" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1">
  <text fill="#000000" font-family="sans-serif" font-size="13" textLength="78" x="14" y="20">You can use</text>
  <a target="_top" xlink:actuate="onRequest" xlink:href="http://plantuml.com/start" xlink:show="new" xlink:title="http://plantuml.com/start" xlink:type="simple">
    <text fill="#0000FF" font-family="sans-serif" font-size="13" text-decoration="underline" textLength="87" x="96" y="20">links in notes</text>
    <line style="stroke: #0000FF; stroke-width: 1.0;" x1="96" x2="183" y1="20" y2="20"/>
  </a>

  <g transform="translate(0 20)">
    <text fill="#000000" font-family="sans-serif" font-size="13" textLength="78" lengthAdjust="spacingAndGlyphs" x="14" y="20">You can use</text>
    <a target="_top" xlink:actuate="onRequest" xlink:href="http://plantuml.com/start" xlink:show="new" xlink:title="http://plantuml.com/start" xlink:type="simple">
      <text fill="#0000FF" font-family="sans-serif" font-size="13" text-decoration="underline" textLength="87" lengthAdjust="spacingAndGlyphs" x="96" y="20">links in notes</text>
      <line style="stroke: #0000FF; stroke-width: 1.0;" x1="96" x2="183" y1="20" y2="20"/>
    </a>
  </g>
</svg>"""

        val headers = HttpHeaders()
        headers.cacheControl = CacheControl.noCache().headerValue
        headers.contentType = MediaType("image", "svg+xml", StandardCharsets.UTF_8)
        return ResponseEntity(svg.toByteArray(StandardCharsets.UTF_8), headers, HttpStatus.OK)
    }
}