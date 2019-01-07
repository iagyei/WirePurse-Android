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

package com.transcodium.wirepurse.classes

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import com.transcodium.wirepurse.*
import org.json.JSONObject


class Account(private val mContext: Context) {


    //if user is logged in
    fun isLoggedIn(): Boolean {


        val authInfoJson = mContext.secureSharedPref()
                                .getJsonObject(USER_AUTH_INFO,null)
        ?: return false


        if(!(authInfoJson.has("user_id") ||
           authInfoJson.has("email") ||
           authInfoJson.has("token_data")
        )){
            return false
        }//end if

        return true
    }//end


    /**
     * process Login
     */
    suspend fun processEmailLogin(
         email: String,
         password: String
    ): Status {


        val loginParam = listOf(
                Pair("email",email as Any),
                Pair("password",password as Any)
        )

        //process login
        val loginStatus = TnsApi(mContext).post(
                "/auth/login",
                loginParam
        )


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

       return mContext.secureSharedPref().put(USER_AUTH_INFO,userInfo)
    }//end fun

    /**
     * doLogout
     */
    fun doLogout(
        status: Status? = null
    ){

        mContext.sharedPref().edit{
            remove(USER_AUTH_INFO)
        }

        var bundle: Bundle? = null

        if(status != null){

            Log.e("LOGOUT_STATUS",status.toJsonString())

            bundle = Bundle()
            bundle.putString("status",status.toJsonString())
        }

      if(mContext is Activity){
            mContext.startClassActivity(
               activityClass = LoginActivity::class.java,
               clearActivityStack = true,
               data = bundle
            )
       }

    }
}