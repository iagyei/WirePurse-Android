package com.transcodium.tnsmoney

import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment

open class DialogBase  : DialogFragment()  {


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val win = dialog?.window

        if(win != null){ win.setWindowAnimations(R.style.AppDialog) }
    }


    override fun onStart() {
        super.onStart()

        val win = dialog?.window

        val maxWidth = toDip(dialog.context,380f).toInt()

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

        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
    }
}