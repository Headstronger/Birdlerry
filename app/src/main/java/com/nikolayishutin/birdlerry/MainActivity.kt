package com.nikolayishutin.birdlerry

import android.os.Bundle
import com.nikolayishutin.birdlerry.activity.BaseActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        leftMenu()

    }
}

