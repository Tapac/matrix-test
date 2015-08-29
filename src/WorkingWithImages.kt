
import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamUtils
import com.jhlabs.image.AbstractBufferedImageOp
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.font.TextAttribute
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JFrame

/**
 * User: Andrey.Tarashevskiy
 * Date: 06.06.2015
 */

object winAdapter: WindowAdapter() {
     override fun windowClosing(e: WindowEvent?) {
         System.exit(0)
     }
 }

object MainFrame : JFrame("Main") {
    init {
        val camera = Webcam.getDefault()
        camera.open()
        val file = File("tmp")
//        WebcamUtils.capture(camera, file, "jpeg")
//        val imagePath = File(ClassLoader.getSystemResource("image_2.jpg").path).toURI().toURL()
        val imagePath = file.toURI().toURL()
        add("Center", ImageDrawingComponent(imagePath))
        addWindowListener(winAdapter)
        pack()
    }
}

class ImageDrawingComponent(val imageUrl: URL) : Component() {

    var bufImage: BufferedImage? = null

    init {
        val bi = ImageIO.read(imageUrl).getScaledInstance(800, 600,java.awt.Image.SCALE_DEFAULT );
        val w = bi.getWidth(null);
        val h = bi.getHeight(null);
//        if (bi.type != BufferedImage.TYPE_INT_RGB) {
            val bi2 = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            val big = bi2.graphics;
            big.drawImage(bi, 0, 0, null);
            bufImage = bi2;
//        }
    }

    override public fun paint(g: Graphics) {
        with(g as Graphics2D) {
//            g.setComposite(MiscComposite.getInstance(MiscComposite.ADD, 0.3f))  "゠グシヘヸ"
            val renderTextFilter = PrintCharsFilter.filter

            drawImage(bufImage, renderTextFilter, 0, 0)
        }
    }

}

data class PrintableChar(val char: Char = (0x30A1 + rnd.nextInt(40)).toChar(), val fgColor:Color = Color.GREEN, val bgColor: Color = Color.BLACK)

fun Graphics2D.applyCharAt(char: PrintableChar, x: Int, y: Int ) {
//    setBackground(char.bgColor)
//    setColor(char.fgColor.rndBrightness())
    drawChars(charArrayOf(char.char), 0, 1, x, y)
}

private val rnd: Random = Random()

fun Color.cloneWithAlpha(alpha: Int) = Color(red, green, blue, alpha)

fun Color.rgb() = rgb
fun Color.hsb() = Color.RGBtoHSB(red, green, blue, null)

fun Color.withBrightness(br: Float) = this.hsb().let{ Color(Color.HSBtoRGB(it[0], it[1], br)) }
val Color.brigtness: Float get() = hsb()[2]



fun Color.rndBrightness(): Color {
    var _color = this
    val brighter = rnd.nextBoolean()
    for (i in 1..rnd.nextInt(10)) {
        if (brighter) {
            _color = _color.brighter()
        } else {
            _color = _color.darker()
            _color.alpha
        }
    }
    return _color
}


class PrintCharsFilter(val pixelSize: Int = 10, vararg val chars: PrintableChar) : AbstractBufferedImageOp() {
    fun rndChar() = chars[rnd.nextInt(chars.size())]

    override fun filter(src: BufferedImage, dest: BufferedImage?): BufferedImage {
        with (dest ?: createCompatibleDestImage(src, null)) {
            val gr = createGraphics()
            gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            gr.font = font
            val width = src.width
            val height = src.height
            for (y in 0..height step pixelSize) {
                val h = Math.min(pixelSize, height - y)
                for (x in 0..width step pixelSize) {
                    val w = Math.min(pixelSize, width - x)
                    val rgbs = getRGB(src, x, y, w, h, null)
                    val newColor = calcAverageColor(rgbs)

                    val newRgbs = rgbs.map {
//                        Color.black.withBrightness(newColor.brigtness).getRGB()
                        Color.black.rgb
                    }.toIntArray()
                    setRGB(this, x, y, w, h, newRgbs)
                    gr.color = Color.GREEN./*withBrightness(newColor.brigtness).*/cloneWithAlpha(((/* 1 -*/ newColor.brigtness) * 255).toInt())
                    gr.applyCharAt(rndChar(), x + pixelSize / 4 , y + pixelSize/ 4)
                }
            }
            gr.dispose()
            return this
        }
    }
    companion object {
        val filter = PrintCharsFilter(chars = *(12440..12500).map { PrintableChar(it.toChar()) }.toTypedArray())
        val font = Font.getFont(mapOf (
            TextAttribute.FAMILY to "Monospaced",
            TextAttribute.WEIGHT to TextAttribute.WEIGHT_BOLD,
            TextAttribute.SIZE to 8
        ))
    }
}

fun Triple<Int, Int, Int>.plus(second: Triple<Int, Int, Int>)
        = Triple(first + second.first, this.second + second.second, third + second.third)

fun calcAverageColor(rgbRange: IntArray): Color = if ( rgbRange.isEmpty() ) Color.BLACK
 else rgbRange
        .map { Color(it).let {  Triple(it.red, it.green, it.blue) }
        }.fold(Triple (0,0,0)) { result, cur ->
            result + cur
    }.let { Color (it.first / rgbRange.size(), it.second / rgbRange.size(), it.third / rgbRange.size())}

fun main(args: Array<String>) {
    with(MainFrame) {
        setSize(600, 600)
        isVisible = true
    }
}