package com.moon.instagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.moon.instagram.R
import com.moon.instagram.databinding.ActivityAddPhotoBinding
import com.moon.instagram.navigation.model.ContentDTO
import java.text.SimpleDateFormat
import java.util.Date

class AddPhotoActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityAddPhotoBinding
    private var storage: FirebaseStorage? = null
    private var PICK_IMAGE_FROM_ALBUM = 0
    private var photoUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private val contentDTO = ContentDTO()

    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = ActivityAddPhotoBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        //Initiate
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()

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

        //Promise method (구글에서 권장하는 방식)
        photoUri?.let {
            storageRef?.putFile(it)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }?.addOnSuccessListener { uri ->
                //Insert downloadUrl of image
                contentDTO.imageUrl = uri.toString()

                //Insert uid of user
                contentDTO.uid = auth.currentUser?.uid!!

                //Insert userId
                contentDTO.userId = auth.currentUser?.email!!

                //Insert downloadUrl of image
                contentDTO.explain = mBinding.addPhotoEditExplain.text.toString()

                //Insert timeStamp
                contentDTO.timeStamp = System.currentTimeMillis()

                fireStore.collection("images").document().set(contentDTO)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

        //Callback method
//        photoUri?.let {
//            storageRef?.putFile(it)?.addOnSuccessListener {
//                storageRef.downloadUrl.addOnSuccessListener { uri ->
//
//                    //Insert downloadUrl of image
//                    contentDTO.imageUrl = uri.toString()
//
//                    //Insert uid of user
//                    contentDTO.uid = auth.currentUser?.uid!!
//
//                    //Insert userId
//                    contentDTO.userId = auth.currentUser?.email!!
//
//                    //Insert downloadUrl of image
//                    contentDTO.explain = mBinding.addPhotoEditExplain.text.toString()
//
//                    //Insert timeStamp
//                    contentDTO.timeStamp = System.currentTimeMillis()
//
//                    fireStore.collection("images").document().set(contentDTO)
//                    setResult(Activity.RESULT_OK)
//                    finish()
//                }
//            }
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == RESULT_OK) {
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