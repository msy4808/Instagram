package com.moon.instagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.moon.instagram.R
import com.moon.instagram.databinding.FragmentDetailBinding
import com.moon.instagram.navigation.model.ContentDTO

class DetailViewFragment : Fragment() {
    lateinit var firestore: FirebaseFirestore
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mBinding = FragmentDetailBinding.inflate(layoutInflater, container, false)
        firestore = FirebaseFirestore.getInstance()

        mBinding.let {
            it.recyclerview.adapter = DetailViewRecyclerViewAdapter()
            it.recyclerview.layoutManager = LinearLayoutManager(activity)
        }

        return mBinding.root
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        val contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore.collection("images").orderBy("timeStamp").addSnapshotListener { value, error ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    for (snapshot in value!!.documents) {
                        val item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            //UserId
            holder.itemView.findViewById<TextView>(R.id.item_profile_text).text = contentDTOs[position].userId

            //Image
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(holder.itemView.findViewById(R.id.item_image_content))

            //Explain of content
            holder.itemView.findViewById<TextView>(R.id.item_explain_text).text = contentDTOs[position].explain

            //likes
            holder.itemView.findViewById<TextView>(R.id.item_favorite_counter).text = "Likes ${contentDTOs[position].favoriteCount}"

            //ProfileImage
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(holder.itemView.findViewById(R.id.item_profile_image))

            //This code is when the button is clicked
            holder.itemView.findViewById<ImageView>(R.id.item_favorite_image).setOnClickListener {
                favoriteEvent(position)
            }

            //This code is when the page is loaded
            if(contentDTOs[position].favorites.containsKey(uid)) {
                //This is like status
                holder.itemView.findViewById<ImageView>(R.id.item_favorite_image).setImageResource(R.drawable.ic_favorite)
            } else {
                //This is unlike status
                holder.itemView.findViewById<ImageView>(R.id.item_favorite_image).setImageResource(R.drawable.ic_favorite_border)
            }
        }

        private fun favoriteEvent(position: Int) {
            val tsDoc = firestore.collection("images").document(contentUidList[position])
            firestore.runTransaction {
                val contentDTO = it.get(tsDoc).toObject(ContentDTO::class.java)

                if (contentDTO?.favorites?.containsKey(uid) == true) {
                    //When the button is clicked
                    contentDTO.favoriteCount = contentDTO.favoriteCount - 1
                    contentDTO.favorites.remove(uid)
                } else {
                    //When the button is not clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount?.plus(1)!!
                    contentDTO.favorites.set(uid!!, true)
                }
                it.set(tsDoc, contentDTO)
            }
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}