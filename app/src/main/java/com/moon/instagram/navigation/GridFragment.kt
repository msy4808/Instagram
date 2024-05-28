package com.moon.instagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.moon.instagram.databinding.FragmentGridBinding

class GridFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mBinding = FragmentGridBinding.inflate(layoutInflater,container,false)
        return mBinding.root
    }
}