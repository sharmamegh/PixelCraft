package dev.mnsharma.pixelcraft

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    private lateinit var secretImages: Array<ImageView>
//    private lateinit var outputFolder: File
    private lateinit var progressDialog: AlertDialog
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        secretImages = arrayOf(
            findViewById(R.id.secretImage1),
            findViewById(R.id.secretImage2),
            findViewById(R.id.secretImage3),
            findViewById(R.id.secretImage4)
        )

        // Initialize the ProgressBar in a Dialog
        progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Processing")
            .setMessage("Decrypting images...")
            .setView(progressBar)
            .setCancelable(false)

        progressDialog = dialogBuilder.create()

        findViewById<Button>(R.id.btnDecrypt).setOnClickListener {
            decryptSecretImages()
        }
    }

    private fun decryptSecretImages() {
        progressDialog.show()

        // Start background task to handle image decryption
        Thread {
            val bitmaps = loadRawBitmaps()
            if (bitmaps.isEmpty()) {
                runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to load secret images", Toast.LENGTH_SHORT).show()
                }
                return@Thread
            }

            val referenceImage = bitmaps[0]
            val rotatedBitmaps = arrayOf(
                bitmaps[1],
                rotateImageUsingEquation(bitmaps[2], referenceImage, -90f),
                rotateImageUsingEquation(bitmaps[3], referenceImage, -180f),
                rotateImageUsingEquation(bitmaps[4], referenceImage, -270f)
            )

            val decryptedImages = rotatedBitmaps.map { xorImages(referenceImage, it) }

            runOnUiThread {
                decryptedImages.forEachIndexed { index, bitmap ->
                    secretImages[index].setImageBitmap(bitmap)
                    // Save the bitmap in background without blocking UI
//                    saveBitmap(bitmap, "decrypted_image_$index.png")
                }
                progressDialog.dismiss()
                Toast.makeText(this, "Decryption completed!", Toast.LENGTH_LONG).show()
            }
        }.start() // Start the background thread for decryption
    }

    private fun loadRawBitmaps(): Array<Bitmap> {
        return arrayOf(
            BitmapFactory.decodeResource(resources, R.raw.bmp0),
            BitmapFactory.decodeResource(resources, R.raw.bmp1),
            BitmapFactory.decodeResource(resources, R.raw.bmp2),
            BitmapFactory.decodeResource(resources, R.raw.bmp3),
            BitmapFactory.decodeResource(resources, R.raw.bmp4)
        )
    }

    private fun xorImages(bmpRef: Bitmap, bmpTarget: Bitmap): Bitmap {
        val width = bmpRef.width
        val height = bmpRef.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val refPixel = bmpRef.getPixel(x, y)
                val targetPixel = bmpTarget.getPixel(x, y)
                val xorPixel = refPixel xor targetPixel
                result.setPixel(x, y, xorPixel)
            }
        }

        return result
    }

    private fun rotateImageUsingEquation(bitmap: Bitmap, bmpRefImg: Bitmap, angle: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val rotatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val originX = width / 2.0
        val originY = height / 2.0
        val radAngle = Math.toRadians(angle.toDouble())

        for (y in 0 until height) {
            for (x in 0 until width) {
                val col = bmpRefImg.getPixel(x, y)
                val colImg = bitmap.getPixel(x, y)

                if (Color.red(col) == 0) {
                    val deltaX = x - originX
                    val deltaY = y - originY
                    val newX = floor(originX + cos(radAngle) * deltaX - sin(radAngle) * deltaY).toInt()
                    val newY = floor(originY + sin(radAngle) * deltaX + cos(radAngle) * deltaY).toInt()

                    if (newX in 0 until width && newY in 0 until height) {
                        rotatedBitmap.setPixel(newX, newY, colImg)
                    }
                } else {
                    rotatedBitmap.setPixel(x, y, Color.WHITE)
                }
            }
        }

        return rotatedBitmap
    }

//    private fun saveBitmap(bitmap: Bitmap, fileName: String) {
//        // Perform saving operation in a background thread to avoid ANR
//        Thread {
//            try {
//                val file = File(outputFolder, fileName)
//                val outputStream = FileOutputStream(file)
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//                outputStream.flush()
//                outputStream.close()
//
//                // Update UI on the main thread after saving the image
//                runOnUiThread {
//                    Toast.makeText(this, "Image saved at: ${file.absolutePath}", Toast.LENGTH_LONG).show()
//                }
//            } catch (e: IOException) {
//                // Handle error in saving image
//                runOnUiThread {
//                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }.start() // Start background thread
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_FOLDER_PICKER && resultCode == Activity.RESULT_OK) {
//            data?.data?.let { uri ->
//                outputFolder = File(Environment.getExternalStorageDirectory(), uri.path!!)
//                Toast.makeText(this, "Folder selected: ${outputFolder.absolutePath}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

//    companion object {
//        private const val REQUEST_FOLDER_PICKER = 1
//    }
}
