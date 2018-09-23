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

package com.transcodium.tnsmoney.classes.jobs

import android.app.Service
import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.transcodium.tnsmoney.classes.WalletCore
import com.transcodium.tnsmoney.launchIO
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.launch

import kotlin.coroutines.experimental.CoroutineContext

class AssetsDataJob : JobService(), CoroutineScope {

    override val coroutineContext: CoroutineContext
                    get() = Dispatchers.IO

    /**
     * doWork
     */
    override fun onStartJob(job: JobParameters): Boolean {

        val ctx = this

        val job = launch {
                WalletCore.networkFetchUserAssets(ctx)
            Log.i("UPDATING USER STATS","True -----")
        }

        //wait for job to finish
        job.onJoin

        return false
    }//end fun

    /**
     * onStopJob
     */
    override fun onStopJob(job: JobParameters?): Boolean {
        return false
    }

}//end class