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
#  created_at 15/08/2018
 **/

package com.transcodium.wirepurse.classes

import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.transcodium.wirepurse.R
import com.transcodium.wirepurse.R.id.waitTimeTextView
import kotlinx.android.synthetic.main.verification_code_layout.*
import org.json.JSONObject

class VerificationCode(val activity: AppCompatActivity) {


    /**
     * resend
     */
    suspend fun resendCode(dbId: String,hasAuth:Boolean? = true): Status {

        val p = Progress(activity)

        p.show(
                text = R.string.resending_verification_code,
                bgColor = R.color.colorPrimaryDark
        )


        val uri = "/auth/verification-code/$dbId/resend"

        val resendStatus = TnsApi(activity)
                .post(
                        requestPath = uri,
                        hasAuth = hasAuth!!
                )


        p.hide()

        if(resendStatus.isError()){
            AppAlert(activity).showStatus(resendStatus)
            return resendStatus
        }

        AppAlert(activity).success(R.string.code_sent_to_your_email,true)

        //lets get validation data and reshow the count down
        val verificationData = resendStatus.getData<JSONObject>()!!

        processWaitTime(verificationData)

        return resendStatus
    }//end fun


    /**
     * proccessVerificationCallback
     */
    private fun processWaitTime(data: JSONObject){

        val waitTime = data.optLong("wait_time",0)


        //Log.i("Wait Time",waitTime.toString())

        if(waitTime <= 0){
            activity.waitTimeTextView.visibility = View.GONE
            activity.requestCodeBtn?.visibility = View.VISIBLE
        }else{

            //for now set resend to hideen
            activity.waitTimeTextView.visibility = View.VISIBLE
            activity.requestCodeBtn?.visibility = View.GONE

            //1000 ms = 1 second
            val countDownInterval = 1000L

            //convert seconds to milliseconds
            val waitTimeMs = waitTime * countDownInterval

            val waitTimeText =  activity.getString(R.string.resend_after)

            val countDown = object: CountDownTimer(waitTimeMs,countDownInterval){

                override fun onFinish() {

                    activity.waitTimeTextView?.visibility = View.GONE
                    activity.requestCodeBtn?.visibility = View.VISIBLE

                }

                override fun onTick(time: Long) {

                    val t =  "$waitTimeText ${time / countDownInterval}"

                    activity.waitTimeTextView.text = t

                    // Log.i("TICK",time.toString())
                }
            }//end coundown timer

            countDown.start()
        }//end if

    }//end fun


}//end class