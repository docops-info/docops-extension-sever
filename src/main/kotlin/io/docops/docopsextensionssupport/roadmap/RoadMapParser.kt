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

package io.docops.docopsextensionssupport.roadmap

import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import kotlin.math.max


/**
 * The RoadMapParser class is responsible for parsing the content of a road map and
 * grouping the items into different categories.
 */
class RoadMapParser {

    /**
     * Parses the given content and returns a RoadMaps object.
     *
     * @param content the content to parse
     * @return a RoadMaps object representing the parsed content
     */
    fun parse(content: String): RoadMaps {
        return group(content = content)

    }

    private fun group(content: String): RoadMaps {
        val now = mutableListOf<MutableList<String>>()
        val next = mutableListOf<MutableList<String>>()
        val later = mutableListOf<MutableList<String>>()
        val done = mutableListOf<MutableList<String>>()
        var newList: MutableList<String> = mutableListOf()
        val urlMap = mutableMapOf<String,String>()
        content.lines().forEachIndexed { index, input ->
            var s = input
            if(input.contains("[[") && input.contains("]]")) {
                val regex = "(?<=\\[\\[)(.*?)(?=]])".toRegex()
                val matches = regex.findAll(s)
                matches.forEach {
                    item ->
                    val urlItem = item.value.split(" ")
                    val url = urlItem[0]
                    val display = urlItem[1]
                    s = input.replace("[[${item.value}]]", "[[${display}]]")
                    urlMap["[[${display}]]"] = url

                }
            }
            if (s.trim().startsWith("- now")) {
                newList = mutableListOf()
                now.add(newList)
            } else if(s.trim().startsWith("- next")) {
                newList = mutableListOf()
                next.add(newList)
            }
            else if(s.trim().startsWith("- later")) {
                newList = mutableListOf()
                later.add(newList)
            }
            else if(s.trim().startsWith("- done")) {
                newList = mutableListOf()
                done.add(newList)
            }
            else {
                newList.add(s)
            }
        }
        return RoadMaps(
            now = now,
            next = next,
            later = later,
            done=done,
            urlMap= urlMap)
    }
}
data class RoadMaps(
    val now: MutableList<MutableList<String>>,
    val next: MutableList<MutableList<String>>,
    val later: MutableList<MutableList<String>>,
    val urlMap: MutableMap<String, String>,
    val done: MutableList<MutableList<String>>
) {
    fun nowList(): MutableList<String> {
        val list = mutableListOf<String>()
        now.forEach {
            list.addAll(it)
        }
        return list
    }
    fun nextList() : MutableList<String> {
        val list = mutableListOf<String>()
        next.forEach { list.addAll(it) }
        return list
    }
    fun laterList() : MutableList<String> {
        val list = mutableListOf<String>()
        later.forEach { list.addAll(it) }
        return list
    }
    fun doneList() : MutableList<String> {
        val list = mutableListOf<String>()
        done.forEach { list.addAll(it) }
        return list
    }
    fun maxListSize() : Int {
        return max(doneList().size,max(nowList().size, max(nextList().size,laterList().size)))
    }
}

fun RoadMaps.excel(title: String): ByteArray {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet(title)
    val style: CellStyle = workbook.createCellStyle()
    style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GOLD.index)
    style.fillPattern = FillPatternType.SOLID_FOREGROUND

    val header: Row = sheet.createRow(0)
    header.createCell(0).setCellValue("NOW")
    header.createCell(1).setCellValue("NEXT")
    header.createCell(2).setCellValue("LATER")
    header.createCell(3).setCellValue("DONE")

    for (c in 0..3) {
        header.getCell(c).cellStyle = style
    }

    val dataRow: Row = sheet.createRow(1)
    dataRow.createCell(0).setCellValue("")
    dataRow.createCell(1).setCellValue("")
    dataRow.createCell(2).setCellValue("")
    dataRow.createCell(3).setCellValue("")


    repeat(maxListSize()) { index ->
        val row = sheet.createRow(index+2)
        var nowValue = ""
        val n = nowList()
        if(n.size -1 >= index) {
            nowValue = n[index]
        }
        var nextValue = ""
        val nt= nextList()
        if(nt.size -1 > index) {
            nextValue = nt[index]
        }
        var laterValue = ""
        val lt = laterList()
        if(lt.size -1 > index) {
            laterValue = lt[index]
        }
        var doneValue = ""
        val dl = doneList()
        if(dl.size-1 > index){
            doneValue = dl[index]
        }
        val cell0 = row.createCell(0)
        val cellStyle0 = cell0.sheet.workbook.createCellStyle()
        cellStyle0.wrapText = true
        cell0.setCellStyle(cellStyle0)
        cell0.setCellValue(nowValue)

        val cell1 = row.createCell(1)
        val cellStyle1 = cell1.sheet.workbook.createCellStyle()
        cellStyle1.wrapText = true
        cell1.setCellStyle(cellStyle1)
        cell1.setCellValue(nextValue)

        val cell2 = row.createCell(2)

        val cellStyle2 = cell2.sheet.workbook.createCellStyle()
        cellStyle2.wrapText = true
        cell2.setCellStyle(cellStyle2)
        cell2.setCellValue(laterValue)

        val content = row.createCell(3)
        val cellStyle: CellStyle = content.sheet.workbook.createCellStyle()
        cellStyle.wrapText = true

        content.setCellStyle(cellStyle)
        content.setCellValue(doneValue)
    }

    sheet.autoSizeColumn(0)
    sheet.autoSizeColumn(1)
    sheet.autoSizeColumn(2)
    sheet.autoSizeColumn(3)

    val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
    workbook.write(outputStream)
    workbook.close()
    return outputStream.toByteArray()
}
fun RoadMaps.maxLength() : Int {
    return max(now.size, max(next.size,later.size))
}