package com.transcodium.tnsmoney

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import com.transcodium.tnsmoney.classes.Coin
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.app_bar.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch


class HomeActivity : DrawerActivity() {

    val homeActivity by lazy {
        this
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.activity_home)

        super.onCreate(savedInstanceState)

        launch(UI) {
            //lets fetch the data
            Coin.fetchUserCoins(homeActivity)
        }

        toolbar.topToolbarTitle.text = "Bitcoin"

    }
}
