package com.transcodium.tnsmoney

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.transcodium.tnsmoney.classes.Crypt
import com.facebook.stetho.Stetho
import org.jetbrains.anko.startActivityForResult

class AppEntry : AppCompatActivity() {

    private val IN_APP_REQUEST_CODE = 12

    private val mActivity by lazy{ this }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
        //init app
        val isOk = initApp()

        //if initialization is okay, proceed with app
        if(isOk) {

            if (BuildConfig.DEBUG) {
                Stetho.initializeWithDefaults(this)
            }

            val introCompleted = sharedPref().getBoolean("intro_completed", false)

            //if if intro is not completed, then next activity class is intro
            if (!introCompleted) {
                startClassActivity(AppIntroActivity::class.java,true)
            }

            //if user is already logged in
            else if(isLoggedIn()) {

                val i = Intent(this,PinCodeAuthActivity::class.java)

                //if user is logged in, lets get check for inApp Auth
                startActivityForResult(i,IN_APP_REQUEST_CODE)

            } else {

                //start login
                startClassActivity(LoginActivity::class.java,true)
            }

        }//end if everything is ok

    }//end fun


    fun initApp(): Boolean{

        //check if app key exists, if not create a new one
        val createAppKey = Crypt.createAppKey(this)

        if(createAppKey.isError()){

             AlertDialog.Builder(this,R.style.Theme_AppCompat)
                    .setTitle(R.string.initialization_error)
                    .setMessage(R.string.initialization_error_message)
                    .setPositiveButton(R.string.ok){dialog,_ ->
                        mActivity.finish()
             }.show()

            return false
        }//end if error

        /**
         * calling getDeviceID will create it when it doesnt exist,
         * this creates a unique uuid onetime for identifying user device
         */
        getDeviceId(this)

        return true
    }//end fun


    /**
     * listen to inApp Auth
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode != IN_APP_REQUEST_CODE){
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        val status = data!!.getStatus()!!

        println("----- DATA : ${status.toJsonString()}")
    }


}//end class
