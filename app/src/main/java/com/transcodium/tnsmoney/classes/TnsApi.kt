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
import java.util.*


class TnsApi(val activity: Activity) {

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
     fun  get(
             requestPath: String,
             requestParams: List<Pair<String,Any>>? = listOf(),
             hasAuth: Boolean = true
    ): Deferred<Status> {

        val request = requestPath.httpGet(requestParams)

        //execute api request
       return execApiRequest(request,hasAuth)
    }//end get requests

    /**
     * get requests
     * @param requestPath The path of the uri
     * @return
     */
      fun  post(
            requestPath: String,
            requestParams: List<Pair<String,Any>>? = listOf(),
            hasAuth: Boolean = true
    ): Deferred<Status> {

        val request = requestPath.httpPost(requestParams)

        //execute api request
        return execApiRequest(request,hasAuth)
    }//end get requests


    /**
     * execApiRequest
     */
   private  fun execApiRequest(
            requestObj: Request,
            hasAuth: Boolean = true): Deferred<Status>{

        if(hasAuth){

         }

        //run in background
        val apiData: Deferred<Status> = bg {

            if(!API_ENDPOINT.startsWith("https")){
               //return@bg Status.error(R.string.secure_url_required)
            }

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

        return apiData
    }//end fun

}//end class