package com.transcodium.wirepurse


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.os.CancellationSignal
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.transcodium.wirepurse.classes.Account
import com.transcodium.wirepurse.classes.FingerprintCore
import com.transcodium.wirepurse.classes.Status
import kotlinx.android.synthetic.main.activity_pin_code_auth.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

class PinCodeAuthActivity : RootActivity() {

    val mActivity by lazy { this }

    var fingerPrintRetryAttempts = 5

    var pinCodeRetryAttempt = 5

    var fingerprintCancellationSignal: CancellationSignal? = null

    var isFingerPrintPaused = false

    val hasFingerprint   by lazy {
        sharedPref().getBoolean("fingerprint_enabled",false)
    }

    //fingerprint UI delay Job
    var fpUICancelDelayJob: Job? = null


    //lets get pincode
    val savePinCodeHash by lazy { sharedPref().getString("pin_code", null) }



    override fun onCreate(savedInstanceState: Bundle?) {

        //if not set, open the activity
        if (savePinCodeHash == null) {
            startClassActivity(SetPinCodeActivity::class.java, true)
            return
        }//end if

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_code_auth)

        setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

        handleFingerprint()

        //switch from fingerprint to pincode auth
        usePinCode.setOnClickListener { showPincodeUI() }

        if(hasFingerprint) {
            //switch to fingerprint
            useFingerprint.setOnClickListener { handleFingerprint() }
        }else{
            useFingerprint.visibility = View.GONE
        }//end if


        /**
         * pincode continue btn
         */
        pincodeContinueBtn.setOnClickListener { handlePinCodeAuth() }
    }//end fun

    /**
     * cancel fingerprint if paused
     */
    override fun onPause() {
        super.onPause()
        fingerprintCancellationSignal?.cancel()
        isFingerPrintPaused = true
    }

    override fun onResume() {
        super.onResume()

        if(isFingerPrintPaused && hasFingerprint){
            handleFingerprint()
        }
    }//end on resume


    /**
     * processPinCodeAuth
     */
    fun handlePinCodeAuth(){

       val pinCode = pinCodeInput.text!!

       pinCodeInputLayout.error = ""

       if(pinCode.length < 4){
           vibrate()
           pinCodeInputLayout.error = getString(R.string.pincode_lenght_error)
           return
       }

       //lets hash the pin and compare
       val userHashedPin = pinCode.toString().toSha256()

       //validate
       if(savePinCodeHash!!.compareTo(userHashedPin!!) == 0){
           handleAuthSuccess()
           return
       }//end if


        /**
         * Auth Failed
         */
        vibrate()

        pinCodeRetryAttempt -= 1

        pinCodeInputLayout.error = getString(R.string.invalid_pincode,pinCodeRetryAttempt.toString())

        //after 5 times of failure, fail permanently
        if(pinCodeRetryAttempt == 0){
            handleAuthFailure()
        }//end if

    }//end fun


    /**
     * HandleFingerPrint
     */
    fun handleFingerprint(){

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return
        }

        //if fingerprint was not enabled, remove the fingerprint
        if(!hasFingerprint){
            showPincodeUI(true)
            return
        }

        val fingerprintCore = FingerprintCore(this)

        //here, lets check if user has deleted fingerprint enrollment data
        //to avoid crashing
        if(!fingerprintCore.isEnabled()){
            longToast(R.string.fingerprint_records_not_found)

            //disable it
            sharedPref().edit{
                putBoolean("fingerprint_enabled",false)
            }

            //remove use fingerprint btn
            useFingerprint.visibility = View.GONE

            return
        }//end if

        //cancel any fpUICancelDelayJob job
        fpUICancelDelayJob?.cancel()

        //show fingerpring UI
        showFingerprintUI()


        val authStatus = fingerprintCore.authenticate(
                onAuthError = {errCode,errMsg -> onFingerprintError(errCode,errMsg) },
                onAuthFailed = { onFingerprintFailed() },
                onAuthSuccess = { _-> handleAuthSuccess() }
        )

        if(authStatus.isError()){
            showPincodeUI(true)
            return
        }

        fingerprintCancellationSignal = authStatus.getData()


        //also we have noticed that the current activity with fingerprint
        //gets closed after some time of inactivity
        //so after 30 seconds of no touch, we will disable fingerprint to use
        //pin
        fpUICancelDelayJob = UI.launch {

            delay(30000L)

            longToast(R.string.idle_fingerprint_ui_closed)

            fingerprintCancellationSignal?.cancel()

            showPincodeUI()
        }//end avoid wasting resources when idle

    }//end

    /**
     * onFingerprintError
     */
    fun onFingerprintError(errCode: Int, errMsg: String?){

        Log.e("FINGERPRINT_ERROR","Error Code: $errCode : Message: $errMsg")

        val err = if(errMsg != null){
            errMsg
        }else{
            getString(R.string.fingerprint_failed_msg)
        }

        longToast(err)

        showPincodeUI(true)
    }//end fun


    /**
     * handle authFailed
     */
    fun onFingerprintFailed(){

        //decrement
        fingerPrintRetryAttempts -= 1

        toast(getString(R.string.retry_msg,fingerPrintRetryAttempts.toString()))


        if(fingerPrintRetryAttempts == 0){

            toast(R.string.fingerprint_auth_failed)

            showPincodeUI(true)
            return
        }

    }//end


    /**
     * authSuccess
     */
    private fun handleAuthSuccess(){

        fpUICancelDelayJob?.cancel()

        longToast(R.string.in_app_auth_success)

        val data = Intent()
        data.setStatus(Status.success())
        setResult(Activity.RESULT_OK,data)

        finish()
    }//end fun


    /**
     * handleAuthFailure
     */
    private fun handleAuthFailure(){

        fpUICancelDelayJob?.cancel()

        val appLockExpiry = System.currentTimeMillis() + (APP_LOCK_EXPIRY * 6000)

        sharedPref().edit{
            putLong("app_locked_for",appLockExpiry)
            putBoolean("force_login",true)
        }

        val data = Intent()
        data.setStatus(Status.error(R.string.in_app_auth_failed))
        setResult(Activity.RESULT_OK,data)

        //show logout dialog
        dialog {
            setCancelable(false)
            setMessage(getString(R.string.in_app_auth_failed,APP_LOCK_EXPIRY.toString()))
            setPositiveButton(R.string.ok){d,w->
                d.dismiss()
                Account(mActivity).doLogout()
                mActivity.finish()
            }
        }

    }//end

    /**
     * showFingerprintUI
     */
    fun showFingerprintUI(){
        pincodeViewParent.visibility = View.GONE
        fingerprintViewParent.visibility = View.VISIBLE
    }

    /**
     * showPincodeUI
     */
    fun showPincodeUI(hideUseFingerprint: Boolean? = false){

        //if fingerprint is showing, cancel it
        fingerprintCancellationSignal?.cancel()

        fpUICancelDelayJob?.cancel()

        pinCodeInput.requestFocus()

        pincodeViewParent.visibility = View.VISIBLE
        fingerprintViewParent.visibility = View.GONE

        if(hideUseFingerprint!!){
            useFingerprint.visibility = View.GONE
        }

    }//end fun


}//end class


