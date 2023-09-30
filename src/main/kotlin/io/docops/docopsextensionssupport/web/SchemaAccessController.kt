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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.nio.charset.Charset

@Controller
@RequestMapping("/json/schema")
class SchemaAccessController @Autowired constructor(private val applicationContext: ApplicationContext) {


    @GetMapping("", produces = [MediaType.TEXT_HTML_VALUE])
    fun schema(@RequestParam(name = "name", required = true) name: String): ResponseEntity<String> {
        try {
            val json =  applicationContext.getResource("classpath:static/schemas/$name.json")
            //language=html
            return ResponseEntity("""<div>
                    Url: <a class="link link-info" href="schemas/$name.json" target="_blank">schemas/$name.json</a>
                    <pre><code id="dataView">${json.getContentAsString(Charset.defaultCharset())}</code></pre>
                
                </div>
                <script>
                document.querySelectorAll('pre code').forEach((el) => {
                    hljs.highlightElement(el);
                });
                </script>
                """.trimMargin(), HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}