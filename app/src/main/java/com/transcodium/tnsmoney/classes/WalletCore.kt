/**
# Copyright 2018 - Transcodium Ltd.
#  All rights reserved. This program and the accompanying materials
#  are made available under the terms of the  Apache License v2.0 which accompanies this distribution.
#
#  The Apache License v2.0 is available at
#  http://www.opensource.org/licenses/apache2.0.php
#
#  You are required to redistribute this code under the same licenses.
#
#  Project TNSMoney
#  @author Razak Zakari <razak@transcodium.com>
#  https://transcodium.com
#  created_at 26/08/2018
 **/

package com.transcodium.tnsmoney.classes

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.experimental.android.UI
import org.json.JSONObject
import kotlinx.android.synthetic.main.activity_home.*
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.lifecycle.ViewModelProviders
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.transcodium.tnsmoney.*
import com.transcodium.tnsmoney.R.id.coinInfoCard
import com.transcodium.tnsmoney.db.AppDB
import com.transcodium.tnsmoney.db.daos.UserAssetsDao
import com.transcodium.tnsmoney.db.entities.AssetStats
import com.transcodium.tnsmoney.db.entities.UserAssets
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.home_coin_info.*
import kotlinx.coroutines.experimental.*
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.find
import org.json.JSONArray
import java.lang.Exception
import java.util.*


class WalletCore {

