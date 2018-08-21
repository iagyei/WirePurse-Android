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
#  created_at 10/08/2018
 **/

package com.transcodium.tnsmoney.classes

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.transcodium.mothership.core.Status
import com.transcodium.tnsmoney.R
import com.transcodium.tnsmoney.SocialLoginVerification
import com.transcodium.tnsmoney.startClassActivity
import com.twitter.sdk.android.core.TwitterSession
import org.json.JSONObject

class SocialLoginCore(val activity: AppCompatActivity) {

    val appAlert by lazy {
        AppAlert(activity)
    }

    /**
     * processSocial Login
     */
    suspend fun processSocialLogin(socialName: String, socialData: Any) {

        val progress = Progress(activity)

        progress.show(
                title = R.string.loading,
                text = R.string.login_progress_text,
                bgColor = R.color.purpleDarken2
        )


        val requestParam:List<Pair<String,Any>> = when(socialName) {

            "google" -> {

                val googleUser = socialData as GoogleSignInAccount

                listOf(
                        Pair("id_token", (googleUser.idToken as Any)),
                        Pair("google_user_id",(googleUser.id as Any))
                )

            }//end if google

            "facebook" -> {

                val accessToken = socialData as AccessToken

                listOf(
                        Pair("access_token", accessToken.token),
                        Pair("facebook_user_id", accessToken.userId)
                )
            }//end if fb

            "twitter"  -> {

                val tSession = socialData as TwitterSession

                listOf(
                        Pair("twitter_user_id", tSession.userId),
                        Pair("auth_token", tSession.authToken.token),
                        Pair("auth_secret", tSession.authToken.secret)
                )

            }//end if twitter

           else -> {

               appAlert.error(activity.getString(R.string.unknown_social_provider, socialName))
                return
           }

        }//end when


        //lets validate user
        val loginStatus = TnsApi(activity)
                .post(
                        requestPath = "/auth/social/$socialName/login",
                        requestParams = requestParam,
                        hasAuth = false
                )
                .await()


        progress.hide()

        if(loginStatus.isError()){
            appAlert.showStatus(loginStatus)
            return
        }

        //lets get data
        val data = loginStatus.getData<JSONObject>()!!

       // Log.i("STATUS_DATA",data.toString())

        ///this is from the social auth class
        val requiresVerification = data.optBoolean("requires_verification",true)

        //from verifcation class
        val verificationData = data.optJSONObject("verification_data")

        if(requiresVerification){

            val verificationId = verificationData.optString("id","")

            Log.i("VerificationID",verificationId)

            val activityData = Bundle()
                activityData.putString("data",verificationData.toString())

            activity.startClassActivity(
                   activityClass =  SocialLoginVerification::class.java,
                    data = activityData
            )

        }//end if


    }//end fun



}//end  fun