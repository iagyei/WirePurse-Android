package com.transcodium.tnsmoney

import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_set_app_pin.*

class setAppPinActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_app_pin)

        setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark))

        savePinBtn.setOnClickListener {

            val pinCode = pinCodeInput.text

            pinCodeInputLayout.error = ""

            if(pinCode?.length?.compareTo(4) != 0){
                vibrate()
                pinCodeInputLayout.error = getString(R.string.pincode_lenght_error)
                return@setOnClickListener
            }


            val confirmPinCode = confirmPinCodeInput.text!!

            confirmPinCodeInputLayout.error = ""

            if(!confirmPinCode.equals(pinCode)){
                vibrate()
                confirmPinCodeInputLayout.error = getString(R.string.pincodes_dont_match)
                return@setOnClickListener
            }//end if



            //enable fingerprint
        }
    }
}
