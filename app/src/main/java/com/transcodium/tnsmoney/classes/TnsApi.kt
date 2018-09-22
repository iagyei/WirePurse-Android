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
#  created_at 29/07/2018
 **/

package com.transcodium.tnsmoney.classes

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.gson.JsonObject
import com.transcodium.tnsmoney.*
import org.json.JSONObject
import java.lang.Exception
import java.util.*



class TnsApi(val context: Context){

        /**
         * requestHeaders
         */
       val getRequestHeaders by lazy{

            val appName = context.getString(R.string.app_name)
            val appVersion = BuildConfig.VERSION_NAME
            val serviceId =  "wallet"

            val lang = Locale.getDefault().language

            val deviceName = android.os.Build.MODEL
            val deviceMan = android.os.Build.MANUFACTURER ?: ""
            val deviceBuild = android.os.Build.DEVICE
            val osVer = Build.VERSION.RELEASE

            val userAgent = "$appName $serviceId/$appVersion (Linux; Android $osVer; $deviceMan $deviceName )"

            //Log.e("USER_AGENT",userAgent)

             mutableMapOf(
                    "x-device-id"  to getDeviceId(context),
                    "x-service-name" to  serviceId,
                    "x-api-key"    to API_KEY,
                    "User-Agent"   to userAgent,
                    "Accept-Language" to lang
            )
        }


        /**
        * RequestAuth
        **/
        fun getRequestAuth(
                addToHeaders: Boolean? = true
        ): Status{

            //lets get user auth info
            val authInfo = SecureSharedPref(context)
                                 .getJsonObject("user_info",null)


            if(authInfo == null){
                return Status.error(
                        message = R.string.auth_not_found,
                        isSevere = true
                )
            }//end if

            //fetch the access token from the file
            val accessTokenStatus = getAccessToken(authInfo)


            if(accessTokenStatus.isError()){
                return accessTokenStatus
            }//end if error


            //get access token
            val accessToken = accessTokenStatus.getData<String>()

            val userId = authInfo.optString("user_id","")

           //add access token
            authInfo.put("access_token",accessToken)

            //if attach to headers is true
            if(addToHeaders!!){
                getRequestHeaders["x-user-id"] = userId
                getRequestHeaders["Authorization"] = "Bearer $accessToken"
            }

            return Status.success(data = authInfo)
        }


        /**
         * post
         */
        suspend fun post(
                requestPath: String,
                params: List<Pair<String,Any>>? = null,
                headers: MutableMap<String,String>? = null,
                hasAuth: Boolean? = true
        ): Status {

            return  processAPIRequest(
                     requestType = "post",
                     requestPath =requestPath,
                     params = params,
                     headers = headers,
                     hasAuth = hasAuth
            )
        }//end fun

    /**
     * get
     */
    suspend fun get(
            requestPath: String,
            params: List<Pair<String,Any>>? = null,
            headers: MutableMap<String,String>? = null,
            hasAuth: Boolean? = true
    ): Status {

        return  processAPIRequest(
                requestType = "get",
                requestPath =requestPath,
                params = params,
                headers = headers,
                hasAuth = hasAuth
        )
    }//end fun


    /**
     * proccessApiRequest
     */
    suspend fun processAPIRequest(
            requestType: String,
            requestPath: String,
            params: List<Pair<String,Any>>? = null,
            headers: MutableMap<String,String>? = null,
            hasAuth: Boolean? = true
    ): Status {

        val requestHeaders =  getRequestHeaders

        if(headers != null && headers.isNotEmpty()){
            requestHeaders.putAll(headers)
        }

        //Log.e("LOL",JSONObject(requestHeaders).toString())

        //if request hasAuth
        if(hasAuth!!){
            val requestAuthStatus = getRequestAuth(true)

            if(requestAuthStatus.isError()){
                return requestAuthStatus
            }
        }//end if request has auth

        Log.e("LOL",JSONObject(requestHeaders).toString())


        val url = "$API_ENDPOINT/$requestPath"

        val requestStatus = if(requestType == "get"){

            WebClient.getRequest(
                    url = url,
                    headers = requestHeaders,
                    params = params
            )

        }else{

            WebClient.postRequest(
                    url = url,
                    headers = requestHeaders,
                    params = params
            )
        }//end else

        if(requestStatus.isError()){
            return requestStatus
        }


        val responseData = requestStatus.getData<String>()!!


        return try {

           val responseStatusJson = JSONObject(responseData)

            Status.fromJson(responseStatusJson)

        }catch(e: Exception){
            Status.neutral()
        }

    }//end fun



    /**
     * getAccessToken Check if access token has expired, if it has expired
     * regenerate before send it back
     */
    fun getAccessToken(
            authInfo : JSONObject
    ): Status {


        val tokenData: JSONObject? = authInfo.getJSONObject("token_data") ?: null

        if(tokenData == null){
            return Status.error(
                    message = R.string.auth_required,
                    isSevere = true
            )
        }


        val expiry = tokenData.optInt("expiry",0)

        val accessToken = tokenData.optString("token","")

        if(accessToken.isNullOrEmpty()){
            return Status.error(
                    message = R.string.auth_required,
                    isSevere = true
            )
        }


        //lets get current time in milliseconds
        val now = Calendar.getInstance().timeInMillis


        if(expiry > now){
            return Status.success(data = accessToken)
        }

        //if we here,, then lets update access token
        return Status.success(data = accessToken)
    }//end

}