/**
# Copyright 2018 - Transcodium Ltd.
#  All rights reserved. This program and the accompanying materials
#  are made available under the terms of the  Apache License v2.0 which accompanies this distribution.
#
#  The Apache License v2.0 is available at
#  http://www.opensource.org/licenses/apache2.0.php
#
#  You are required to redistribute this code under the same licenses.
#
#  Project TNSMoney
#  @author Razak Zakari <razak@transcodium.com>
#  https://transcodium.com
#  created_at 31/07/2018
 **/

package com.transcodium.tnsmoney.classes

import android.app.Activity
import android.os.Build
import android.view.Gravity
import android.view.Window
import android.transition.Slide


class Anim(val mActivity: Activity) {

    fun slideWindow(
            onEnter: Int? = null,
            onExit: Int? = null
    ) {

       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

           val slide = Slide()

           slide.duration = 300

           val slideRight = slide

           val win = mActivity.window

           win.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

           win.allowEnterTransitionOverlap = false
           win.allowReturnTransitionOverlap = false

           if(onEnter != null) {

               val windowEnterSlide = slide

               windowEnterSlide.slideEdge = onEnter

               win.exitTransition = windowEnterSlide
           }


           if(onExit != null) {

               val windowExitSlide = slide

               windowExitSlide.slideEdge = onExit

               win.exitTransition = windowExitSlide
           }


           win.enterTransition = slideRight
       }

    }//end
}