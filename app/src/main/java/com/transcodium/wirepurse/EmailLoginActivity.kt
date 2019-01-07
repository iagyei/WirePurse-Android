package com.transcodium.wirepurse

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.transcodium.wirepurse.classes.Account
import com.transcodium.wirepurse.classes.AppAlert
import com.transcodium.wirepurse.classes.Progress
import kotlinx.android.synthetic.main.activity_email_login.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch


class EmailLoginActivity : RootActivity() {

    private val mActivity by lazy {
        this
    }

    private val appAlert by lazy {
        AppAlert(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_login)

        submitEmailLogin.setOnClickListener { processEmailLogin() }

        loginFormBackFab.setOnClickListener {
            mActivity.onBackPressed()
        }

        signup.setOnClickListener { startClassActivity(EmailSignUpActivity::class.java) }

    }

    /**
     * processEmailLogin
     */
    private fun processEmailLogin() = launchUI {

        var hasError = false
        emailAddressInputLayout.error = ""
        passwordInputLayout.error = ""

        val emailAddress = emailAddressInput.text.toString()

        //validate email
        if (!emailAddress.isValidEmail()) {
            emailAddressInputLayout.error = getString(R.string.valid_email_required)
            hasError = true
        }

        val password = passwordInput.text.toString()

        if (password.isEmpty()) {
            passwordInputLayout.error = getString(R.string.password_required)
            hasError = true
        }

        //dont continue if error
        if (hasError) {
            vibrate()
            return@launchUI
        }


        val progress = Progress(mActivity)

        progress.show(
                title = R.string.loading,
                text = R.string.login_progress_text,
                bgColor = R.color.purple
        )

        //process login
        val loginStatus = Account(mActivity).processEmailLogin(emailAddress, password)

        progress.hide()

        appAlert.showStatus(loginStatus)

        /**
         * if success log user in
         */
        if (loginStatus.isSuccess()) {
            delay(2000)
            startClassActivity(PinCodeAuthActivity::class.java, true)
        }

    }//end fun


    /**
     * onActivityResult
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //if its pin code activity auth
        if (!(requestCode == INAPP_AUTH_REQUEST_CODE &&
              resultCode == Activity.RESULT_OK)) {
            return
        }

        val status = data?.getStatus()!!


        if (status.isSuccess()) {
                startClassActivity(HomeActivity::class.java, true)
                return
        }//end

    }//end fun


}//end class

