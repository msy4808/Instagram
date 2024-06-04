package com.moon.instagram.navigation

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.moon.instagram.R
import com.moon.instagram.databinding.ActivityAddPhotoBinding
import java.text.SimpleDateFormat
import java.util.Date

class AddPhotoActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityAddPhotoBinding
    private var storage: FirebaseStorage? = null
    private var PICK_IMAGE_FROM_ALBUM = 0
    private var photoUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = ActivityAddPhotoBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        //Initiate Storage
        storage = FirebaseStorage.getInstance()

        //Open the Album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        //Add Image Upload Event
        mBinding.addPhotoBtnUpload.setOnClickListener {
            contentUpload()
        }
    }

    private fun contentUpload() {
        //Make filename
        val timesTamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_${timesTamp}.png"

        val storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //FileUpload
        photoUri?.let {
            storageRef?.putFile(it)?.addOnSuccessListener {
                Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM) {
            if(resultCode == RESULT_OK) {
                //This is path to the selected image
                photoUri = data?.data
                mBinding.addPhotoImage.setImageURI(photoUri)
            } else {
                //Exit the addPhotoActivity if you leave the album without selecting it
                finish()
            }
        }
    }
}