package com.transcodium.tnsmoney

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.transcodium.tnsmoney.classes.Crypt
import org.jetbrains.anko.alert
import com.facebook.stetho.Stetho
class AppEntry : AppCompatActivity() {


    private lateinit var nextActivityClass: Class<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //init app
        val isOk = initApp()

        //if initialization is okay, proceed with app
        if(isOk) {

            if(BuildConfig.DEBUG){
                Stetho.initializeWithDefaults(this)
            }

            val introCompleted = sharedPref().getBoolean("intro_completed", false)

            //if if intro is not completed, then next activity class is intro
            if (!introCompleted) {
                nextActivityClass = AppIntroActivity::class.java
            } else {
                nextActivityClass = LoginActivity::class.java
            }


            startClassActivity(nextActivityClass, true)

        }//end if

    }//end fun


    fun initApp(): Boolean{

        //check if app key exists, if not create a new one
        val createAppKey = Crypt.createAppKey(this)

        if(createAppKey.isError()){

            val alert = alert(R.string.initialization_error_message,
                                R.string.initialization_error
            )

            alert.isCancelable = false
            alert.positiveButton(R.string.ok){
                this.finish()
            }

            alert.show()

            return false
        }//end if error

        /**
         * calling getDeviceID will create it when it doesnt exist,
         * this creates a unique uuid onetime for identifying user device
         */
        getDeviceId()

        return true
    }//end fun


}//end class
