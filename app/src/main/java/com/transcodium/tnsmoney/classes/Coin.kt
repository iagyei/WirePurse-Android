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
import android.util.Log
import androidx.core.content.ContextCompat
import com.transcodium.tnsmoney.classes.Status
import com.transcodium.tnsmoney.R

class Coin {

    companion object {

        private val colors by lazy{

            mapOf(
                "tns" to R.color.colorTNS,
                "eth" to R.color.colorETH,
                "btc" to R.color.colorBTC
            )

        }//end colors

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
         * fetch userCoins
         */
        suspend fun fetchUserCoins(activity: Activity): Status {

            val uri = "/user-coins"

            val dataStatus = TnsApi(activity)
                    .get(uri)

            if(dataStatus.isError()){
                Log.e("FETCH_USER_COIN_INFO",dataStatus.toJsonString())
                return dataStatus
            }


            println(dataStatus.toJsonString())

            return Status.success()
        }//end

    }
}

