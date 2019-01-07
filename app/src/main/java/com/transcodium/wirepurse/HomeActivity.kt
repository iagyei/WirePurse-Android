package com.transcodium.wirepurse

import android.content.Context
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.firebase.jobdispatcher.Job
import com.transcodium.wirepurse.classes.WalletCore
import com.transcodium.wirepurse.classes.WalletCore.Companion.homeUpdateUserAssetList
import com.transcodium.wirepurse.classes.WalletCore.Companion.networkFetchUserAssets
import com.transcodium.wirepurse.classes.WalletCore.Companion.pollNetworkAssetStats
import com.transcodium.wirepurse.classes.jobs.AssetsDataJob
import com.transcodium.wirepurse.view_models.HomeViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.circular_progress_bar.*
import kotlinx.android.synthetic.main.home_coin_info.*
import kotlinx.coroutines.*
import org.jetbrains.anko.toast
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
            sendOrReceiveAssetDialog(ReceiveCryptoAssetActivity::class.java)
        }//end on click


        /**
         * sendCryptoAsset
         */
        sendAsset.setOnClickListener {
            sendOrReceiveAssetDialog(SendCryptoAssetActivity::class.java)
        }

    }//end onCreate


    /**
     * sendOrReceiveAssetDialog
     */
    fun <T>sendOrReceiveAssetDialog(clazz: Class<T>){

        //lets get the current asset
        ///fix errrrrrror... crashes onload cos its empty
        val assetSymbol = coinInfoCard.tag

        if(assetSymbol == null){
            toast(R.string.app_loading_data)
            return
        }

        val data = Bundle().apply { putString("asset_symbol",assetSymbol.toString()) }

        startClassActivity(
                activityClass = clazz,
                clearActivityStack = false,
                data = data
        )

    }//end


    /**
     * observe Live Data
     */
    private fun observeLiveData() {

       val viewProvider = ViewModelProviders.of(this)
                        .get(HomeViewModel::class.java)

        val pb = progressBar

        viewProvider.getUserAssets()
                  .observe(this, Observer{ userAsset->

                   if(pb.isVisible){ pb.hide() }

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
