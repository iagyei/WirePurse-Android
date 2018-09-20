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
#  created_at 20/09/2018
 **/

package com.transcodium.tnsmoney.classes

import androidx.work.*
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KClass

class TNSWorkManager {

    companion object {


        /**
         * addTask
         */
        fun <T : Worker>addTask(
                workClass: Class<T>,
                interval: Long? = null,
                constrains: Map<String,Any>? = null
        ){

            val work = if(interval == null){
                OneTimeWorkRequest.Builder(workClass)
            }else{

                Instant.ofEpochMilli(interval)
                PeriodicWorkRequest.Builder(workClass,interval)
            }
                WorkManager.getInstance()
                            .enqueue(works)
        }
    }
}