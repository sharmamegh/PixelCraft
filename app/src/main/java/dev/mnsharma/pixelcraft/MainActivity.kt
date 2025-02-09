package dev.mnsharma.pixelcraft

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val secretImage1: ImageView = findViewById(R.id.secretImage1)
        val secretImage2: ImageView = findViewById(R.id.secretImage2)
        val secretImage3: ImageView = findViewById(R.id.secretImage3)
        val secretImage4: ImageView = findViewById(R.id.secretImage4)

        //showToast("Loading images from raw resources...")

        val bmp0 = loadImageFromRaw(R.raw.bmp0)
        val bmp1 = loadImageFromRaw(R.raw.bmp1)
        val bmp2 = loadImageFromRaw(R.raw.bmp2)
        val bmp3 = loadImageFromRaw(R.raw.bmp3)
        val bmp4 = loadImageFromRaw(R.raw.bmp4)

        if (bmp0 != null && bmp1 != null) {
            //showToast("Aligning and XORing first pair of images...")
            secretImage1.setImageBitmap(xorAlignedBitmaps(bmp0, bmp1))
        }

        if (bmp0 != null && bmp2 != null && bmp3 != null && bmp4 != null) {
            //showToast("Aligning and XORing second, third, and fourth images...")
            secretImage2.setImageBitmap(xorAlignedBitmaps(bmp0, rotateBitmap(bmp2, -89f)))
            secretImage3.setImageBitmap(xorAlignedBitmaps(bmp0, rotateBitmap(bmp3, -179f)))
            secretImage4.setImageBitmap(xorAlignedBitmaps(bmp0, rotateBitmap(bmp4, -271f)))
        } else {
            showToast("Some images failed to load.")
        }
    }

    private fun loadImageFromRaw(resId: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val inputStream = resources.openRawResource(resId)
            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: IOException) {
            showToast("Error loading image: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun xorAlignedBitmaps(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
        val alignedBmp1 = alignToCenter(bmp1, bmp2)
        val alignedBmp2 = alignToCenter(bmp2, bmp1)

        val width = alignedBmp1.width
        val height = alignedBmp1.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        //showToast("Performing XOR operation on images...")

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel1 = alignedBmp1.getPixel(x, y)
                val pixel2 = alignedBmp2.getPixel(x, y)
                val xorPixel = pixel1 xor pixel2
                result.setPixel(x, y, xorPixel)
            }
        }

        //showToast("XOR operation completed.")
        return result
    }

    private fun alignToCenter(source: Bitmap, target: Bitmap): Bitmap {
        val width = maxOf(source.width, target.width)
        val height = maxOf(source.height, target.height)

        val centeredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(centeredBitmap)
        val xOffset = (width - source.width) / 2
        val yOffset = (height - source.height) / 2
        canvas.drawBitmap(source, xOffset.toFloat(), yOffset.toFloat(), null)

        return centeredBitmap
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        //showToast("Rotating image by $degrees degrees...")
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
