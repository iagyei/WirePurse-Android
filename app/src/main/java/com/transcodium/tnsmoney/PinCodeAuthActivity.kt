package com.transcodium.tnsmoney

import android.hardware.biometrics.BiometricPrompt
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.transcodium.tnsmoney.classes.FingerprintCore

class PinCodeAuthActivity : RootActivity() {

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


        val fprintCore = FingerprintCore(this)

        fprintCore.handleUI()
    }//end fun



}//end class


