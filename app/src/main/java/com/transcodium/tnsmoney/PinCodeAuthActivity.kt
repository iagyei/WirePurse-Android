package com.transcodium.tnsmoney

import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.view.View
import androidx.core.content.ContextCompat
import com.transcodium.tnsmoney.classes.FingerprintCore
import kotlinx.android.synthetic.main.activity_pin_code_auth.*
import org.jetbrains.anko.toast

class PinCodeAuthActivity : RootActivity() {

    var fingerPrintFailCount = 0

    var fingerprintCancellationSignal: CancellationSignal? = null

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
        userPinCode.setOnClickListener { showPincodeUI() }
    }//end fun


    /**
     * HandleFingerPrint
     */
    fun handleFingerprint(){

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return
        }

        val hasFingerprint = sharedPref().getBoolean("fingerprint_enabled",false)

        //if fingerprint was not enabled, remove the fingerprint
        if(!hasFingerprint){ return }

        //show fingerpring UI
        showFingerprintUI()

        val fingerprintCore = FingerprintCore(this)

        val authStatus = fingerprintCore.authenticate(
                onAuthError = {_,_ -> showPincodeUI() },
                onAuthFailed = { handleAuthFailure() },
                onAuthSuccess = { _-> handleAuthSuccess() }
        )

        if(authStatus.isError()){
            showPincodeUI()
            return
        }


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
        pincodeViewParent.visibility = View.VISIBLE
        fingerprintViewParent.visibility = View.GONE

    }//end fun

}//end class


