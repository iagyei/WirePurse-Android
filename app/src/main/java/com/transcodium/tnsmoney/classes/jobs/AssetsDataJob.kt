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

import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO

import kotlin.coroutines.experimental.CoroutineContext

class AssetsDataJob : JobService(), CoroutineScope {

    override val coroutineContext: CoroutineContext
                    get() = Dispatchers.IO

    /**
     * doWork
     */
    override fun onStartJob(job: JobParameters): Boolean {

        val ctx = applicationContext


        return false
    }//end fun

    /**
     * onStopJob
     */
    override fun onStopJob(job: JobParameters?): Boolean {
        return false
    }

}//end class