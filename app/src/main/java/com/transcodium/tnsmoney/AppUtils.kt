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
#  Project Android
#  @author Razak Zakari <razak@transcodium.com>
#  https://transcodium.com
#  created_at 26/07/2018
 **/

package com.transcodium.tnsmoney

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.tapadoo.alerter.Alerter
import com.transcodium.mothership.core.Status
import com.transcodium.tnsmoney.classes.Account
import com.transcodium.tnsmoney.classes.SecureSharedPref
import org.json.JSONObject
import java.util.*
import kotlin.reflect.KClass

/**
 * Activity.sharedPref
 * @return SharedPreference
 */
fun Activity.sharedPref() = getPreferences(MODE_PRIVATE)

/**
 * secureSharedPre
 */
fun Activity.secureSharedPref() = SecureSharedPref(this)

/**
 * minmizeApp
 */
fun Activity.minimizeApp(){
    val i = Intent(Intent.ACTION_MAIN)
    i.addCategory(Intent.CATEGORY_HOME)
    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(i)
}//end minimize app


/**
 * hideStatusBar
 */
fun Activity.hideStatusBar(){

    //jellybean and lower
    if(Build.VERSION.SDK_INT < 16){
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
    else{//else if greater than android 4

        //get decor view
        val decorView: View = window.decorView

        //fullscreen flag
        val uiOptions: Int = View.SYSTEM_UI_FLAG_FULLSCREEN

        //set ui visibility to full screen
        decorView.systemUiVisibility = uiOptions
    }//end if

}//end function


/**
 *
 */
/**
 *startNewActivity
 **/
fun <T> Activity.startClassActivity(
        activityClass: Class<T>,
        clearActivityStack: Boolean = false,
        data: Bundle? = null
){

    //leave this intent to auth intent
    val i = Intent(this,activityClass)

    //put extra data
    if(data != null) {
        i.putExtras(data)
    }

    //if clear activity Stack is true
    if(clearActivityStack) {
        i.flags = (
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
          )
    }//end if

    startActivity(i)

}//end fun

/**
 * UTCDate
 * @return Calendar
 **/
fun UTCDate() = Calendar.getInstance(TimeZone.getTimeZone("GMT"))

/**
 * isLoggedIn
 * @return Boolean
 **/
fun Activity.isLoggedIn() = Account(this).isLoggedIn()

/**
 *isValidEmail
 */
fun String.isValidEmail(): Boolean{
    if(this.isEmpty()){
        return false
    }

    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}//end

/**
 * getOrCreateDeviceId
 **/
fun Activity.getDeviceId(): String{

    val id = secureSharedPref().getString(DEVICE_ID,"")

    if(!id.isNullOrEmpty()){
       return  id!!
    }

    //lets generate a new one and save it
    val deviceId = UUID.randomUUID().toString()

    //lets save it
    secureSharedPref().put(DEVICE_ID, deviceId)

    return deviceId
}//end

/**
 *vibrate
 **/
fun Activity.vibrate(pattern: List<Long>? = listOf(0L,15L)){

    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    if(!vibrator.hasVibrator()){
        return
    }

    //val vibrationE = VibrationEffect.
    vibrator.vibrate(pattern!!.toLongArray(),-1)
}


/**
 * putAll
 */
fun JSONObject.merge(newData: JSONObject): JSONObject{

    for(newDataKey in newData.keys()){
        this.put(newDataKey,newData[newDataKey])
    }

    return this
}

/**
 * FromBitsToByte
 **/
fun Int.fromBitToByte(): Int{
    return (this / java.lang.Byte.SIZE).toInt()
}

/**
 * convertToBitMetric
 **/
fun Int.toBitMetric(): Int{
    return (this * java.lang.Byte.SIZE)
}

/**
 *isMashmelloOrHigher
 **/
fun isMarshmallowOrHeigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M


/**
 *hideAlert
 **/
fun hideAlert(activity: Activity) {
    if(Alerter.isShowing){ Alerter.hide() }
}//end