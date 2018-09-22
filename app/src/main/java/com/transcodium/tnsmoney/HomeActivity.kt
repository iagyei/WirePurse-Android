package com.transcodium.tnsmoney

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.transcodium.tnsmoney.classes.Anim
import com.transcodium.tnsmoney.classes.WalletCore
import com.firebase.jobdispatcher.*
import com.transcodium.tnsmoney.classes.WalletCore.Companion.networkFetchUserAssets
import com.transcodium.tnsmoney.classes.jobs.AssetsDataJob
import com.transcodium.tnsmoney.db.entities.UserAssets
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.android.UI
import java.lang.Exception


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

        launch(Dispatchers.Main) {

            //lets start the job for r
            fetchStatsData()
        }

    }//end onCreate

    /**
     * observe UserAssetUpdate
     */
    fun observeAndUpdateUserAsset() {

       val vmprovider = ViewModelProviders.of(this)

        vmprovider.get(HomeViewModel::class.java)
                  .getUserAssets()
                  .observe(this, Observer<List<UserAssets>>{

              println("HEHEHEHE $it")
        })//end observer
    }


    /**
     * doPeriodicTask
     */
    suspend fun fetchStatsData(){

        //initial data assets fetch
        networkFetchUserAssets(this)

        observeAndUpdateUserAsset()

        try {
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(mActivity))

            val job = dispatcher.newJobBuilder()
                    .setService(AssetsDataJob::class.java)
                    .setTag("assets_data")
                    .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                    .setTrigger(Trigger.executionWindow(0, 30))
                    .setRecurring(true)
                    .setConstraints(Constraint.ON_ANY_NETWORK)
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    .build()

            dispatcher.mustSchedule(job)
        }catch(e: Exception){
            Log.e("JoB Error","${e.message}")
            e.printStackTrace()
        }

    }

}//end class
