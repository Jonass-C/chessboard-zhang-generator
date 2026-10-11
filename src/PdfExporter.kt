import java.awt.image.BufferedImage
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import javax.imageio.ImageIO

/**
 * Pure Kotlin PDF Exporter. Writes valid PDF 1.4 documents natively
 * without external third-party dependencies or OS print service requirements.
 */
object PdfExporter {

    /**
     * Exports a BufferedImage as a standard A4 Portrait PDF file.
     *
     * @param bufferedImage High-resolution image to embed in the PDF
     * @param outputFileName Name or path of the destination PDF file
     */
    fun exportToPdfA4Native(
        bufferedImage: BufferedImage,
        outputFileName: String = "chessboard_calibration.pdf"
    ): File {
        val outputFile = File(outputFileName)

        val baosJpg = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "jpg", baosJpg)
        val jpgData = baosJpg.toByteArray()

        val imgWidth = bufferedImage.width
        val imgHeight = bufferedImage.height

        // Standard A4 dimensions in PDF points (72 DPI)
        val pageWidthPt = 595.28
        val pageHeightPt = 841.89

        val contentStream = "q\n${String.format(java.util.Locale.US, "%.2f 0 0 %.2f 0.00 0.00", pageWidthPt, pageHeightPt)} cm\n/Im1 Do\nQ\n"
        val contentBytes = contentStream.toByteArray(Charsets.US_ASCII)

        class CountingOutputStream(out: OutputStream) : BufferedOutputStream(out) {
            var count: Long = 0
                private set

            override fun write(b: Int) {
                super.write(b)
                count++
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                out.write(b, off, len)
                count += len
            }
        }

        val countingOut = CountingOutputStream(FileOutputStream(outputFile))
        val offsets = mutableListOf<Long>()

        fun writeAscii(s: String) {
            countingOut.write(s.toByteArray(Charsets.US_ASCII))
        }

        // Write PDF 1.4 Header
        writeAscii("%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n")

        offsets.add(countingOut.count)
        writeAscii("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")

        offsets.add(countingOut.count)
        writeAscii("2 0 obj\n<< /Type /Pages /Kids [ 3 0 R ] /Count 1 >>\nendobj\n")

        offsets.add(countingOut.count)
        writeAscii("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [ 0 0 595.28 841.89 ] /Resources << /XObject << /Im1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n")

        offsets.add(countingOut.count)
        writeAscii("4 0 obj\n<< /Type /XObject /Subtype /Image /Width $imgWidth /Height $imgHeight /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length ${jpgData.size} >>\nstream\n")
        countingOut.write(jpgData)
        writeAscii("\nendstream\nendobj\n")

        offsets.add(countingOut.count)
        writeAscii("5 0 obj\n<< /Length ${contentBytes.size} >>\nstream\n")
        countingOut.write(contentBytes)
        writeAscii("\nendstream\nendobj\n")

        val startXref = countingOut.count
        writeAscii("xref\n0 6\n0000000000 65535 f \n")
        for (offset in offsets) {
            writeAscii(String.format(java.util.Locale.US, "%010d 00000 n \n", offset))
        }

        writeAscii("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n$startXref\n%%EOF\n")

        countingOut.flush()
        countingOut.close()

        println("PDF successfully generated at: ${outputFile.absolutePath}")
        return outputFile
    }
}