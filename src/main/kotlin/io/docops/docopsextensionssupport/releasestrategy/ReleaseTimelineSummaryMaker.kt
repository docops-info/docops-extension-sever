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

package io.docops.docopsextensionssupport.releasestrategy

import io.docops.asciidoc.utils.escapeXml
import io.docops.docopsextensionssupport.roadmap.linesToUrlIfExist
import io.docops.docopsextensionssupport.roadmap.wrapText
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

/**
 * This class represents a Release Timeline Summary Maker.
 * It extends the ReleaseTimelineMaker class.
 * The ReleaseTimelineSummaryMaker class is responsible for generating a summary of the release timeline
 * based on the given release strategy.
 */
class ReleaseTimelineSummaryMaker : ReleaseTimelineMaker() {

     /**
      * Generates a SVG string representation of a document using the given release strategy.
      *
      * @param releaseStrategy The release strategy to use for generating the document.
      * @param isPdf Specifies whether the document format is PDF.
      * @return The SVG string representation of the generated document.
      */
     override fun make(releaseStrategy: ReleaseStrategy, isPdf: Boolean) : String{
        val width = determineWidth(releaseStrategy = releaseStrategy)
        val id = UUID.randomUUID().toString()
        val str = StringBuilder(head(
                width,
                id,
                title = releaseStrategy.title,
                releaseStrategy.scale,
                releaseStrategy
            ))
        str.append(defs(isPdf, id,  releaseStrategy.scale, releaseStrategy))
         var titleFill = "#000000"
         if(releaseStrategy.useDark) {
             titleFill = "#fcfcfc"
         }
        str.append(title(releaseStrategy.title, width, titleFill))
        releaseStrategy.releases.forEachIndexed { index, release ->
            str.append(buildReleaseItem(release,index, isPdf, id, releaseStrategy))
            str.append(buildReleaseItemHidden(release,index, isPdf, id, releaseStrategy))
        }

        str.append("</g>")
        str.append(tail())
        return str.toString()
    }

    private fun head(width: Float, id: String, title: String, scale: Float, releaseStrategy: ReleaseStrategy) : String{

        val height = (270  + 215 + releaseStrategy.maxLinesForHeight())* scale
        //language=svg
        return """
            <svg width="$width" height="$height" viewBox='0 0 $width $height' xmlns='http://www.w3.org/2000/svg' xmlns:xlink="http://www.w3.org/1999/xlink" role='img'
            aria-label='Docops: Release Strategy' id="ID$id">
            <desc>https://docops.io/extension</desc>
            <title>${title.escapeXml()}</title>
        """.trimIndent()
    }

