package com.moon.instagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.moon.instagram.R
import com.moon.instagram.databinding.ActivityCommentBinding
import com.moon.instagram.navigation.model.AlarmDTO
import com.moon.instagram.navigation.model.ContentDTO

class CommentActivity : AppCompatActivity() {
    lateinit var mBinding: ActivityCommentBinding
    lateinit var firebaseAuth: FirebaseAuth
    var contentUid: String = ""
    var destinationUid: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCommentBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        contentUid = intent.getStringExtra("contentUid") ?: ""
        destinationUid = intent.getStringExtra("destinationUid") ?: ""
        mBinding.commentRecyclerview.adapter = CommentRecyclerviewAdapter()
        mBinding.commentRecyclerview.layoutManager = LinearLayoutManager(this)
        setContentView(mBinding.root)

        mBinding.commentBtnSend.setOnClickListener {
            val comment = ContentDTO.Comment()
            comment.userId = firebaseAuth.currentUser?.email ?: ""
            comment.uid = firebaseAuth.currentUser?.uid ?: ""
            comment.comment = mBinding.commentEditMessage.text.toString()
            comment.timeStamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid)
                .collection("comments").document().set(comment)
            commentAlarm(destinationUid, mBinding.commentEditMessage.text.toString())
            mBinding.commentEditMessage.setText(null)
        }
    }

    private fun commentAlarm(destinationUid: String, message: String) {
        val alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        FirebaseAuth.getInstance().currentUser?.email.let { if (it != null) { alarmDTO.userId = it } }
        FirebaseAuth.getInstance().currentUser?.uid.let { if (it != null) { alarmDTO.uid = it } }
        alarmDTO.timeStamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

    }

    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comments: ArrayList<ContentDTO.Comment> = arrayListOf()

        init {
            FirebaseFirestore.getInstance().collection("images").document(contentUid)
                .collection("comments").orderBy("timeStamp")
                .addSnapshotListener { value, error ->
                    comments.clear()
                    if (value == null) return@addSnapshotListener

                    for (snapshot in value.documents) {
                        snapshot.toObject(ContentDTO.Comment::class.java)?.let { comments.add(it) }
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val view = holder.itemView
            view.findViewById<TextView>(R.id.comment_view_item_text_comment).text = comments[position].comment
            view.findViewById<TextView>(R.id.comment_view_item_textview_profile).text = comments[position].userId
            FirebaseFirestore.getInstance().collection("profileImages").document(comments[position].uid).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val url = it.result["image"]
                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.findViewById<ImageView>(R.id.comment_view_item_profile))
                }
            }

        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }
}