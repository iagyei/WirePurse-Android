package com.transcodium.tnsmoney

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.transcodium.tnsmoney.classes.Anim
import com.firebase.jobdispatcher.Job
import com.transcodium.tnsmoney.classes.TNSChart
import com.transcodium.tnsmoney.classes.WalletCore.Companion.homeUpdateUserAssetList
import com.transcodium.tnsmoney.classes.WalletCore.Companion.networkFetchUserAssets
import com.transcodium.tnsmoney.classes.WalletCore.Companion.pollNetworkAssetStats
import com.transcodium.tnsmoney.classes.jobs.AssetsDataJob
import com.transcodium.tnsmoney.db.entities.UserAssets
import com.transcodium.tnsmoney.view_models.HomeViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import org.json.JSONObject


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
            initHome()
        }

    }//end onCreate

    /**
     * observe Live Data
     */
    fun observeLiveData() {

       val viewProvider = ViewModelProviders.of(this)
                        .get(HomeViewModel::class.java)


        viewProvider.getUserAssets()
                  .observe(this, Observer{

                   if(it.isEmpty()){
                       return@Observer
                   }

                   val dataStr = it.first().data

                   val dataJson = JSONObject(dataStr)

                   //update user asset list
                   homeUpdateUserAssetList(homeActivity,dataJson)

        })//end observer

        val tnsChartObj = TNSChart()

       viewProvider.getCryptoAssetStats()
               .observe(this, Observer {

                   val data = JSONObject(it.data)

                   //lets get selected or active coin in the info card
                  val activeCoinSymbol = homeActivity.coinInfoCard.tag?.toString() ?: "tns"

                  val activeCoinPair = "$activeCoinSymbol.usd"

                  val dataArray = data.optJSONArray(activeCoinPair)

                    if(dataArray == null){
                        Log.e("HOME_ASSET_STATS","$activeCoinPair key not found")
                        return@Observer
                    }

                  tnsChartObj.processHomeCoinInfoGraph(homeActivity,dataArray)
       })
    }//end fun


    /**
     * doPeriodicTask
     */
    suspend fun initHome(){


        launch {

            //initial data assets fetch
            networkFetchUserAssets(homeActivity)

            //fetch asset stats
            pollNetworkAssetStats(homeActivity as Context)
        }

        observeLiveData()

        appJob ?: startPeriodicJob(
                  activity =   homeActivity,
                       tag = "statsData",
                    clazz = AssetsDataJob::class,
                   triggerInterval =  Pair(30,60)
        )
    }

}//end class
