package com.transcodium.tnsmoney

import android.os.Bundle
import android.view.Gravity
import com.transcodium.tnsmoney.classes.Anim
import com.transcodium.tnsmoney.classes.CoinsCore
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.app_bar.view.*
import kotlinx.coroutines.experimental.launch


class HomeActivity : DrawerActivity() {

    val homeActivity by lazy {
        this
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        Anim(this).slideWindow(
                Gravity.START,
                Gravity.END
        )

        setContentView(R.layout.activity_home)

        super.onCreate(savedInstanceState)

        launch {
            CoinsCore.fetchUserCoins(mActivity, true)
        }

    }
}
