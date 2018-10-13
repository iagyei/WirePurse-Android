package com.transcodium.tnsmoney

import android.app.Application
import android.content.ComponentCallbacks
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.os.CancellationSignal
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.transcodium.tnsmoney.classes.FingerprintCore
import kotlinx.android.synthetic.main.activity_pin_code_auth.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

class PinCodeAuthActivity : RootActivity() {

    var fingerPrintFailCount = 0

    var fingerprintCancellationSignal: CancellationSignal? = null

    var isFingerPrintPaused = false

    val hasFingerprint   by lazy {
        sharedPref().getBoolean("fingerprint_enabled",false)
    }

    //fingerprint UI delay Job
    var fpUICancelDelayJob: Job? = null


    //lets get pincode
    val pinCodeHash by lazy { sharedPref().getString("pin_code", null) }


    override fun onCreate(savedInstanceState: Bundle?) {

        //if not set, open the activity
        if (pinCodeHash == null) {
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
    }


    /**
     * HandleFingerPrint
     */
    fun handleFingerprint(){

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return
        }

        //if fingerprint was not enabled, remove the fingerprint
        if(!hasFingerprint){
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
                onAuthError = {_,_ -> showPincodeUI() },
                onAuthFailed = { handleAuthFailure() },
                onAuthSuccess = { _-> handleAuthSuccess() }
        )

        if(authStatus.isError()){
            showPincodeUI()
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
     * handle authFailed
     */
    fun handleAuthFailure(){

        if(fingerPrintFailCount == 3){
            showPincodeUI()
            return
        }

        //increment
        fingerPrintFailCount += 1
    }//end



    fun handleAuthSuccess(){
        toast("Success")
    }


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
    fun showPincodeUI(){

        //if fingerprint is showing, cancel it
        fingerprintCancellationSignal?.cancel()

        pincodeViewParent.visibility = View.VISIBLE
        fingerprintViewParent.visibility = View.GONE

    }//end fun

}//end class


