package com.transcodium.tnsmoney

import android.os.Bundle
import android.util.Log
import android.util.TimeUtils
import android.view.Gravity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.transcodium.tnsmoney.classes.Anim
import com.firebase.jobdispatcher.*
import com.firebase.jobdispatcher.Job
import com.transcodium.tnsmoney.classes.WalletCore.Companion.homeUpdateUserAssetList
import com.transcodium.tnsmoney.classes.WalletCore.Companion.networkFetchUserAssets
import com.transcodium.tnsmoney.classes.jobs.AssetsDataJob
import com.transcodium.tnsmoney.db.entities.UserAssets
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import org.json.JSONObject
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass


class HomeActivity : DrawerActivity() {



    val homeActivity by lazy {
        this
    }

    var appJob: Job? = null

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

                   if(it.isEmpty()){
                       return@Observer
                   }

                   val dataStr = it.first().data

                   val dataJson = JSONObject(dataStr)

                   //update user asset list
                   homeUpdateUserAssetList(homeActivity,dataJson)

        })//end observer
    }


    /**
     * doPeriodicTask
     */
    suspend fun fetchStatsData(){

        //initial data assets fetch
        networkFetchUserAssets(this)

        observeAndUpdateUserAsset()

        appJob ?: startPeriodicJob(
                  activity =   homeActivity,
                       tag = "statsData",
                    clazz = AssetsDataJob::class,
                   triggerInterval =  Pair(30,60)
        )
    }

}//end class
