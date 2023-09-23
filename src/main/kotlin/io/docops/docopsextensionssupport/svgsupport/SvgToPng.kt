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

package io.docops.docopsextensionssupport.svgsupport

import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.JPEGTranscoder
import org.apache.batik.transcoder.image.PNGTranscoder
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.StringReader



class SvgToPng {

    fun toPngFromSvg(svg: String, res: Pair<String, String>): ByteArray {
        try {
            val input = TranscoderInput(StringReader(svg))
            val baos = ByteArrayOutputStream()
            val output = TranscoderOutput(baos)
            val converter = PNGTranscoder()
            converter.addTranscodingHint(PNGTranscoder.KEY_HEIGHT,  res.first.toFloat())
            converter.addTranscodingHint(PNGTranscoder.KEY_WIDTH,  res.second.toFloat())
            converter.addTranscodingHint(PNGTranscoder.KEY_EXECUTE_ONLOAD,  false);
            converter.addTranscodingHint(PNGTranscoder.KEY_DEFAULT_FONT_FAMILY, "Arial");
            converter.transcode(input, output)
            return baos.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    fun toJpegFromSvg(svg: String, outputStream: OutputStream) {
        val input = TranscoderInput(StringReader(svg))
        val output = TranscoderOutput(outputStream)
        val converter = JPEGTranscoder()
        converter.transcode(input, output)
    }
}