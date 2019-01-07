package com.transcodium.wirepurse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.transcodium.wirepurse.classes.AppAlert
import com.transcodium.wirepurse.classes.Progress
import kotlinx.android.synthetic.main.activity_email_sign_up.*


class EmailSignUpActivity : AppCompatActivity() {


    private val mActivity by lazy {
        this
    }

    private val appAlert by lazy {
        AppAlert(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_sign_up)


        formBackFab.setOnClickListener {
            mActivity.onBackPressed()
        }

        loginBtn.setOnClickListener {
            startClassActivity(LoginActivity::class.java,true)
        }

    }//end fun



    /**
     * processEmailLogin
     */
    private fun processEmailSignup() = launchUI {

        var hasError = false
        emailAddressInputLayout.error = ""
        passwordInputLayout.error = ""
        confrimPasswordInputLayout.error = ""

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

        val passwordConfirm = confrimPasswordInput.text.toString()

        if(password.compareTo(passwordConfirm) != 0){
            confrimPasswordInputLayout.error = getString(R.string.passwords_do_not_match)
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



    }//end fun



}//end activity class
