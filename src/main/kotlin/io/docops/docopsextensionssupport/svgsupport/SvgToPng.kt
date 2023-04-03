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
        val input = TranscoderInput(StringReader(svg))
        val baos = ByteArrayOutputStream()
        val output = TranscoderOutput(baos)
        val converter = PNGTranscoder()
        converter.addTranscodingHint(PNGTranscoder.KEY_HEIGHT,  res.first.toFloat())
        converter.addTranscodingHint(PNGTranscoder.KEY_WIDTH,  res.second.toFloat())
        //converter.addTranscodingHint(PNGTranscoder.KEY_EXECUTE_ONLOAD,  false);
        converter.addTranscodingHint(PNGTranscoder.KEY_DEFAULT_FONT_FAMILY, "Arial");
        converter.addTranscodingHint(PNGTranscoder.KEY_USER_STYLESHEET_URI,"")
        converter.transcode(input, output)
        return baos.toByteArray()
    }
    fun toJpegFromSvg(svg: String, outputStream: OutputStream) {
        val input = TranscoderInput(StringReader(svg))
        val output = TranscoderOutput(outputStream)
        val converter = JPEGTranscoder()
        converter.transcode(input, output)
    }
}