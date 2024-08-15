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
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.moon.instagram.R
import com.moon.instagram.databinding.FragmentAlarmBinding
import com.moon.instagram.navigation.model.AlarmDTO

class AlarmFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mBinding = FragmentAlarmBinding.inflate(layoutInflater,container,false)
        mBinding.alarmFragmentRecyclerview.adapter = AlarmRecyclerviewAdapter()
        mBinding.alarmFragmentRecyclerview.layoutManager = LinearLayoutManager(activity)
        return mBinding.root
    }

    inner class AlarmRecyclerviewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val alarmDTOList: ArrayList<AlarmDTO> = arrayListOf()

        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).addSnapshotListener { value, error ->
                if (value == null) return@addSnapshotListener

                alarmDTOList.clear()
                for (snapshot in value.documents) {
                    snapshot.toObject(AlarmDTO::class.java)?.let { alarmDTOList.add(it) }
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val view = holder.itemView
            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val url = it.result["image"]
                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.findViewById(R.id.comment_view_item_profile))
                }
            }

            when (alarmDTOList[position].kind) {
                0 -> {
                    val favoriteAlarm = "${alarmDTOList[position].userId} ${getString(R.string.alarm_favorite)}"
                    view.findViewById<TextView>(R.id.comment_view_item_textview_profile).text = favoriteAlarm
                }
                1 -> {
                    val commentAlarm = "${alarmDTOList[position].userId} ${getString(R.string.alarm_comment)} of ${alarmDTOList[position].message}"
                    view.findViewById<TextView>(R.id.comment_view_item_textview_profile).text = commentAlarm
                }
                2 -> {
                    val followAlarm = "${alarmDTOList[position].userId} ${getString(R.string.alarm_follow)}"
                    view.findViewById<TextView>(R.id.comment_view_item_textview_profile).text = followAlarm
                }
            }
            view.findViewById<TextView>(R.id.comment_view_item_text_comment).visibility = View.GONE
        }

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        private inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view)
    }
}