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

import android.app.Activity
import android.os.Build
import android.util.Log
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.transcodium.mothership.core.Status
import com.transcodium.tnsmoney.*
import kotlinx.coroutines.experimental.Deferred
import org.jetbrains.anko.coroutines.experimental.bg
import org.json.JSONObject
import java.util.*


class TnsApi(val activity: Activity) {

     private var HAS_AUTH = true

    /**
     * requestHeaders
     */
    val requestHeaders: MutableMap<String,String> by lazy {

        val appName = activity.getString(R.string.app_name)
        val appVersion = BuildConfig.VERSION_NAME
        val serviceId =  "wallet"

        val lang = Locale.getDefault().language

        val deviceName = android.os.Build.MODEL
        val deviceMan = android.os.Build.MANUFACTURER
        val deviceBuild = android.os.Build.DEVICE
        val osVer = Build.VERSION.RELEASE

        val userAgent = "$appName $serviceId/$appVersion (Linux; Android $osVer; $deviceMan $deviceName )"

        Log.e("USER_AGENT",userAgent)

        mutableMapOf(
                "x-device-id"  to activity.getDeviceId(),
                "x-service-id" to  serviceId,
                "x-api-key"    to API_KEY,
                "User-Agent"   to userAgent,
                "Accept-Language" to lang
        )
    }

    /**
     * init
     */
    init {
        //gobal headers
        FuelManager.instance.baseHeaders = requestHeaders
        FuelManager.instance.basePath = API_ENDPOINT
    }//end

    /**
     * get requests
     * @param requestPath The path of the uri
     * @return
     */
     suspend fun  get(
             requestPath: String,
             requestParams: List<Pair<String,Any>>? = listOf(),
             hasAuth: Boolean = true
    ): Status {

        HAS_AUTH = hasAuth

        val request = requestPath.httpGet(requestParams)

        //execute api request
       return execApiRequest(request)
    }//end get requests

    /**
     * get requests
     * @param requestPath The path of the uri
     * @return
     */
      suspend fun  post(
            requestPath: String,
            requestParams: List<Pair<String,Any>>? = listOf(),
            hasAuth: Boolean? = true
     ): Status {

        HAS_AUTH = hasAuth!!

        val request = requestPath.httpPost(requestParams)

        //execute api request
        return execApiRequest(request)
    }//end get requests


    /*
     * setHasAuth
     */
    fun setHasAuth(hasAuth: Boolean): TnsApi{
        HAS_AUTH = hasAuth
        return this
    }


    /**
     * execApiRequest
     */
   private suspend fun execApiRequest(
            requestObj: Request
    ): Status {


        //run in background
        val apiData: Deferred<Status> = bg {

            if(!API_ENDPOINT.startsWith("https")){
               //return@bg Status.error(R.string.secure_url_required)
            }

            //if it has auth, process it
            if(HAS_AUTH){

                //lets get user auth info
                val authInfo = SecureSharedPref(activity)
                                    .getJsonObject("user_info",null)

                if(authInfo == null){
                    return@bg Status.error(
                            message = "auth_not_found",
                            isSevere = true
                    )
                }//end if

                val accessTokenStatus = getAccessToken(authInfo)

                if(accessTokenStatus.isError()){

                    if(accessTokenStatus.isSevere()){
                        Account(activity).doLogout(accessTokenStatus)
                    }

                    return@bg accessTokenStatus
                }//end if error

                //get access token
                val accessToken = accessTokenStatus.getData<String>()

                val userId = authInfo.optString("user_id","")

                //var userEmail = authInfo.optString("user_email")

                //add the headers
                requestObj.header(
                        Pair("x-user-id",userId),
                        Pair("Authorization","Bearer $accessToken")
                )
            }//end handle auth data


            try{

                val (request, response, result) = requestObj.responseJson()

                val statusCode = response.statusCode


                if(statusCode != 200){

                    Log.e("HTTP_ERROR","${request.url} returned $statusCode - ${response.responseMessage}")

                    return@bg Status.error(activity.getString(R.string.network_request_failed))

                }else{

                    return@bg Status.fromJson(result.get().obj())

                }

            }catch (e: Exception){

                Log.e("HTTP_ERROR",e.message)
                e.printStackTrace()

                return@bg Status.error(R.string.network_request_failed)
            }
        }//end

        val requestStatus = apiData.await()

        //check if there is an error, but check if its a severe,
        //a severe
       if(requestStatus.isError() && requestStatus.isSevere()){

           Log.e("CRITICAL_ERROR",requestStatus.toJsonString())

            val code =  requestStatus.code()
            val err = activity.getString(R.string.critical_error_message,code.toString())

            val status = Status.error(
                    message = err,
                    code = code
            )

            Account(activity).doLogout(status)

            return status
        }//end if

        return requestStatus
    }//end fun


    /**
     * getAccessToken Check if access token has expired, if it has expired
     * regenerate before send it back
     */
    fun getAccessToken(authInfo : JSONObject): Status {

        val tokenData: JSONObject? = authInfo.optJSONObject("token_data") ?: null

        if(tokenData == null){
            return Status.error(
                    message = R.string.auth_required,
                    isSevere = true
            )
        }

        val expiry = tokenData.optInt("expiry",0)
        val accessToken = tokenData.optString("access_token","")

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


}//end class