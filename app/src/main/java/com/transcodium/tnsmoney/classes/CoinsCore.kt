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

import android.app.Activity
import android.content.Context
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.transcodium.tnsmoney.R
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject
import com.transcodium.tnsmoney.calColumns


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

            val rc = activity.findViewById<RecyclerView>(R.id.coinsListRecycler)

            if(rc.adapter != null) {

                rc.swapAdapter(adapter,true)

            }else{

                val toolbar = activity.findViewById<Toolbar>(R.id.topToolbarTitle)

                val tnsCoinName = tnsCoinInfo.getString("")

                val calColumn = activity.calColumns(160)

                val gl = GridLayoutManager(activity,calColumn)

                rc.layoutManager = gl


                rc.adapter = adapter
            }

        }//end fun

    }
}

