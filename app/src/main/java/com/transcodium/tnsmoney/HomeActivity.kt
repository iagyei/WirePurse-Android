package com.transcodium.tnsmoney

import android.os.Bundle
import android.view.Gravity
import com.transcodium.tnsmoney.classes.Anim
import com.transcodium.tnsmoney.classes.CoinsCore
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.app_bar.view.*
import kotlinx.coroutines.experimental.launch
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.setTag
import com.firebase.jobdispatcher.*
import com.transcodium.tnsmoney.classes.jobs.AssetsDataJob


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

            //lets start the job for r
            fetchStatsData()
        }

    }//end onCreate



    /**
     * doPeriodicTask
     */
    suspend fun fetchStatsData(){

        CoinsCore.fetchUserCoins(mActivity, true)

        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))

        val job = dispatcher.newJobBuilder()
                .setService(AssetsDataJob::class.java)
                .setTag("assets_data")
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setRecurring(true)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build()

        dispatcher.mustSchedule(job)
    }

}//end class
