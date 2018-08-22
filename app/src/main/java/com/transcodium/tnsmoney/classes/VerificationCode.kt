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
#  created_at 15/08/2018
 **/

package com.transcodium.tnsmoney.classes

import androidx.appcompat.app.AppCompatActivity
import com.transcodium.mothership.core.Status
import com.transcodium.tnsmoney.R
import kotlinx.android.synthetic.main.verification_code_layout.*

class VerificationCode(val activity: AppCompatActivity) {


    /**
     * resend
     */
    suspend fun resend(id: String): Status {

        val p = Progress(activity)

        p.show(
                text = R.string.resending_verification_code,
                bgColor = R.color.colorPrimaryDark
        )


        val uri = "/auth/verification-code/$id/resend"

        val resendStatus = TnsApi(activity)
                .post(uri)


        p.hide()

        return resendStatus
    }//end fun



}//end class