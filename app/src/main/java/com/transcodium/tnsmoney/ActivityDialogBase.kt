package com.transcodium.tnsmoney

import android.os.Bundle
import android.os.PersistableBundle
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

open class ActivityDialogBase  : RootActivity()  {



    override fun onStart() {
        super.onStart()

        val win = window

        val maxWidth = toDip(this,380f).toInt()

        if(win != null) {


            val lp = WindowManager.LayoutParams()

            lp.copyFrom(win.attributes)

            var dialogWidth = maxWidth

            val curWidth = lp.width

            if (curWidth < maxWidth){
                dialogWidth = ViewGroup.LayoutParams.MATCH_PARENT
            }

            win.setLayout(
                    dialogWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }//end id

    }
}