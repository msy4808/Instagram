package com.moon.instagram.navigation

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.moon.instagram.LoginActivity
import com.moon.instagram.MainActivity
import com.moon.instagram.R
import com.moon.instagram.databinding.FragmentUserBinding
import com.moon.instagram.navigation.model.ContentDTO
import com.moon.instagram.navigation.model.FollowDTO

class UserFragment: Fragment() {
    lateinit var mBinding: FragmentUserBinding
    lateinit var firestore: FirebaseFirestore
    lateinit var uid: String
    lateinit var auth: FirebaseAuth
    lateinit var currentUserUid: String
    lateinit var mContext: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentUserBinding.inflate(layoutInflater,container,false)
        uid = arguments?.getString("destinationUid") ?: ""
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth.currentUser?.uid ?: ""
        if (uid == currentUserUid) {
            //MyPage
            mBinding.accountBtnFollowSignOut.text = getString(R.string.signout)
            mBinding.accountBtnFollowSignOut.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth.signOut()
            }
        } else {
            //OtherUserPage
            mBinding.accountBtnFollowSignOut.text = getString(R.string.follow)
            val mainActivity = activity as MainActivity
            mainActivity.mBinding.toolbarUserName.text = arguments?.getString("userId")
            mainActivity.mBinding.toolbarBackBtn.setOnClickListener {
                mainActivity.mBinding.bottomNavigation.selectedItemId = R.id.action_home
            }
            mainActivity.mBinding.toolbarTitleImage.visibility = View.GONE
            mainActivity.mBinding.toolbarUserName.visibility = View.VISIBLE
            mainActivity.mBinding.toolbarBackBtn.visibility = View.VISIBLE

            mBinding.accountBtnFollowSignOut.setOnClickListener {
                requestFollow()
            }
        }
        mBinding.accountRecycleView.adapter = UserFragmentRecycleViewAdapter()
        mBinding.accountRecycleView.layoutManager = GridLayoutManager(activity, 3)

        mBinding.accountIvProfile.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage()
        getFollowerAndFollowing()
        return mBinding.root
    }

    private fun getProfileImage() {
        firestore.collection("profileImages").document(uid).addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener

            if (value.data != null) {
                val url = value.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(mBinding.accountIvProfile)
            }
        }
    }

    private fun requestFollow() {
        //Save data to my account
        val tsDocFollowing = firestore.collection("users").document(currentUserUid)
        firestore.runTransaction {
            var followDTO = it.get(tsDocFollowing).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO.followingCount = 1
                followDTO.followings[uid] = true

                it.set(tsDocFollowing, followDTO)
                return@runTransaction
            }

            if (followDTO.followings.containsKey(uid)) {
                //It remove following third person when a third person follow me
                followDTO.followingCount -= 1
                followDTO.followings.remove(uid)
            } else {
                //It add following third person when a third person follow me
                followDTO.followingCount += 1
                followDTO.followings[uid] = true
            }
            it.set(tsDocFollowing, followDTO)
            return@runTransaction
        }
        //Save data to third person
        val tsDocFollower = firestore.collection("users").document(uid)
        firestore.runTransaction {
            var followerDTO = it.get(tsDocFollower).toObject(FollowDTO::class.java)
            if (followerDTO == null) {
                followerDTO = FollowDTO()
                followerDTO.followerCount = 1
                followerDTO.followers[currentUserUid] = true

                it.set(tsDocFollower, followerDTO)
                return@runTransaction
            }

            if (followerDTO.followers.containsKey(currentUserUid)) {
                //It cancel my follower when I follow a third person
                followerDTO.followerCount -= 1
                followerDTO.followers.remove(currentUserUid)
            } else {
                //It add my follower when I don't follow a third person
                followerDTO.followerCount += 1
                followerDTO.followers[currentUserUid] = true
            }
            it.set(tsDocFollower, followerDTO)
            return@runTransaction
        }
    }

    private fun getFollowerAndFollowing() {
        firestore.collection("users").document(uid).addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener
            var followDTO = value.toObject(FollowDTO::class.java)

            if (followDTO?.followingCount != null) {
                mBinding.accountTvFollowingCount.text = followDTO.followingCount.toString()
            }
            if (followDTO?.followerCount != null) {
                mBinding.accountTvFollowerCount.text = followDTO.followerCount.toString()
                if (followDTO.followers.containsKey(currentUserUid)) {
                    mBinding.accountBtnFollowSignOut.text = getString(R.string.follow_cancel)
                    mBinding.accountBtnFollowSignOut.background.colorFilter = PorterDuffColorFilter(resources.getColor(R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
                } else {
                    if (uid != currentUserUid) {
                        mBinding.accountBtnFollowSignOut.text = getString(R.string.follow)
                        mBinding.accountBtnFollowSignOut.background.colorFilter = null
                    }
                }
            }
        }
    }

    inner class UserFragmentRecycleViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        init {
            firestore.collection("images").whereEqualTo("uid", uid).addSnapshotListener { value, error ->
                //Sometimes, This code return null of value when it signOut
                if (value == null) return@addSnapshotListener

                //Get datta
                for (snapshot in value.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                mBinding.accountTvPostCount.text = contentDTOs.size.toString()
                notifyDataSetChanged()
            }
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(holder.itemView.findViewById(R.id.user_image_view))
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        val PICK_PROFILE_FROM_ALBUM = 10
    }
}