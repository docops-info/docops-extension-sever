package io.docops.docopsextensionssupport.vcard.model

import io.docops.docopsextensionssupport.vcard.model.renderer.*
import java.io.File

class VCardSvgGeneratorService(val useDark: Boolean = false) {
    companion object {
        private const val DESIGNS_PATH = "designs"
    }

    private val renderers: Map<String, VCardRenderer> = listOf(
        BusinessCardRenderer(useDark),
        BusinessCard2Renderer(useDark),
        BusinessCardTemplateRenderer(useDark),
        TechPatternCardRenderer(useDark),
        CreativeAgencyCardRenderer(useDark),
        ModernCardRenderer(includeQR = true, useDark)
    ).associateBy { it.designKey }

    fun generateSvg(vcard: VCard, config: VCardConfig): String {
        val renderer = renderers[config.design] ?: renderers["business_card_design"]!!
        return renderer.render(vcard, config)
    }


}

fun main() {
    val card = """BEGIN:VCARD
VERSION:4.0
FN:Jordan A. Rivera
N:Rivera;Jordan;A.;Mr.;
ORG:Acme Solutions;Product Engineering
TITLE:Senior Engineering Manager
ROLE:Engineering Management
EMAIL;TYPE=work,preferred:jjrivera@acmesolutions.com
EMAIL;TYPE=personal:jordan.rivera@gmail.com
TEL;TYPE=work,voice,preferred:+1-415-555-0123
TEL;TYPE=work,fax:+1-415-555-0140
TEL;TYPE=cell:+1-415-555-0199
TEL;TYPE=home:+1-415-555-0166
ADR;TYPE=work;LABEL="Acme Solutions\n123 Market St\nSuite 400\nSan Francisco, CA 94105\nUSA":;;123 Market St;San Francisco;CA;94105;USA
ADR;TYPE=home;LABEL="Home\n456 Oak Ave\nApt 2B\nOakland, CA 94607\nUSA":;;456 Oak Ave;Oakland;CA;94607;USA
URL;TYPE=work:https://www.acmesolutions.com
IMPP;TYPE=work:xmpp:jrivera@acmesolutions.com
X-SOCIALPROFILE;TYPE=twitter:https://twitter.com/jordanrivera
X-SOCIALPROFILE;TYPE=linkedin:https://www.linkedin.com/in/jordanrivera
X-SOCIALPROFILE;TYPE=facebook:https://www.facebook.com/jordan.rivera
BDAY:1985-07-14
REV:2025-11-07T12:00:00Z
NOTE:Primary contact for product engineering and cross-functional initiatives.
END:VCARD
"""
    val parser = VCardParserService()
    val pvcard = parser.parseVCard(card)
    val generator = VCardSvgGeneratorService()
    var svg = generator.generateSvg(pvcard, VCardConfig(design = "business_card_design"))
    var f = File("gen/vcard1.svg")
    f.writeText(svg)

    svg = generator.generateSvg(pvcard, VCardConfig(design = "business_card_design2"))
    f = File("gen/vcard2.svg")
    f.writeText(svg)

    svg = generator.generateSvg(pvcard, VCardConfig(design = "business_card_template"))
    f = File("gen/vcard3.svg")
    f.writeText(svg)

    svg = generator.generateSvg(pvcard, VCardConfig(design = "tech_pattern_background"))
    f = File("gen/vcard4.svg")
    f.writeText(svg)

    svg = generator.generateSvg(pvcard, VCardConfig(design = "creative_agency_pro_contact_card"))
    f = File("gen/vcard5.svg")
    f.writeText(svg)

    svg = generator.generateSvg(pvcard, VCardConfig(design = "modern_card"))
    f = File("gen/vcard6.svg")
    f.writeText(svg)
    println("done")
}