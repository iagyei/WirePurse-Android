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
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import com.transcodium.tnsmoney.classes.Status
import com.transcodium.tnsmoney.*
import org.json.JSONObject


class Account(val context: Context) {


    //if user is logged in
    fun isLoggedIn(): Boolean {

        val authInfo = context.sharedPref()
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
                          activity: Activity,
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
        )

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
        saveUserInfo(data)

        return Status.success(message = R.string.login_success)
    }//end


    /**
     * save UserInfo
     */
    fun saveUserInfo(userInfo: JSONObject): Status{

       if(userInfo.has("email")){
           userInfo.put("user_email",userInfo.getString("email"))
       }

       return context.secureSharedPref().put("user_info",userInfo)
    }//end fun

    /**
     * doLogout
     */
    fun doLogout(
            status: Status? = null,
            activity: Activity ? = null
    ){

        context.sharedPref().edit{
            remove("user_data")
        }

        var bundle: Bundle? = null

        if(status != null){

            Log.e("LOGOUT_STATUS",status.toJsonString())

            bundle = Bundle()
            bundle.putString("status",status.toJsonString())
        }

       activity?.startClassActivity(
               activityClass = LoginActivity::class.java,
               clearActivityStack = true,
               data = bundle
        )

    }
}