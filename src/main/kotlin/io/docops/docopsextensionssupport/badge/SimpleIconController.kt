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

package io.docops.docopsextensionssupport.badge

import io.micrometer.core.annotation.Timed
import io.micrometer.observation.annotation.Observed
import org.silentsoft.simpleicons.SimpleIcons
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.RegexPatternTypeFilter
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.util.*
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


@Controller
@RequestMapping("/api")
@Observed(name = "simpleicon.controller")
class SimpleIconController {
    private val allClasses = findAllClassesUsingClassLoader("org.silentsoft.simpleicons.icons").sortedBy { it.beanClassName }

    private final fun findAllClassesUsingClassLoader(packageName: String): MutableSet<BeanDefinition> {
        val provider = ClassPathScanningCandidateComponentProvider(false)
        provider.addIncludeFilter(RegexPatternTypeFilter(Pattern.compile(".*")))
        return provider.findCandidateComponents(packageName)
    }
    @RequestMapping("/simple/icons", method = [RequestMethod.GET, RequestMethod.POST])
    @Timed(value = "docops.simpleicons", histogram = true, percentiles = [0.5, 0.95])
    fun getByLetter(@RequestParam(name = "SEL") selected : String,  model: Model) : String {
        val list = allClasses.filter { it.beanClassName!!.startsWith("org.silentsoft.simpleicons.icons.$selected") }.map { it.beanClassName?.replace("org.silentsoft.simpleicons.icons.", "") }.map { it?.replace("Icon", "") }
        model.addAttribute("icons", list.chunked(7))
        model.addAttribute("letter", selected)
        return "icons/iconsbyletter"
    }


    @GetMapping("/simple/icon")
    @Timed(value = "docops.simpleicon", histogram = true, percentiles = [0.5, 0.95])
    fun showIcon(@RequestParam(name = "iconName") iconName: String, model: Model): String {
        val ico = SimpleIcons.get(iconName)
        val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(ico.svg.toByteArray()))
        val src  =manipulateSVG(xml, ico.hex)
        // val src = "data:image/svg+xml;base64," + Base64.getEncoder()
        //    .encodeToString(ico.svg.toByteArray())

        model.addAttribute("iconName", "<$iconName>")
        model.addAttribute("src", src)
        return "icons/showicon"

    }


}
fun manipulateSVG(document: Document, value: String): String {
    val elem = document.documentElement
    elem.setAttribute("fill", "#$value")
    elem.setAttribute("width", "100px")
    elem.setAttribute("height", "100px")
    val transformer: Transformer = TransformerFactory.newInstance().newTransformer().apply {
        setOutputProperty(OutputKeys.INDENT, "yes")
        setOutputProperty(OutputKeys.METHOD, "xml")
        setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5")
    }
    val source = DOMSource(document)
    val writer = StringWriter()
    val result = StreamResult(writer)
    transformer.transform(source, result)
    return writer.toString()
}