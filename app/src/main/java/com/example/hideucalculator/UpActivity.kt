package com.example.hideucalculator

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class UpActivity : AppCompatActivity() {

    private lateinit var selectImageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var displayButton: Button
    private var selectedImageUris: ArrayList<Uri> = ArrayList()
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_up)

        selectImageView = findViewById(R.id.Select_image)
        uploadButton = findViewById(R.id.upload_image)
        displayButton = findViewById(R.id.disply_image)

        // Retrieve the password from the Intent
        password = intent.getStringExtra("PASSWORD").toString()

        // Check if password is null or empty
        if (password.isEmpty()) {
            Toast.makeText(this, "Password is missing", Toast.LENGTH_SHORT).show()
            finish()  // Finish the activity if password is missing
            return
        }

        // Check and request permissions
        checkAndRequestPermissions()

        selectImageView.setOnClickListener {
            openGalleryForImages()
        }

        uploadButton.setOnClickListener {
            uploadImages()
        }

        displayButton.setOnClickListener {
            displayImages()
        }
    }

    private fun openGalleryForImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startForResult.launch(intent)
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri: Uri = data.clipData!!.getItemAt(i).uri
                    selectedImageUris.add(imageUri)
                }
            } else if (data?.data != null) {
                val imageUri: Uri? = data.data
                imageUri?.let { selectedImageUris.add(it) }
            }
            Toast.makeText(this, "Selected ${selectedImageUris.size} images", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImages() {
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // Clear previously saved image paths from SharedPreferences
                clearImagePathsFromSharedPreferences()

                // Save only the newly selected images
                selectedImageUris.forEach { uri ->
                    val imagePath = saveImageToInternalStorage(uri)
                    imagePath?.let { saveImagePathToSharedPreferences(it) }
                }

                // Delete selected images from gallery
                deleteImagesFromGallery(selectedImageUris)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@UpActivity, "New images saved locally and deleted from gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayImages() {
        val imagePaths = getImagePathsFromSharedPreferences()
        val intent = Intent(this, DisplayImagesActivity::class.java)
        intent.putStringArrayListExtra("image_paths", ArrayList(imagePaths))
        startActivity(intent)
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveImagePathToSharedPreferences(imagePath: String) {
        val sharedPreferences = getSharedPreferences("image_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val imagePathList = sharedPreferences.getStringSet("image_paths", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        imagePathList.add(imagePath)
        editor.putStringSet("image_paths", imagePathList)
        editor.apply()
    }

    private fun getImagePathsFromSharedPreferences(): List<String> {
        val sharedPreferences = getSharedPreferences("image_prefs", MODE_PRIVATE)
        return sharedPreferences.getStringSet("image_paths", mutableSetOf())?.toList() ?: emptyList()
    }

    private fun clearImagePathsFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences("image_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("image_paths")
        editor.apply()
    }

    private fun deleteImagesFromGallery(uris: List<Uri>) {
        uris.forEach { uri ->
            try {
                // Retrieve the content URI for the image
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val projection = arrayOf(MediaStore.Images.Media._ID)
                val selection = "${MediaStore.Images.Media.DATA} = ?"
                val selectionArgs = arrayOf(uri.path)

                contentResolver.query(
                    contentUri,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                        val deleteUri = ContentUris.withAppendedId(contentUri, id)
                        contentResolver.delete(deleteUri, null, null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            if (!it.value) {
                Toast.makeText(this, "Permission ${it.key} denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
