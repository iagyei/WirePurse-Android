package com.transcodium.tnsmoney

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.app_bar.view.*


class HomeActivity : DrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.activity_home)

        super.onCreate(savedInstanceState)

       // contentView.setBackgroundColor(resources.getColor(R.color.white))


        toolbar.topToolbarTitle.text = "Bitcoin"

    }
}
