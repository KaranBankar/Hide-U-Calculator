package com.example.hideucalculator

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream

class DisplayImagesActivity : AppCompatActivity() {

    private lateinit var imagePaths: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_images)

        imagePaths = intent.getStringArrayListExtra("image_paths") ?: arrayListOf()
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val unhideButton = findViewById<Button>(R.id.unhide_btn)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ImageAdapter(this, imagePaths)

        unhideButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                unhideImages()
            }
        }
    }

    private suspend fun unhideImages() {
        imagePaths.forEach { path ->
            val file = File(path)
            if (file.exists()) {
                saveImageToGallery(this, file)
            }
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(this@DisplayImagesActivity, "Images saved to gallery", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToGallery(context: Context, file: File): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/UnhiddenImages")
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            try {
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                val inputStream = FileInputStream(file)
                inputStream.copyTo(outputStream!!)
                inputStream.close()
                outputStream.close()
                file.delete()  // Delete the file from internal storage
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return uri
    }
}
