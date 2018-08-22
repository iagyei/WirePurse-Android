package com.transcodium.tnsmoney

import android.os.Bundle
import com.transcodium.tnsmoney.classes.Account
import com.transcodium.tnsmoney.classes.AppAlert
import kotlinx.android.synthetic.main.activity_email_login.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class EmailLoginActivity : RootActivity() {

    private val mActivity by lazy {
        this
    }

    private val appAlert by lazy{
        AppAlert(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_login)

        submitEmailLogin.setOnClickListener{processEmailLogin()}

        loginFormBackFab.setOnClickListener{
            mActivity.onBackPressed()
        }

    }

    /**
     * processEmailLogin
     */
    private fun processEmailLogin() = launch(UI){

        var hasError = false
        emailAddressInputLayout.error = ""
        passwordInputLayout.error = ""

        val emailAddress = emailAddressInput.text.toString()

        //validate email
        if(!emailAddress.isValidEmail()){
            emailAddressInputLayout.error = getString(R.string.valid_email_required)
            hasError = true
        }

        val password = passwordInput.text.toString()

        if(password.isEmpty()){
            passwordInputLayout.error = getString(R.string.password_required)
            hasError = true
        }

        //dont continue if error
        if(hasError){
            vibrate()
            return@launch
        }

        //process login
        val loginStatus = Account(mActivity).processEmailLogin(emailAddress,password)

        appAlert.showStatus(loginStatus)


        /**
         * if success log user in
         */
        if(loginStatus.isSuccess()){
            delay(3000)
            startClassActivity(HomeActivity::class.java)
        }

    }//end fun

}
