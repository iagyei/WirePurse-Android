package com.transcodium.tnsmoney

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.firebase.jobdispatcher.Job
import com.transcodium.tnsmoney.classes.WalletCore
import com.transcodium.tnsmoney.classes.WalletCore.Companion.homeUpdateUserAssetList
import com.transcodium.tnsmoney.classes.WalletCore.Companion.networkFetchUserAssets
import com.transcodium.tnsmoney.classes.WalletCore.Companion.pollNetworkAssetStats
import com.transcodium.tnsmoney.classes.jobs.AssetsDataJob
import com.transcodium.tnsmoney.view_models.HomeViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.home_coin_info.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import org.json.JSONObject


class HomeActivity : DrawerActivity() {



    private val homeActivity by lazy {
        this
    }


    private var appJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {


        setContentView(R.layout.activity_home)

        super.onCreate(savedInstanceState)

        launch(Dispatchers.Main) {

            //lets start the job for r
            initHome()
        }


        /**
         * receive
         */
        receiveAsset.setOnClickListener {

            //lets get the current asset
            val assetSymbol = coinInfoCard.tag.toString()

            val data = Bundle().apply { putString("asset_symbol",assetSymbol) }

            startClassActivity(
                    activityClass = ReceiveCryptoAssetActivity::class.java,
                    clearActivityStack = false,
                    data = data
            )

        }//end on click

    }//end onCreate

    /**
     * observe Live Data
     */
    private fun observeLiveData() {

       val viewProvider = ViewModelProviders.of(this)
                        .get(HomeViewModel::class.java)


        viewProvider.getUserAssets()
                  .observe(this, Observer{ userAsset->

                   if(userAsset == null || userAsset.isEmpty()){
                       return@Observer
                   }

                   val dataStr = userAsset.first().data

                   val dataJson = JSONObject(dataStr)

                   //update user asset list
                   homeUpdateUserAssetList(homeActivity,dataJson)

        })//end observer


       viewProvider.getCryptoAssetStats()
               .observe(this, Observer {assetStats->

                   if(assetStats == null){
                       return@Observer
                   }

                   //lets get selected or active coin in the info card
                   val activeCoinSymbol = homeActivity.coinInfoCard.tag?.toString() ?: "tns"

                   WalletCore.homeUpdateAssetLatestPriceAndGraph(
                           activity = homeActivity,
                           assetSymbol = activeCoinSymbol,
                           allStatsJsonStr = assetStats.data
                   )//end

               })
    }//end fun


    /**
     * doPeriodicTask
     */
    private suspend fun initHome(){


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
