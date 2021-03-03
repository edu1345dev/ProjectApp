package com.example.projectapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import java.io.File

class MainActivity : AppCompatActivity() {
    companion object {
        const val FILE_AUTHORITY = "com.example.projectapp"
    }

    val photo by lazy { findViewById<AppCompatImageView>(R.id.picture) }
    val description by lazy { findViewById<TextInputLayout>(R.id.pic_description) }
    val name by lazy { findViewById<TextInputLayout>(R.id.pic_name) }
    val share by lazy { findViewById<FloatingActionButton>(R.id.share) }
    val capture by lazy { findViewById<FloatingActionButton>(R.id.capture) }
    var fileShare: File? = null
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var permissionsHelper: PermissionsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferencesHelper = SharedPreferencesHelper(this)
        permissionsHelper = PermissionsHelper(this, sharedPreferencesHelper)

        capture.setOnClickListener {
            handleCaptureImage()
        }

        share.setOnClickListener {
            if (validate()) shareFile(
                name.editText!!.text.toString(),
                description.editText!!.text.toString()
            )
        }
    }

    private fun handleCaptureImage() {
        val permissions =
            listOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionsHelper.requestAllPermission(permissions)) {
            openChooser()
        }
    }

    private fun openChooser() {
        val intentList = mutableListOf<Intent>()

        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val file = FileHelper.createFileInStorage(this)

        val uri = FileProvider.getUriForFile(
            this,
            FILE_AUTHORITY,
            file!!
        )

        fileShare = file

        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

        val pickIntent = Intent()
        pickIntent.type = "image/*"
        pickIntent.action = Intent.ACTION_GET_CONTENT

        intentList.add(pickIntent)
        intentList.add(takePhotoIntent)

        val chooserIntent = Intent.createChooser(intentList[0], "Escolha como tirar a fotografia:")
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            intentList.toTypedArray()
        )

        startActivityForResult(chooserIntent, 200)
    }

    var uri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == 200 && resultCode == Activity.RESULT_OK && (intent == null || intent.extras == null) && intent?.data == null) {
            val picture = BitmapFactory.decodeFile(fileShare?.path)
            photo.background = null
            photo.setImageBitmap(picture)

            uri = FileProvider.getUriForFile(
                applicationContext,
                FILE_AUTHORITY,
                fileShare!!
            )
        } else if (requestCode == 200 && resultCode == Activity.RESULT_OK && intent?.data != null) {
            val pic = intent.data as Uri
            uri = pic
            photo.background = null
            photo.setImageURI(pic)
        }
    }


    private fun shareFile(photoName: String, photoDescription: String) {
        val textToSend = "Name: $photoName\n\nDescription: $photoDescription"

        val intentSend = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textToSend)
            putExtra(
                Intent.EXTRA_STREAM,
                uri
            )

            type = "image/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val intentChooser = Intent.createChooser(intentSend, "Enviar texto")
        startActivity(intentChooser)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsHelper.handleRequestPermissionResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    private fun validate(): Boolean {
        return if (name.editText!!.text.isEmpty()) {
            name.error = "Required field"
            false
        } else if (description.editText!!.text.isEmpty()) {
            description.error = "Required field"
            name.error = null
            false
        } else {
            name.error = null
            description.error = null
            true
        }
    }
}