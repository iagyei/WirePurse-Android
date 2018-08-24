package com.transcodium.tnsmoney

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.app_bar.view.*

class HomeActivity : DrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.activity_home)

        super.onCreate(savedInstanceState)


        toolbar.topToolbarTitle.text = "Bitcoin"


    }
}
