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
#  created_at 25/08/2018
 **/

package com.transcodium.tnsmoney.classes.workers

import android.app.Activity
import android.util.Log
import androidx.work.Worker
import com.transcodium.tnsmoney.classes.CoinsCore
import com.transcodium.tnsmoney.classes.TnsApi
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject

class UserAssetsWorker : Worker() {


    val ctx by lazy {
        applicationContext
    }

    val mActivity = ctx as Activity

    /**
     * doWork
     */
    override fun doWork(): Result {

        launch {

            //lets get user balace
            val assetsCoinsStatus = CoinsCore.fetchUserCoins(mActivity,false)

            if(assetsCoinsStatus.isError()){
                Log.e("USER_ASSETS_WORKER",assetsCoinsStatus.message())
                return@launch
            }

            //lets get the data
            val data = assetsCoinsStatus.getData<JSONObject>()

            println(data)
        }//end launch

        return Result.SUCCESS
    }//end fun

}//end class