    fun buildReleaseItem(release: Release, currentIndex: Int, isPdf: Boolean, id: String, releaseStrategy: ReleaseStrategy): String {
        var startX = 0
        if (currentIndex > 0) {
            startX = currentIndex * 425 -(20*currentIndex)
        }
        val lineText = StringBuilder()
        var lineStart = 25
        release.lines.forEachIndexed { index, s ->
            lineText.append(
                """
                <tspan x="$lineStart" dy="10" class="entry" font-size="12px" font-weight="normal"
                   font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" text-anchor="start">- ${s.escapeXml()}</tspan>
            """.trimIndent()
            )
            if (index <= 7) {
                lineStart += 10
            } else {
                lineStart -= 10
            }
        }
        val goals = release.goal.escapeXml()
        val lines = linesToUrlIfExist(wrapText(goals, 20F), mutableMapOf())
        val spans = linesToSpanText(lines,24, 150)
        val textY = 88 - (lines.size * 12)
        var positionX = startX
        if(currentIndex>0) {
            positionX += currentIndex * 5
        }
        var completed = ""
        if(release.completed) {
            completed = "<use xlink:href=\"#completedCheck\" x=\"405\" y=\"65\" width=\"24\" height=\"24\"/>"
        }
        //language=svg
        return """
         <g transform="translate(${positionX+10},60)" class="${shadeColor(release)}">
             <text text-anchor="middle" x="250" y="-12" class="milestoneTL">${release.date}</text>
             <path d="m 0,0 h 400 v 200 h -400 l 0,0 l 100,-100 z" stroke="${fishTailColor(release, releaseStrategy)}" fill="url(#${shadeColor(release)}_rect)"/>
             <path d="m 400,0 v 200 l 100,-100 z" fill="url(#${shadeColor(release)}_rect)" stroke="${fishTailColor(release, releaseStrategy)}" />
            <text x="410" y="110" class="milestoneTL" font-size="36px" fill="${releaseStrategy.displayConfig.fontColor}">${release.type}</text>
            $completed
            <g transform="translate(100,0)" cursor="pointer" onclick="strategyShowItem('ID${id}_${currentIndex}')">
                <rect x="0" y="0" height="200" width="300" fill="url(#${shadeColor(release)}_rect)" class="raise"/>
                <text text-anchor="middle" x="150" y="$textY" class="milestoneTL lines" font-size="12px"
                      font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" font-weight="bold" fill="${releaseStrategy.displayConfig.fontColor}">
                   $spans
                </text>
            </g>
        </g>
        """.trimIndent()
    }
    fun buildReleaseItemHidden(release: Release, currentIndex: Int, isPdf: Boolean, id: String, releaseStrategy: ReleaseStrategy): String {
            var startX = 0
            if (currentIndex > 0) {
                startX = currentIndex * 425 -(20*currentIndex)
            }
            val lineText = StringBuilder()
            var lineStart = 2
        val newLines = releaseStrategy.releaseLinesToDisplay(release.lines)
        newLines.forEachIndexed { index, s ->
            var bullet = ""
            if(s is BulletLine) {
                bullet = "- "
                lineStart = 2
            } else {
                lineStart = 4
            }
                lineText.append(
                    """
                <tspan x="$lineStart" dy="12" class="entry" font-size="12px" font-weight="normal"
                   font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" text-anchor="start">$bullet ${s.text.escapeXml()}</tspan>
            """.trimIndent()
                )

            }
            var x = 200
            var anchor = "text-anchor=\"middle\""
            if (isPdf) {
                x = 10
                anchor = ""
            }
        val height = (newLines.size+1) * 12
        var positionX = startX
        if(currentIndex>0) {
            positionX += currentIndex * 5
        }
        var visibility = """"""
        if(isPdf) {
            visibility = ""
        }
            //language=svg
            return """
         <g transform="translate(${positionX+10},275)" class="${shadeColor(release)}" $visibility id="ID${id}_${currentIndex}">
            <rect width='400' height='$height' stroke="${fishTailColor(release, releaseStrategy)}" fill="#fcfcfc"/>
            <text $anchor x="$x" y="2" class="milestoneTL lines" font-size="12px" font-family='Arial, "Helvetica Neue", Helvetica, sans-serif' font-weight="bold">
                $lineText
            </text>
        </g>
        """.trimIndent()
    }

    private fun linesToSpanText(lines: MutableList<String>, dy: Int, x: Int): String {
        val text = StringBuilder()
        lines.forEach {
            text.append("""<tspan x="$x" dy="$dy" text-anchor="middle" font-family="Arial, 'Helvetica Neue', Helvetica, sans-serif" font-size="24" font-weight="normal">$it</tspan>""")
        }
        return text.toString()
    }


}

