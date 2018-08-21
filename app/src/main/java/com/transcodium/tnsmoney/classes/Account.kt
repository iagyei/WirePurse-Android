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
#  created_at 27/07/2018
 **/

package com.transcodium.tnsmoney.classes

import android.app.Activity
import android.os.Bundle
import androidx.core.content.edit
import com.transcodium.mothership.core.Status
import com.transcodium.tnsmoney.*
import org.json.JSONObject


class Account(val activity: Activity) {


    //if user is logged in
    fun isLoggedIn(): Boolean {

        val authInfo = activity.sharedPref()
                .getString("auth_info",null)
                ?: return false


        val authInfoJson = JSONObject(authInfo)

        val expiryMillis = authInfoJson.getLong("expiry")

        val now = UTCDate().timeInMillis

        if(expiryMillis < now){
            return false
        }

        return true
    }//end


    /**
     * process Login
     */
    suspend fun processEmailLogin(
                          email: String,
                          password: String
    ): Status {

        val progress = Progress(activity)

        progress.show(
                title = R.string.loading,
                text = R.string.login_progress_text,
                bgColor = R.color.purple
        )

        val loginParam = listOf(
                Pair("email",email),
                Pair("password",password)
        )

        //process login
        val loginStatus = TnsApi(activity).post(
                "/auth/login",
                loginParam
        ).await()

        //hide progress
        progress.hide()

        if(loginStatus.isError()){
            return loginStatus
        }

        //lets save the login info
        val data = loginStatus.getData<JSONObject?>()

        if(data == null){
            return Status.error(
                    message = R.string.unexpected_error,
                    code =  StatusCodes.EMAIL_LOGIN_DATA_NULL
            )
        }

        //save data
        activity.secureSharedPref().put("user_info", data)

        return Status.success(message = R.string.login_success)
    }//end

    /**
     * doLogout
     */
    fun doLogout(status: Status? = null){

        activity.sharedPref().edit().remove("user_info").apply()

        var bundle: Bundle? = null

        if(status != null){
            bundle = Bundle()
            bundle.putString("status",status.toJsonString())
        }

        activity.startClassActivity(
                activityClass = LoginActivity::class.java,
                clearActivityStack = true,
                data = bundle
        )

    }
}