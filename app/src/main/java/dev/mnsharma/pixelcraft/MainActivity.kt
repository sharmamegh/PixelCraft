package dev.mnsharma.pixelcraft

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageViewOutput: ImageView

    private var bitmap1: Bitmap? = null
    private var bitmap2: Bitmap? = null

    private lateinit var pickImageLauncher1: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher2: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncherOutput: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView1 = findViewById(R.id.imageView1)
        imageView2 = findViewById(R.id.imageView2)
        imageViewOutput = findViewById(R.id.imageViewOutput)

        val buttonSelectImage1: Button = findViewById(R.id.buttonSelectImage1)
        val buttonSelectImage2: Button = findViewById(R.id.buttonSelectImage2)
        val buttonPerformXor: Button = findViewById(R.id.buttonPerformXor)

        // Register activity result launchers for image selection
        pickImageLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (uri != null) {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                bitmap1 = BitmapFactory.decodeStream(inputStream)
                imageView1.setImageBitmap(bitmap1)
            }
        }

        pickImageLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (uri != null) {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                bitmap2 = BitmapFactory.decodeStream(inputStream)
                imageView2.setImageBitmap(bitmap2)
            }
        }

        pickImageLauncherOutput = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (uri != null) {
                imageViewOutput.setImageURI(uri)
            }
        }

        buttonSelectImage1.setOnClickListener { selectImage(pickImageLauncher1) }
        buttonSelectImage2.setOnClickListener { selectImage(pickImageLauncher2) }
        buttonPerformXor.setOnClickListener { performXor() }
        // Click listener for Output ImageView
        imageViewOutput.setOnClickListener { selectImageForOutput() }
    }

    private fun selectImage(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        launcher.launch(intent)
    }

    private fun selectImageForOutput() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncherOutput.launch(intent)
    }

    private fun performXor() {
        if (bitmap1 == null || bitmap2 == null) {
            Toast.makeText(this, "Please select both images", Toast.LENGTH_SHORT).show()
            return
        }

        if (bitmap1!!.width != bitmap2!!.width || bitmap1!!.height != bitmap2!!.height) {
            Toast.makeText(this, "Images must have the same dimensions", Toast.LENGTH_SHORT).show()
            return
        }

        val width = bitmap1!!.width
        val height = bitmap1!!.height
        val xorBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel1 = bitmap1!!.getPixel(x, y)
                val pixel2 = bitmap2!!.getPixel(x, y)

                val r = (pixel1 shr 16 and 0xFF) xor (pixel2 shr 16 and 0xFF)
                val g = (pixel1 shr 8 and 0xFF) xor (pixel2 shr 8 and 0xFF)
                val b = (pixel1 and 0xFF) xor (pixel2 and 0xFF)

                xorBitmap.setPixel(x, y, (0xFF shl 24) or (r shl 16) or (g shl 8) or b)
            }
        }

        imageViewOutput.setImageBitmap(xorBitmap)
    }
}