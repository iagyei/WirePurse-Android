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
#  created_at 09/08/2018
 **/

package com.transcodium.tnsmoney.classes

import android.app.Activity
import com.tapadoo.alerter.Alerter
import com.transcodium.mothership.core.Status
import com.transcodium.tnsmoney.R
import com.transcodium.tnsmoney.vibrate

class AppAlert(val activity: Activity) {

    private val duration = 8000L

   private fun alertObj(): Alerter {

        if(Alerter.isShowing){ Alerter.hide() }

       return Alerter.create(activity)
                .enableVibration(false)
                .enableIconPulse(true)
                .setDismissable(true)
                .enableSwipeToDismiss()
                .enableInfiniteDuration(true)
    }

    /**
     * error
     */
    fun error(message: Any,autoClose : Boolean = false){

        val alertObj = alertObj()

        val messageStr = if(message is Int){
            activity.getString(message)
        }else{
            message.toString()
        }

        alertObj.setBackgroundColorRes(R.color.colorAccent)
                .setText(messageStr)
                .setIcon(R.drawable.ic_error_outline_pink_24dp)

        if(autoClose){
            alertObj.enableInfiniteDuration(false)
                    .setDuration(duration)
        }

        activity.vibrate()
        alertObj.show()
    }


    /**
     * success
     */
    fun success(message: Any,autoClose : Boolean = true) {

        val alertObj = alertObj()

        val messageStr = if(message is Int){
            activity.getString(message)
        }else{
            message.toString()
        }

        alertObj.setBackgroundColorRes(R.color.green)
                .setText(messageStr)
                .setIcon(R.drawable.ic_done_all_black_24dp)

        if (autoClose) {
            alertObj.enableInfiniteDuration(false)
                    .setDuration(duration)
        }

        alertObj.show()
    }//end if


    /**
     * showStatus
     */
    fun showStatus(status: Status){


        if(Alerter.isShowing){ Alerter.hide() }

        if(status.isError()){
            error(status.message(activity))
        } else if(status.isSuccess()){
            success(status.getMessage(activity))
        }

    }//end fun


}//end class