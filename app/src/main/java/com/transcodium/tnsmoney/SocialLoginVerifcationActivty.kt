package com.transcodium.tnsmoney

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.transcodium.tnsmoney.classes.AppAlert
import com.transcodium.tnsmoney.classes.Progress
import com.transcodium.tnsmoney.classes.TnsApi
import com.transcodium.tnsmoney.classes.VerificationCode
import kotlinx.android.synthetic.main.activity_social_login_verification.*
import kotlinx.android.synthetic.main.verification_code_layout.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject
import java.lang.Integer.parseInt

class SocialLoginVerification : AppCompatActivity() {


    lateinit var verificationData: JSONObject

    lateinit var title: String

    private val mActivity by lazy { this}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_social_login_verification)

        val bundle = intent.extras

        val dataStr =  bundle?.getString("data")

        verificationData = JSONObject(dataStr)

        //resend code
        resendBtn.setOnClickListener { resendCode() }

        //verify
        verifyCodeBtn.setOnClickListener{ verifyCode() }

        //process waitTime
        processWaitTime(verificationData)
    }

    /**
     * proccessVerificationCallback
     */
    private fun processWaitTime(data: JSONObject){

        val waitTime = data.optLong("wait_time",0)


        //Log.i("Wait Time",waitTime.toString())

        if(waitTime <= 0){
            waitTimeTextView.visibility = View.GONE
            resendBtn?.visibility = View.VISIBLE
        }else{

            //for now set resend to hideen
            waitTimeTextView.visibility = View.VISIBLE
            resendBtn?.visibility = View.GONE

            //1000 ms = 1 second
            val countDownInterval = 1000L

            //convert seconds to milliseconds
            val waitTimeMs = waitTime * countDownInterval

            val waitTimeText = getString(R.string.resend_after)

            val countDown = object: CountDownTimer(waitTimeMs,countDownInterval){

                override fun onFinish() {

                    waitTimeTextView?.visibility = View.GONE
                    resendBtn?.visibility = View.VISIBLE

                }

                override fun onTick(time: Long) {

                    val t =  "$waitTimeText ${time / countDownInterval}"

                    waitTimeTextView.text = t

                    // Log.i("TICK",time.toString())
                }
            }//end coundown timer

            countDown.start()
        }//end if

    }//end fun

    /**
     * resendCode
     */
    private  fun resendCode() = launch(UI){

        val id = verificationData.optString("id")

        val resendStatus = VerificationCode(mActivity).resend(id)

        if(resendStatus.isError()){
            AppAlert(mActivity).showStatus(resendStatus)
            return@launch
        }

        AppAlert(mActivity).success(R.string.code_sent_to_your_email,true)

        //lets get validation data and reshow the count down
        verificationData = resendStatus.getData<JSONObject>()!!

        processWaitTime(verificationData)
    }//end fun

    /**
     * verify code
     */
    private fun verifyCode() = launch(UI){

        val appAlert = AppAlert(mActivity)

        val p = Progress(mActivity)

        p.show(
                text = R.string.verifying_code,
                bgColor = R.color.colorPrimaryDark
        )


        val codeInt = try{
             parseInt(verificationCodeInput.text.toString())
        }catch(e: NumberFormatException){
            appAlert.error(R.string.invalid_code)
            return@launch
        }

        val verificationId = verificationData.optString("id","")

        val uri = "/auth/social/verify"

        val postParams = listOf<Pair<String,Any>>(
                Pair("verification_id",verificationId),
                Pair("verification_code",codeInt)
        )

        val veriyStatus = TnsApi(mActivity)
                .post(uri,postParams)



        p.hide()


        if(veriyStatus.isError()){
            appAlert.showStatus(veriyStatus)
            return@launch
        }

        //lets save the data


    }//end fun

}//end class
