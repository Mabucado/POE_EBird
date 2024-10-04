package com.ebookfrenzy.poe_ebird

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager

class Home : AppCompatActivity() {
    lateinit var frameLayout: FrameLayout
    lateinit var frag:Map
    lateinit var fragManager: FragmentManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        frameLayout=findViewById(R.id.frameLayout)
        frag=Map()
        fragManager=supportFragmentManager
        val fragTrans=fragManager.beginTransaction()
        fragTrans.replace(R.id.frameLayout,frag)
        fragTrans.commit()

    }
}