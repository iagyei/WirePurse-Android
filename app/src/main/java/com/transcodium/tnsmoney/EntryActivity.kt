package com.transcodium.tnsmoney

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.transcodium.tnsmoney.classes.Crypt
import com.facebook.stetho.Stetho

class AppEntry : AppCompatActivity() {


    private lateinit var nextActivityClass: Class<*>

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
                nextActivityClass = AppIntroActivity::class.java
            }

            else if(isLoggedIn()){
              nextActivityClass = HomeActivity::class.java
            }

            else {
                nextActivityClass = SocialLoginActivity::class.java
            }

            nextActivityClass = setAppPinActivity::class.java

            startClassActivity(nextActivityClass, true)

        }//end if

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


}//end class
