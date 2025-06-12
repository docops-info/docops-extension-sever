package io.docops.docopsextensionssupport.cal

import io.docops.docopsextensionssupport.svgsupport.addSvgMetadata
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalMaker {

    fun makeCalendar(entry: CalEntry?): String{
        val monFmt = DateTimeFormatter.ofPattern("MMM")
        val today = LocalDate.now()
        val month = entry?.month ?: monFmt.format(today)
        val year = entry?.year ?: today.year

        val svg = """
    <svg xmlns="http://www.w3.org/2000/svg" height="32" width="32"
     aria-label="Calendar" role="img" viewBox="0 0 512 512">
    <defs>
        <linearGradient x2="0%" y2="100%" id="calGrad3">
            <stop stop-color="#ee99a1" stop-opacity="1" offset="0%"/>
            <stop stop-color="#dd3344" stop-opacity="1" offset="100%"/>
        </linearGradient>
        <linearGradient x2="0%" y2="100%" id="calGrad4">
            <stop stop-color="#b2bbbb" stop-opacity="1" offset="0%"/>
            <stop stop-color="#667777" stop-opacity="1" offset="100%"/>
        </linearGradient>
        <linearGradient x2="0%" y2="100%" id="calGrad5">
            <stop stop-color="#eef6f6" stop-opacity="1" offset="0%"/>
            <stop stop-color="#ddeeee" stop-opacity="1" offset="100%"/>
        </linearGradient>
    </defs>
    <rect width="512" height="512" rx="77" fill="url(#calGrad5)"/>
    <path d="m77 0h358a77 77 0 0 1 77 77v103h-512v-103a77 77 0 0 1 77-77z" fill="url(#calGrad3)"/>
    <g fill="#eab">
        <circle cx="470" cy="142" r="14"/>
        <circle cx="470" cy="100" r="14"/>
        <circle cx="427" cy="142" r="14"/>
        <circle cx="427" cy="100" r="14"/>
        <circle cx="384" cy="142" r="14"/>
        <circle cx="384" cy="100" r="14"/>
    </g>
    <g fill="url(#calGrad4)" >
        <text fill="#fff" font-size="140" x="140" y="164" id="m" style="font-family: monospace; text-anchor: middle;">$month</text>
        <text font-size="200" x="256" y="400" id="d" style="font-family: monospace; text-anchor: middle; fill: #555555">$year</text>
    </g>
</svg>
        """.trimIndent()
        return addSvgMetadata(svg)
    }

}