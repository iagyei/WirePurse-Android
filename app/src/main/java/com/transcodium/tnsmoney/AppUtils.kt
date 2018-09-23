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
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.os.Vibrator
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.view.View
import android.widget.TextView
import com.firebase.jobdispatcher.*
import com.tapadoo.alerter.Alerter
import com.transcodium.tnsmoney.classes.*
import com.transcodium.tnsmoney.classes.jobs.AssetsDataJob
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.Main
import org.jetbrains.anko.find
import org.json.JSONObject
import java.util.*
import kotlin.reflect.KClass


/**
 * Activity.sharedPref
 * @return SharedPreference
 */
fun Context.sharedPref() = getDefaultSharedPreferences(this)

/**
 * secureSharedPre
 */
fun Context.secureSharedPref() = SecureSharedPref(this)

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

    //get decor view
    val decorView: View = window.decorView

    //fullscreen flag
    val uiOptions: Int = View.SYSTEM_UI_FLAG_FULLSCREEN

    //set ui visibility to full screen
    decorView.systemUiVisibility = uiOptions
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
fun getDeviceId(context: Context): String{

    val id = context.secureSharedPref().getString(DEVICE_ID,"")

    if(!id.isNullOrEmpty()){
       return  id!!
    }

    //lets generate a new one and save it
    val deviceId = UUID.randomUUID().toString()

    //lets save it
    context.secureSharedPref().put(DEVICE_ID, deviceId)

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

/**
 * isNetworkAvailable
 **/
fun Context.isNetworkAvailable(): Boolean {

    val conManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetInfo = conManager.activeNetworkInfo

    return (activeNetInfo != null && activeNetInfo.isConnected)
}//end


 fun Int.darken(fraction: Double): Int {

    var color = this
    var red = Color.red(color)
    var green = Color.green(color)
    var blue = Color.blue(color)
    red = darkenColor(red, fraction)
    green = darkenColor(green, fraction)
    blue = darkenColor(blue, fraction)
    val alpha = Color.alpha(color)

    return Color.argb(alpha, red, green, blue)
}

 private fun darkenColor(color: Int, fraction: Double): Int {
    return Math.max(color - color * fraction, 0.0).toInt()
 }

 fun Int.lighten(fraction: Double): Int {

    var color = this

    var red = Color.red(color)
    var green = Color.green(color)
    var blue = Color.blue(color)
    red = lightenColor(red, fraction)
    green = lightenColor(green, fraction)
    blue = lightenColor(blue, fraction)
    val alpha = Color.alpha(color)
    return Color.argb(alpha, red, green, blue)
}

    private fun lightenColor(color: Int, fraction: Double): Int {
        return Math.min(color + color * fraction, 255.0).toInt()
    }


/**
 * calculateColumns
 **/
fun Activity.calColumns(minWidth: Int): Int {

    val displayMetrics = resources.displayMetrics
    val dpWidth = displayMetrics.widthPixels / displayMetrics.density
    return (dpWidth / minWidth).toInt()
}


/**
 * rotate
 */
fun Bitmap.rotate(degree: Float, pivotX: Float, pivotY: Float): Bitmap{

    val m = Matrix().apply {
        postRotate(degree,pivotX,pivotY)
    }

    return Bitmap.createBitmap(this, 0, 0, width, height, m, true)
}


/**
 * updateHomeUICoinName
 */
fun Activity.setToolbarTitle(
        titleText: String
){

    val txtView = find<TextView>(R.id.topToolbarTitle)
        txtView.animate()
                .translationY(txtView.height.toFloat())
                .alpha(0f)
                .setDuration(500)
                .withEndAction {

                    txtView.text = titleText

                    txtView.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(500)
                            .start()

                }.start()
}//end


/**
 * setStatusBarColor
 **/
fun Activity.setStatusBarColor(color: Int){

    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
        return
    }

    window.statusBarColor = color
}


suspend fun <T> awaitEvent(block: (h: Handler<T>) -> Unit) : T {
    return suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
        try {
            block.invoke(Handler { t ->
                cont.resume(t)
            })
        } catch(e: Exception) {
            cont.resumeWithException(e)
        }
    }
}

/**
 * handleAppError
 **/
fun AppErrorUI(
        activity: Activity,
        status: Status,
        showAlert: Boolean? = true,
        killOnSevere: Boolean? = true
){

    if(showAlert!!){
        AppAlert(activity).showStatus(status)
    }

    if(killOnSevere!! && status.isSevere()){
        Account(activity).doLogout(status)
    }
}//end fun


/**
 *launchIO
 */
fun launchIO(block : CoroutineScope.() -> Unit) : Job{

    val scope = CoroutineScope(Dispatchers.IO)

    return  scope.launch{
            block.invoke(this)
    }
}

/**
 * launch io
 */
fun launchUI(block : CoroutineScope.() -> Unit) : Job{

    val scope = CoroutineScope(Dispatchers.Main)

    return  scope.launch{
        block.invoke(this)
    }
}


/**
 * startTask
 **/
fun <T : JobService> startPeriodicJob(
        activity: Activity,
        tag: String,
        clazz: KClass<T>,
        triggerInterval: Pair<Int,Int>
): com.firebase.jobdispatcher.Job {

    val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(activity))

    val job = dispatcher.newJobBuilder()
            .setService(clazz.java)
            .setTag(tag)
            .setLifetime(Lifetime.FOREVER)
            .setTrigger(Trigger.executionWindow(
                    triggerInterval.first,
                    triggerInterval.second)
            )
            .setRecurring(true)
            .setReplaceCurrent(true)
            .setConstraints(Constraint.ON_ANY_NETWORK)
            .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
            .build()

    dispatcher.mustSchedule(job)

    return job
}//end fun