    companion object {


        private val colors by lazy{

            mapOf(
                "tns" to R.color.colorTNS,
                "eth" to R.color.colorETH,
                "btc" to R.color.colorBTC,
                "xmr" to R.color.colorXMR,
                "ltc" to R.color.colorLTC,
                "eos" to R.color.colorEOS
            )

        }//end colors

        private val icons by lazy {
            mapOf(
                "tns" to R.drawable.ic_tns_normal,
                "eth" to R.drawable.ic_eth,
                "btc" to R.drawable.ic_btc,
                "xmr" to R.drawable.ic_xmr,
                "ltc" to R.drawable.ic_ltc,
                "eos" to R.drawable.ic_eos
            )
        }

        /**
         * getColor
         */
        fun  getColor(ctx: Context, coin: String): Int{
            return ContextCompat.getColor(
                    ctx,
                    colors[coin] ?: R.color.colorPrimaryDark
            )
        }

        /**
         * getIcon
         */
        fun getIcon(name: String): Int {
            return  icons[name] ?: R.drawable.transparent_img
        }

        /**
         * fetch userCoins
         */
        suspend fun networkFetchUserAssets(context: Context): Status {

            val dataStatus = TnsApi(context)
                        .get(API_USER_ASSETS)

            if(dataStatus.isError()){
                return dataStatus
            }//end

            //lets update db
            val data = dataStatus.getData<JSONObject>()

            if(data == null){
                Log.e("USER_ASSETS","User Assets Data returned from server is null")
                return dataStatus
            }

            //println("NETWORK DATA - data.toString()")

            val userAssetDBData = UserAssets(
                    data = data.toString()
            )

            launchIO{
               try {
                   AppDB.getInstance(context).userAssetsDao()
                           .updateData(userAssetDBData)
               }catch (e: Exception){
                    Log.e("USER_ASSET_SAVE","Failed to save user assets")
                   e.printStackTrace()
               }

            } //end launch

            return dataStatus
        }//end

        /**
         * dbFetchUser Assets
         */
         suspend fun dbFetchUserAssets(
                context: Context,returnList: Boolean = false
        ): Status{

            //userAssetDao
            val db = AppDB.getInstance(context)

            val userAssetsDataList = db.userAssetsDao().all

            if(userAssetsDataList.isEmpty()){
                return Status.error(R.string.no_assets_available)
            }


            val dataObjStr = userAssetsDataList.first().data

            if(dataObjStr == null){
                return Status.error(R.string.no_assets_available)
            }

            val  dataJsonObj = JSONObject(dataObjStr)

            if(!returnList) {
                return Status.success(data = dataJsonObj)
            }

            val tnsData = dataJsonObj.optJSONObject("tns")

            //les convert into list
            val proccessedDataList = mutableListOf<JSONObject>()

            if(tnsData != null){ proccessedDataList.add(tnsData) }

            for(key in dataJsonObj.keys()){

                if(key == "tns"){ continue }


            }
        }//end fun


        /**
         * networkFetchAssetStats
         */
        suspend fun networkFetchAssetStats(context: Context): Status {

            val dataStatus = TnsApi(context)
                    .get("/stats/assets/")


            if (dataStatus.isError()) {
                return dataStatus
            }//end

            //lets update db
            val dataArray = dataStatus.getData<JSONArray>()

            if (dataArray == null) {
                Log.e("HTTP_ASSETS_STATS", "Asset STats returned from server is null")
                return dataStatus
            }


            launchIO {
                try {

                    val dao = AppDB.getInstance(context).assetStatsDao()

                    val dataSize = dataArray.length() - 1

                    for(i in 0..dataSize){

                        val dataObj = dataArray[i] as JSONObject

                        val type = dataObj.optString("type")
                        val data = dataObj.optJSONObject("data")

                        if(type == null || data == null){
                            return@launchIO
                        }

                        val assetStatsData = AssetStats(
                                type = type,
                                data = data.toString()
                        )

                        //update db
                        dao.updateData(
                            data = assetStatsData
                        )
                    }

                } catch (e: Exception) {
                    Log.e("USER_ASSET_SAVE", "Failed to save user assets")
                    e.printStackTrace()
                }

            }

            return Status.success()
        }//end fun

        /**
         * pollNetworkAssetStats
         */
        suspend fun pollNetworkAssetStats(context: Context): Timer{

            val cs = CoroutineScope(Dispatchers.IO)

            //lets get inital data
            networkFetchAssetStats(context)

            val timer = setPeriodic(60_000L){
               cs.launch { networkFetchAssetStats(context) }
            }//end set periodic task


            return timer
        }//end fun


        /**
         * homeUpdateAssetPriceAndGraph
         */
         fun homeUpdateAssetLatestPriceAndGraph(
                activity: Activity,
                assetSymbol: String,
                animateGraph: Boolean = false,
                allStatsJsonStr: String? = null
        ) = launchIO{

           val assetsStatsJsonStr = if(allStatsJsonStr != null){

               allStatsJsonStr

            }else{

               val db = AppDB.getInstance(activity)

               db.assetStatsDao().findByType("crypto").data
               ?: return@launchIO

           }//end fetch data

            //lets create our json object
            val assetsJsonObj = try{

                JSONObject(assetsStatsJsonStr)

            }catch (e: Exception){

                e.printStackTrace()

                return@launchIO
            }//end

            val assetPair = "$assetSymbol.usd"

            if(!assetsJsonObj.has(assetPair)){
                Log.e("HOME_ASSET_STATS","$assetPair key not found")
                return@launchIO
            }

            val dataArray = assetsJsonObj.optJSONArray(assetPair)

            //println(dataArray)

            launchUI {
                TNSChart(activity).processHomeCoinInfoGraph(dataArray,animateGraph)
            }

        }//end fun

        /**
        * updateHomeCoinCardColor
         */
        fun homeUpdateCurrentAssetInfo(
                activity: Activity,
                coinInfo: JSONObject,
                draweGraph: Boolean? = true
        ){
           activity.apply{

               val animDuration = 1000L

               val coinName = coinInfo.getString("name")

               val symbol   = coinInfo.getString("symbol").toLowerCase()

               //user balance
               val userBalance = coinInfo.optDouble("balance",0.0000)

               //split userBalance
               val userBalanceSplit = userBalance.toString().split(".")

               val coinColor = WalletCore.getColor(this,symbol)

               val coinColorDarken = coinColor.darken(0.1)

               val view = coinInfoCard as CardView


               if(draweGraph!!) {
                   //update graph
                   launchUI {
                       homeUpdateAssetLatestPriceAndGraph(
                               activity = activity,
                               assetSymbol = symbol,
                               animateGraph = true
                       )
                   }//end
               }

               //set tag as symbol
               view.tag = symbol

               val oldColor = view.cardBackgroundColor.defaultColor

               val cardBgAnim = ObjectAnimator.ofInt(toolbar,
                       "backgroundColor",
                       oldColor,
                       coinColorDarken
               )

               cardBgAnim.doOnStart { view.setLayerType(View.LAYER_TYPE_HARDWARE, null)  }

               cardBgAnim.doOnEnd { view.setLayerType(View.LAYER_TYPE_NONE, null) }

               cardBgAnim.addUpdateListener{ ls->

                   val animColor = ls.animatedValue as Int

                   view.setCardBackgroundColor(animColor)

                   activity.setStatusBarColor(animColor)
               }

               cardBgAnim.duration = animDuration

               cardBgAnim.setEvaluator(ArgbEvaluator())

               cardBgAnim.doOnStart {

                   //use hardware to run anim

                   activity.setToolbarTitle(coinName)

                   //update userBalance the same when
                   //the bg anim starts
                   userBalanceView.animate()
                           .alpha(0f)
                           .withLayer()
                           .setDuration(animDuration/2)
                           .withEndAction{

                               coinTicker.text = symbol

                               //set user balance
                               balanceFirstDigit.text = userBalanceSplit[0]

                               val balanceDecimal = ".${userBalanceSplit[1]}"

                               userBalanceDecimal.text = balanceDecimal

                               userBalanceView
                                       .animate()
                                       .alpha(1f)
                                       .setDuration(animDuration/2)
                                       .withLayer()
                                       .start()

                           }.start()
               }//end do on start

               cardBgAnim.start()
           }//end

        }//end fun



        /**
         * update home coins UI
         */
        fun homeUpdateUserAssetList(
                activity: Activity,
                coinsInfo: JSONObject
        ): RecyclerView{

            //we need TNS first so we extract it and put
            val tnsCoinInfo = coinsInfo.getJSONObject("tns")

            val sortedData = mutableListOf<JSONObject>()

            sortedData.add(tnsCoinInfo)

            for(coinKey in coinsInfo.keys()){

                //skip tns as we have added to the tp already
                if(coinKey == "tns"){ continue }

                sortedData.add(coinsInfo.getJSONObject(coinKey))
            }

            val adapter = HomeCoinListAdapter(activity,sortedData)

            val recyclerView = activity.findViewById<RecyclerView>(R.id.coinsListRecycler)

            if(recyclerView.adapter != null) {

                recyclerView.swapAdapter(adapter,true)

                //apply activity obj
                activity.apply {

                    //if recycler exists, lets get active
                    //recyclerView
                    val currentCoinSymbol = coinInfoCard.tag.toString()

                    val selectedCoinInfo = coinsInfo.optJSONObject(currentCoinSymbol)

                    //println(selectedCoinInfo)

                    if (selectedCoinInfo != null) {

                        //lets set balance only
                        val userBalanceSplit = selectedCoinInfo
                                .optDouble("balance", 0.0000)
                                .toString().split(".")

                        //set user balance
                        balanceFirstDigit.text = userBalanceSplit[0]

                        val balanceDecimal = ".${userBalanceSplit[1]}"

                        userBalanceDecimal.text = balanceDecimal

                    }//end if

                }//end activity apply

            }else{

                //update initial  ui
                homeUpdateCurrentAssetInfo(activity,tnsCoinInfo,false)

                val calColumn = activity.calColumns(160)

                val gridLayout = GridLayoutManager(activity,calColumn)

                recyclerView.layoutManager = gridLayout


                recyclerView.adapter = adapter

            }

            return recyclerView
        }//end fun

    }
}

