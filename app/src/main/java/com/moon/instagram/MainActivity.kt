package com.moon.instagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.moon.instagram.databinding.ActivityMainBinding
import com.moon.instagram.navigation.AlarmFragment
import com.moon.instagram.navigation.DetailViewFragment
import com.moon.instagram.navigation.GridFragment
import com.moon.instagram.navigation.UserFragment

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mBinding.run {
            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.action_home -> {
                        val fragment = DetailViewFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
                        return@setOnItemSelectedListener true
                    }
                    R.id.action_search -> {
                        val fragment = GridFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
                        return@setOnItemSelectedListener true
                    }
                    R.id.action_add_photo -> {
                        return@setOnItemSelectedListener true
                    }
                    R.id.action_favorite_alarm -> {
                        val fragment = AlarmFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
                        return@setOnItemSelectedListener true
                    }
                    R.id.action_account -> {
                        val fragment = UserFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
                        return@setOnItemSelectedListener true
                    }
                }
                return@setOnItemSelectedListener false
            }
        }
    }
}