fun main() {
    val data = """
        {
            "title": "Release Strategy Builder",
            "releases": [
                {
                    "type": "M1",
                    "lines": [
                        "Reason for ",
                        "Improve Sanitize Sensitive Values section in reference documentation #39200",
                        "Update CRaC support status link #39173",
                        "Fix link to Log4j2's JDK logging adapter documentation #39172",
                        "Document virtual threads limitations #39169",
                        "Polish reference documentation #39157",
                        "Use the term tags in documentation consistently #39152",
                        "Update links to Micrometer docs in metrics section of reference docs #39150",
                        "Remove entry for OCI starter as it is no longer maintained #39145",
                        "Correct the documentation on injecting dependencies into FailureAnalyzer implementations #39101",
                        "Fix typos #38983"
                    ],
                    "date": "TBD",
                    "selected": true,
                    "goal": "Our goal is to build a better cog..."
                },
                {
                    "type": "RC1",
                    "lines": [
                        "Auto-configure TypeDefinitionConfigurer beans for GraphQL apps #39118",
                        "Create multiple registrations for beans that implement multiple Servlet API contracts #39056",
                        "Remove APIs that were deprecated for removal in 3.3 #39039",
                        "Remove dependency management for Dropwizard Metrics #39034",
                        "Add configuration property spring.task.execution.pool.shutdown.accept-tasks-after-context-close #38968",
                        "Autoconfigure Undertow/XNIO for virtual thread support #38819",
                        "Add client-id and subscription-durable properties for JMS connections #38817",
                        "Add property for maximum number of reactive sessions #38703",
                        "Add support for the @SpanTag annotation #38662",
                        "Add configuration option for path inclusion in DefaultErrorAttributes #38619",
                        "Add configuration properties for cluster-level failover with Apache Pulsar #38559",
                        "Change Health.down(Exception) factory method to Health.down(Throwable), aligning with Health.Builder.down(Throwable) #38550",
                        "Make spring.config.activate.on-cloud-platform=none match when the current cloud platform is null #38510",
                        "Add ProcessInfoContributor #38371",
                        "Add possibility to configure a custom ExecutionContextSerializer in BatchAutoConfiguration #38328",
                        "Remove deprecated support for FailureAnalyzer setter injection #38322",
                        "Use unknown_service as default application name for OpenTelemetry #38219",
                        "Auto-configure a JwtAuthenticationConverter #38105",
                        "Fail configuration property metadata processing when additional metadata has unexpected content #37597",
                        "Add local and tag correlation fields #37435",
                        "Use request.requestPath().value() to populate path error attribute with WebFlux #37269",
                        "Improve log messages to use the singular or plural forms instead of 'noun(s)', #37017",
                        "Add observation-enabled properties for RabbitMQ #36451",
                        "Make WebServers' started log messages more consistent #36149",
                        "Add property to configure the queue size for Tomcat #36087"
                    ],
                    "date": "TBD",
                    "selected": true,
                    "goal": "Our goal is ..."
                },
                {
                    "type": "GA",
                    "lines": [
                        "Even when spring.security.user.name or spring.security.user.password has been configured, user details auto-configuration still backs off when resource server is on the classpath #39239",
                        "JarEntry.getComment() returns incorrect result from NestedJarFile instances #39226",
                        "Oracle OJDBC BOM version is flagged not for production use #39225",
                        "MockRestServiceServerAutoConfiguration with RestTemplate and RestClient together throws incorrect exception #39198",
                        "SslBundle implementations do not provide useful toString() results #39168",
                        "Mixing PEM and JKS certificate material in server.ssl properties does not work #39159",
                        "Containers are not started when using @ImportTestcontainers #39151",
                        "Having AspectJ and Micrometer on the classpath is not a strong enough signal to enable support for Micrometer observation annotations #39132",
                        "Actuator endpoints with no operations that use selectors are not accessible when mapped to / #39123",
                        "spring-boot-maven-plugin repackage uber jar execution fails when jar is put on WSL network drive #39121",
                        "Spring Boot 3.2 app that uses WebFlux, Security, and Actuator may fail to start due to a missing authentication manager #39117",
                        "@ConfigurationPropertiesBinding converters that rely on initial CharSequence to String conversion no longer work #39115",
                        "management.observations.http.server.requests.name no longer has any effect #39106",
                        "Configuring server.jetty.max-connections has no effect #39080",
                        "spring.rabbitmq.listener.stream.auto-startup property has no effect #39079",
                        "Connection leak when using jOOQ and spring.jooq.sql-dialect has not been set #39077",
                        "Error mark in the log message for PatternParseException is in the wrong place #39076",
                        "Manifest attributes cannot be resolved with the new loader implementation #39071"
                    ],
                    "date": "TBD",
                    "selected": true,
                    "goal": "Our goal is ..."
                }
            ],
            "style": "TLS",
            "displayConfig": {
             "colors": [ "#5f57ff", "#2563eb", "#7149c6"],
              "fontColor": "#000000"
            }
        }
    """.trimIndent()

    val release = Json.decodeFromString<ReleaseStrategy>(data)
    release.useDark = false
    val str = ReleaseTimelineSummaryMaker().make(release, isPdf = false)
    val f = File("gen/release.svg")
    f.writeText(str)
}