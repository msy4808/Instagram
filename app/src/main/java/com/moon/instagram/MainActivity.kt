package com.moon.instagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.moon.instagram.databinding.ActivityMainBinding
import com.moon.instagram.navigation.AddPhotoActivity
import com.moon.instagram.navigation.AlarmFragment
import com.moon.instagram.navigation.DetailViewFragment
import com.moon.instagram.navigation.GridFragment
import com.moon.instagram.navigation.UserFragment

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private val REQUEST_CODE_PERMISSIONS = 1004
    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initPermissions()
        initViews()
    }

    private fun initViews() {
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
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if((ContextCompat.checkSelfPermission(this.root.context, Manifest.permission.READ_MEDIA_AUDIO)
                                == PackageManager.PERMISSION_GRANTED) ||
                                (ContextCompat.checkSelfPermission(this.root.context, Manifest.permission.READ_MEDIA_IMAGES)
                                        == PackageManager.PERMISSION_GRANTED) ||
                                (ContextCompat.checkSelfPermission(this.root.context, Manifest.permission.READ_MEDIA_VIDEO)
                                        == PackageManager.PERMISSION_GRANTED)) {
                                startActivity(Intent(this.root.context, AddPhotoActivity::class.java))
                            }
                        } else {
                            if(ContextCompat.checkSelfPermission(this.root.context, Manifest.permission.READ_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED) {
                                startActivity(Intent(this.root.context, AddPhotoActivity::class.java))
                            }
                        }

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

    private fun initPermissions() {
        //Sdk 버전이 33 이상이면 권한이 세분화 되어서 구분해야함
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), REQUEST_CODE_PERMISSIONS)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_CODE_PERMISSIONS)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_VIDEO), REQUEST_CODE_PERMISSIONS)

        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSIONS)
        }
    }
}