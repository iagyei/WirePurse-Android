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
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject
import kotlinx.android.synthetic.main.activity_home.*
import android.view.ViewAnimationUtils
import android.view.animation.AnimationUtils
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnStart
import androidx.core.graphics.ColorUtils
import com.transcodium.tnsmoney.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.home_coin_info.*
import java.util.*


class CoinsCore {

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
        suspend fun fetchUserCoins(activity: Activity,
                                   renderUI: Boolean = false): Status {

            val uri = "/wallet/coins"

            val dataStatus = TnsApi(activity)
                        .get(uri)

            if(dataStatus.isError()){

                if(renderUI) {
                    AppAlert(activity).showStatus(Status)
                }

                return dataStatus
            }//end

            //lets get the data and insert into db
            if(!renderUI){
                return dataStatus
            }

            val data = dataStatus.getData<JSONObject>()!!

            updateHomeUI(activity,data)

            return dataStatus
        }//end


        /**
         * assetsStats
         */
        suspend fun fetchAllAssetStats(
                activity: Activity,
                type: String? = null,
                renderUI: Boolean = false
        ): Status{

            //lets get assets Stats

            var uri = "/stats/assets"

            if(type != null){
                uri = "$uri?type=$type"
            }

            val dataStatus = TnsApi(activity)
                                .get(uri)

            if(dataStatus.isError()){
                return dataStatus
            }//end

            //lets proccess the data
            

        }//end fun


        /**
        * updateHomeCoinCardColor
         */
        fun updateHomeCoinInfoCard(
                activity: Activity,
                coinInfo: JSONObject
        ){
           activity.apply{

               val animDuration = 1000L

               val coinName = coinInfo.getString("name")

               val symbol   = coinInfo.getString("symbol").toLowerCase()

               //user balance
               val userBalance = coinInfo.optDouble("balance",0.0)

               //split userBalance
               val userBalanceSplit = userBalance.toString().split(".")

               val coinColor = CoinsCore.getColor(this,symbol)

               val coinColorDarken = coinColor.darken(0.1)

               val view = coinInfoCard as CardView

               val oldColor = view.cardBackgroundColor.defaultColor

               val cardBgAnim = ObjectAnimator.ofInt(toolbar,
                       "backgroundColor",
                       oldColor,
                       coinColorDarken
               )

               cardBgAnim.addUpdateListener{ ls->

                   val animColor = ls.animatedValue as Int

                   view.setCardBackgroundColor(animColor)

                   activity.setStatusBarColor(animColor)
               }

               cardBgAnim.duration = animDuration

               cardBgAnim.setEvaluator(ArgbEvaluator())

               cardBgAnim.doOnStart {

                   activity.setToolbarTitle(coinName)

                   //update userBalance the same when
                   //the bg anim starts
                   userBalanceView.animate()
                           .alpha(0f)
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
                                       .start()

                           }.start()
               }//end do on start

               cardBgAnim.start()
           }//end

        }//end fun

        /**
         * update home coins UI
         */
        fun updateHomeUI(activity: Activity,
                         coinsInfo: JSONObject) = launch(UI){

            //we need TNS first so we extract it and put
            val tnsCoinInfo = coinsInfo.getJSONObject("tns")

            coinsInfo.remove("tns")

            val sortedData = mutableListOf<JSONObject>()

            sortedData.add(tnsCoinInfo)

            for(coinKey in coinsInfo.keys()){
                sortedData.add(coinsInfo.getJSONObject(coinKey))
            }

            val adapter = HomeCoinListAdapter(sortedData)

            val recyclerView = activity.findViewById<RecyclerView>(R.id.coinsListRecycler)

            if(recyclerView.adapter != null) {

                recyclerView.swapAdapter(adapter,true)

            }else{

                //update initial  ui
                updateHomeCoinInfoCard(activity,tnsCoinInfo)

                val calColumn = activity.calColumns(160)

                val gridLayout = GridLayoutManager(activity,calColumn)

                recyclerView.layoutManager = gridLayout


                recyclerView.adapter = adapter
            }

        }//end fun

    }
}

