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

package com.transcodium.tnsmoney.classes

import android.util.Log
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.coroutines.experimental.bg

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
                params: MutableMap<String,Any>? = null,
                headers: MutableMap<String,String>? = null
        ): Status {

            val urlParser = HttpUrl.parse(url)

            if(urlParser == null){
                Log.e("MALFORMED_URL","Failed to parse url $url")
                return Status.error("http_error")
            }

            val urlBuilder = urlParser.newBuilder()

            //add url parameters
            if(params != null && params.isNotEmpty()){
                params.forEach{(key,value) ->
                    urlBuilder.addQueryParameter(key,value.toString())
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
        fun postRequest(
                url : String,
                params: MutableMap<String,Any>? = null,
                headers: MutableMap<String,String>? = null
        ): Status {

                

        }//end fun


        /**
        * execRequest
         */
        suspend fun execRequest(request: Request): Status {

            //run in background
            val httpData: Deferred<Status> = bg {

                return@bg try {

                    val response = client.newCall(request)
                            .execute()

                    if (!response.isSuccessful) {
                        Log.e("HTTP_ERROR", "code: ${response.code()} Message: ${response.message()} ${response.code()}")

                        Status.error("server_request_failed")

                    } else {

                        //if success
                        val body = response.body().toString()

                        Status.success(data = body)
                    }

                } catch (e: Exception) {

                    Log.e("HTTP_EXCEPTION", e.message)

                    e.printStackTrace()

                    Status.error("server_request_failed")
                }//end exception catching

            }//end background


            return httpData.await()
        }//end fun

    }//end companion
}//end