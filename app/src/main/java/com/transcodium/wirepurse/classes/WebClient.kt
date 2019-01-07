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
#  created_at 21/09/2018
 **/

package com.transcodium.wirepurse.classes

import android.util.Log
import com.transcodium.wirepurse.IO
import com.transcodium.wirepurse.R
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import okhttp3.*

class WebClient {

    companion object {

        //client
        private val client by lazy{
             OkHttpClient()
        }

        /**
        * getRequests
         */
        suspend fun getRequest(
                url : String,
                params: List<Pair<String,Any>>? = null,
                headers: MutableMap<String,String>? = null
        ): Status {

            val urlParser = HttpUrl.parse(url)

            if(urlParser == null){
                Log.e("MALFORMED_URL","Failed to parse url $url")
                return Status.error(R.string.http_error)
            }

            val urlBuilder = urlParser.newBuilder()

            //add url parameters
            if(params != null && params.isNotEmpty()){
                params.forEach{pair ->
                    urlBuilder.addQueryParameter(pair.first,pair.second.toString())
                }
            }

            val finalUrl = urlBuilder.build().toString()


            var request = Request.Builder()
                                .url(finalUrl)


            //if we have headers, we add it
            if(headers != null && headers.isNotEmpty()){
                request.headers(Headers.of(headers))
            }//end


            return execRequest(request.build())
        }//end fun


        /**
         * postRequest
         */
        suspend fun postRequest(
                url : String,
                params: List<Pair<String,Any>>? = null,
                headers: MutableMap<String,String>? = null
        ): Status {

            val request =  Request.Builder()

            if(headers != null && headers.isNotEmpty()){
                request.headers(Headers.of(headers))
            }

            val formBody = MultipartBody.Builder()
                                .setType(MultipartBody.FORM)

            if(params != null && params.isNotEmpty()){

                params.forEach{pair ->
                    formBody.addFormDataPart(pair.first,pair.second.toString())
                }
            }//end if params


            val requestBuilder =  request.url(url)
                                         .post(formBody.build())
                                         .build()

            return execRequest(requestBuilder)
        }//end fun


        /**
        * execRequest
         */
        suspend fun execRequest(request: Request): Status {

            //run in background
            val httpData: Deferred<Status> = IO.async {

                return@async try {

                    val response = client.newCall(request)
                            .execute()

                    if (!response.isSuccessful) {
                        Log.e("HTTP_ERROR", " " +
                                "CODE: ${response.code()}  " +
                                "MESSAGE: ${response.message()} ${response.code()} " +
                                "URL : ${request.url()}"
                        )

                        Status.error(R.string.server_connection_failed)

                    } else {

                        //if success
                        val body = response.body()?.string() ?: ""

                        Status.success(data = body)
                    }

                } catch (e: Exception) {

                    Log.e("HTTP_EXCEPTION", e.message)

                    e.printStackTrace()

                    Status.error(R.string.server_connection_failed)
                }//end exception catching

            }//end background


            return httpData.await()
        }//end fun

    }//end companion
}//end