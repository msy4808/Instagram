package com.moon.instagram.navigation

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.moon.instagram.R
import com.moon.instagram.databinding.ActivityCommentBinding
import com.moon.instagram.navigation.model.ContentDTO

class CommentActivity : AppCompatActivity() {
    lateinit var mBinding: ActivityCommentBinding
    lateinit var firebaseAuth: FirebaseAuth
    var contentUid: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCommentBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        contentUid = intent.getStringExtra("contentUid") ?: ""
        setContentView(mBinding.root)

        mBinding.commentBtnSend.setOnClickListener {
            val comment = ContentDTO.Comment()
            comment.userId = firebaseAuth.currentUser?.email ?: ""
            comment.uid = firebaseAuth.currentUser?.uid ?: ""
            comment.comment = mBinding.commentEditMessage.text.toString()
            comment.timeStamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid)
                .collection("comments").document().set(comment)
            mBinding.commentEditMessage.setText(null)
        }
    }

    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comments: ArrayList<ContentDTO.Comment> = arrayListOf()

        init {
            FirebaseFirestore.getInstance().collection("images").document(contentUid)
                .collection("comments").orderBy("timestamp")
                .addSnapshotListener { value, error ->
                    comments.clear()
                    if (value == null) return@addSnapshotListener
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }
    }
}