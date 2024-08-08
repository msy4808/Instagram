package com.moon.instagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.moon.instagram.R
import com.moon.instagram.databinding.FragmentGridBinding
import com.moon.instagram.navigation.model.ContentDTO

class GridFragment: Fragment() {
    lateinit var firestore: FirebaseFirestore
    lateinit var mBinding: FragmentGridBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentGridBinding.inflate(layoutInflater,container,false)
        firestore = FirebaseFirestore.getInstance()
        mBinding.gridFragmentRecyclerview.adapter = GridFragmentRecycleViewAdapter()
        mBinding.gridFragmentRecyclerview.layoutManager = GridLayoutManager(activity, 3)
        return mBinding.root
    }

    inner class GridFragmentRecycleViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        init {
            firestore.collection("images").addSnapshotListener { value, error ->
                //Sometimes, This code return null of value when it signOut
                if (value == null) return@addSnapshotListener

                //Get datta
                for (snapshot in value.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(
                RequestOptions().centerCrop()).into(holder.itemView.findViewById(R.id.user_image_view))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
            val width = resources.displayMetrics.widthPixels / 3
            val imageView = view.findViewById<ImageView>(R.id.user_image_view) as ImageView
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }
}