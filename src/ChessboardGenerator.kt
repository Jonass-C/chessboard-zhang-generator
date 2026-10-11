import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities

/**
 * Generates high-resolution chessboard pattern images for camera calibration (Zhang's Method).
 */
object ChessboardGenerator {

    // A4 Portrait resolution at 300 DPI (210mm x 297mm)
    const val A4_WIDTH_300DPI = 2480
    const val A4_HEIGHT_300DPI = 3508

    /**
     * Generates a BufferedImage of the chessboard pattern optimized for A4 Portrait paper.
     *
     * @param innerCornersX Number of inner corners horizontally (default 6 -> 7 squares)
     * @param innerCornersY Number of inner corners vertically (default 8 -> 9 squares)
     * @param marginMm White quiet-zone margin around the board in millimeters
     */
    fun generateA4ChessboardImagePortrait(
        innerCornersX: Int = 6,
        innerCornersY: Int = 8,
        marginMm: Float = 10f
    ): BufferedImage {
        val squareCols = innerCornersX + 1 // 7 squares horizontally
        val squareRows = innerCornersY + 1 // 9 squares vertically

        val image = BufferedImage(A4_WIDTH_300DPI, A4_HEIGHT_300DPI, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()

        // Disable anti-aliasing for sharp, mathematically accurate edges required by OpenCV
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)

        // Fill background with solid white
        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, A4_WIDTH_300DPI, A4_HEIGHT_300DPI)

        // Convert margin from mm to pixels at 300 DPI
        val marginPxX = ((marginMm / 25.4f) * 300f).toInt()
        val marginPxY = ((marginMm / 25.4f) * 300f).toInt()

        val usableWidth = A4_WIDTH_300DPI - (2 * marginPxX)
        val usableHeight = A4_HEIGHT_300DPI - (2 * marginPxY)

        // Calculate maximum square size that fits the usable area
        val squareSizeX = usableWidth / squareCols
        val squareSizeY = usableHeight / squareRows
        val squareSizePx = minOf(squareSizeX, squareSizeY)

        // Center the chessboard on the A4 page
        val boardWidth = squareCols * squareSizePx
        val boardHeight = squareRows * squareSizePx
        val startX = (A4_WIDTH_300DPI - boardWidth) / 2
        val startY = (A4_HEIGHT_300DPI - boardHeight) / 2

        // Render checkerboard pattern ((r + c) % 2 == 0 ensures all 4 outer corners are black)
        g2d.color = Color.BLACK
        for (r in 0 until squareRows) {
            for (c in 0 until squareCols) {
                if ((r + c) % 2 == 0) {
                    val x = startX + (c * squareSizePx)
                    val y = startY + (r * squareSizePx)
                    g2d.fillRect(x, y, squareSizePx, squareSizePx)
                }
            }
        }

        g2d.dispose()
        return image
    }
}

fun main() {
    // Generate chessboard image (6x8 inner corners / 7x9 squares)
    val chessboardImage = ChessboardGenerator.generateA4ChessboardImagePortrait(
        innerCornersX = 6,
        innerCornersY = 8,
        marginMm = 10f
    )

    // Export PDF to project root using the singleton PdfExporter
    PdfExporter.exportToPdfA4Native(chessboardImage, "chessboard_calibration.pdf")

    // Display preview window
    SwingUtilities.invokeLater {
        val frame = JFrame("Zhang Chessboard (7x9 Squares)")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        val displayScale = 0.22f
        val windowWidth = (chessboardImage.width * displayScale).toInt()
        val windowHeight = (chessboardImage.height * displayScale).toInt()

        val panel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                val g2d = g as Graphics2D
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                g2d.drawImage(chessboardImage, 0, 0, windowWidth, windowHeight, null)
            }

            override fun getPreferredSize(): Dimension {
                return Dimension(windowWidth, windowHeight)
            }
        }

        frame.add(panel)
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}