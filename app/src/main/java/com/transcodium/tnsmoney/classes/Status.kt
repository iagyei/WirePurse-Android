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
#  @author Razak Zakari <razak@transcodium.com>
#  https://transcodium.com
 **/

package com.transcodium.mothership.core

import android.app.Activity
import android.util.Log
import com.transcodium.tnsmoney.R
import com.transcodium.tnsmoney.classes.StatusCodes
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

/**
 * Status after a function call
 */
 object Status{


       private var isError: Boolean = false
       private var isSuccess: Boolean = false
       private var isNeutral: Boolean = false
       private var message: Any? = null
       private var type: String = ""
       private var data : Any? = null

       private var isSevere = false


       private var code = 0


        fun instance(): Status{
            return this
        }


        /**
         * isSevere
         */
         fun isSevere(): Boolean{
            return this.isSevere
        }

        /**
         * success
         */
        fun success(
                message: Any? = "",
                data: Any? = null,
                code: Int? = null
        ): Status{

            this.isError = false

            this.isSuccess = true

            this.message = message

            this.type = "success"

            this.data = data

            this.code = code ?: StatusCodes.SUCCESS

            return this
        }//end fun

        /**
         * success
         */
        fun neutral(message: String="",
                    data: Any? = null,
                    code: Int? = null): Status{

            this.isError = false

            this.isSuccess = false

            this.isNeutral = true

            this.message = message

            this.type = "success"

            this.data = data

            this.code = code ?: StatusCodes.NEUTRAL

            return this

        }//end fun


        /**
        * messageless success
        */
        fun success(data: Any? = null): Status{

            this.code = StatusCodes.SUCCESS

            return success("",data)
        }

        /**
         * error
         */
        fun error(
                message: Any? = "",
                data: Any? = null,
                code: Int? = null,
                isSevere: Boolean? = false
        ): Status{

            this.isError = true

            this.isSuccess = false

            this.isNeutral = false

            this.type = "error"

            this.message = message

            this.data = data

            this.code = code ?: StatusCodes.FAILED

            this.isSevere = isSevere!!

            return this
        }


        /**
        * set
        */
        fun set(status: Boolean,
                message: String = "",
                data: Any? = null,
                code: Int? = null
            ): Status{

            return if(status){
                this.success(message,data,code)
            }else{
                this.error(message,data,code)
            }

        } //end fun



        //isError
        fun isError(): Boolean{
            return this.isError
        }

        //succeeded
        fun isSuccess(): Boolean{
            return this.isSuccess
        }

        fun isNeutral(): Boolean{
            return this.isNeutral
        }

        //get message
        fun message(activity: Activity? = null): String{

            return when(this.message){

                is Int -> {

                    if(activity == null){
                        this.message.toString()
                    }else{
                        activity.getString(this.message as Int)
                    }
                }

                else -> {
                    this.message.toString()
                }
            }
        }//end

        /**
        * getMessage
        */
        fun getMessage(activity: Activity? = null): String {
            return message(activity)
        }

        /**
         * setMessage
         */
        fun setMessage(message: Any): Status{
           this.message = message
            return instance()
        }

        /**
        * code
        */
        fun code(): Int {
            return this.code
        }

        /**
        * set code
        */
        fun setCode(code: Int): Status{
            this.code = code
            return instance()
        }

        /**
         * data
         */
        fun data(): Any?{
            return this.data
        }

        fun <T>getData(): T?{
            return this.data as T
        }

        /**
        * setData
        */
        fun <T> setData(data: T): Status{
            this.data = data
            return instance()
        }


         fun fromJson(data: JSONObject): Status {

            val alertType = data.getString("type")

            if(alertType == "error"){
                return this.error(
                      message = data.optString("message",""),
                      data    =  data.get("data") as Any,
                      code   =  data.optInt("code",StatusCodes.FAILED),
                      isSevere = data.optBoolean("isSevere",false)

                )
            }else if(alertType == "success") {

                return this.success(
                        data.optString("message",""),
                        data.opt("data") as Any,
                        data.optInt("code",StatusCodes.SUCCESS)
                )

            }else{

                return this.neutral(
                        data.optString("message",""),
                        data.opt("data") as Any,
                        data.optInt("code",StatusCodes.NEUTRAL)
                )

            }//end if

        }//end

        /*
        * toJson
         */
        fun toJsonString(): String {
            return toJsonObject().toString()
        }

        /**
         * to Json Object
         */
       fun toJsonObject(): JSONObject {
           return JSONObject(mapOf(
                   "type"     to type,
                   "message"  to message,
                   "code"     to code,
                   "data"     to data,
                   "isSevere" to isSevere
           ))
       }//end

}//end class


