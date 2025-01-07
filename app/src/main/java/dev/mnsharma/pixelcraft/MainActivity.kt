package dev.mnsharma.pixelcraft

import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageViewOutput: ImageView
    private lateinit var editTextRotation: EditText

    private var bitmap1: Bitmap? = null
    private var bitmap2: Bitmap? = null

    private lateinit var pickImageLauncher1: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher2: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        imageView1 = findViewById(R.id.imageView1)
        imageView2 = findViewById(R.id.imageView2)
        imageViewOutput = findViewById(R.id.imageViewOutput)
        editTextRotation = findViewById(R.id.editTextRotation)

        val buttonSelectImage1: Button = findViewById(R.id.buttonSelectImage1)
        val buttonSelectImage2: Button = findViewById(R.id.buttonSelectImage2)
        val buttonRotateAndXor: Button = findViewById(R.id.buttonRotateAndXor)

        // Register activity result launchers for image selection
        pickImageLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (uri != null) {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                bitmap1 = BitmapFactory.decodeStream(inputStream)
                imageView1.setImageBitmap(bitmap1)
                displayImageName(uri) // Show image name as Toast
            }
        }

        pickImageLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (uri != null) {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                bitmap2 = BitmapFactory.decodeStream(inputStream)
                imageView2.setImageBitmap(bitmap2)
                displayImageName(uri) // Show image name as Toast
            }
        }

        // Set up buttons
        buttonSelectImage1.setOnClickListener { selectImage(pickImageLauncher1) }
        buttonSelectImage2.setOnClickListener { selectImage(pickImageLauncher2) }
        buttonRotateAndXor.setOnClickListener { rotateAndXor() }
    }

    private fun selectImage(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        launcher.launch(intent)
    }

    private fun rotateAndXor() {
        if (bitmap1 == null || bitmap2 == null) {
            Toast.makeText(this, "Please select both images", Toast.LENGTH_SHORT).show()
            return
        }

        val rotationInput = editTextRotation.text.toString()
        val rotationAngle = rotationInput.toFloatOrNull() ?: 0f

        try {
            val rotatedBitmap = rotateBitmap(bitmap2!!, rotationAngle)
            val resultBitmap = xorImages(bitmap1!!, rotatedBitmap)
            imageViewOutput.setImageBitmap(resultBitmap) // Display XOR result
        } catch (e: Exception) {
            Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun xorImages(bitmap1: Bitmap, bitmap2: Bitmap): Bitmap {
        val width = bitmap1.width.coerceAtMost(bitmap2.width)
        val height = bitmap1.height.coerceAtMost(bitmap2.height)
        val xorBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel1 = bitmap1.getPixel(x, y)
                val pixel2 = bitmap2.getPixel(x, y)

                val r = (pixel1 shr 16 and 0xFF) xor (pixel2 shr 16 and 0xFF)
                val g = (pixel1 shr 8 and 0xFF) xor (pixel2 shr 8 and 0xFF)
                val b = (pixel1 and 0xFF) xor (pixel2 and 0xFF)

                xorBitmap.setPixel(x, y, (0xFF shl 24) or (r shl 16) or (g shl 8) or b)
            }
        }

        return xorBitmap
    }

    private fun rotateBitmap(bitmap: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun displayImageName(uri: Uri) {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val imageName = it.getString(nameIndex)
                Toast.makeText(this, "Selected Image: $imageName", Toast.LENGTH_SHORT).show()
            }
        }
    }
}