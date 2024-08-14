import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import com.sik.sikimage.ImageConvertUtils

class TiffPainter(filePath: String) : Painter() {

    private val bitmap: Bitmap? = loadTiffAsBitmap(filePath)

    // 返回 TIFF 图像的原始尺寸
    override val intrinsicSize: Size
        get() = Size(bitmap?.width?.toFloat() ?: 0f, bitmap?.height?.toFloat() ?: 0f)

    // 在给定的绘制范围内绘制 TIFF 图像
    override fun DrawScope.onDraw() {
        bitmap?.let {
            val imageBitmap = bitmap.asImageBitmap()

            // 计算缩放比例以适应绘制范围
            val scaleFactor = minOf(size.width / bitmap.width, size.height / bitmap.height)

            // 计算偏移量，使图像居中绘制
            val offsetX = (size.width - (bitmap.width * scaleFactor)) / 2
            val offsetY = (size.height - (bitmap.height * scaleFactor)) / 2

            // 绘制图像，并在绘制之前应用缩放
            drawImage(
                image = imageBitmap,
                topLeft = Offset(offsetX, offsetY)
            )
        }
    }

    // 从文件路径加载 TIFF 文件并转换为 Bitmap
    private fun loadTiffAsBitmap(filePath: String): Bitmap? {
        // 这里你可以使用自定义的 TIFF 解码逻辑，如果没有 TIFF 解码库，示例中使用了普通的Bitmap加载
        // 注意：Android 原生不支持直接加载 TIFF 文件，实际中需要使用第三方库如 libtiff, ImageMagick 或 OpenCV
        return ImageConvertUtils.tifToBitmap(filePath)
    }
}
