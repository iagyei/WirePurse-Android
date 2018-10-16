package com.transcodium.tnsmoney

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.transcodium.tnsmoney.classes.Crypt
import com.facebook.stetho.Stetho
import com.transcodium.tnsmoney.classes.Account
import org.jetbrains.anko.startActivityForResult

class AppEntry : AppCompatActivity() {

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

                startInAppAuth()

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

             dialog {
                    setTitle(R.string.initialization_error)
                    setMessage(R.string.initialization_error_message)
                    setPositiveButton(R.string.ok){_,_ -> mActivity.finish()}
             }

            return false
        }//end if error

        /**
         * calling getDeviceID will create it when it doesnt exist,
         * this creates a unique uuid onetime for identifying user device
         */
        getDeviceId(this)

        return true
    }//end fun


    //onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode != INAPP_AUTH_REQUEST_CODE){
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        val status = data!!.getStatus()!!

        if(status.isError()){
            Account(this).doLogout(status)
            return
        }

        startClassActivity(HomeActivity::class.java,true)
    }//end on activity result



}//end class
