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
#  created_at 26/08/2018
 **/

package com.transcodium.tnsmoney.classes

import android.content.Context
import androidx.core.content.ContextCompat
import com.transcodium.tnsmoney.R

class CoinInfo {

    companion object {

        private val colors by lazy{

            mapOf(
                "tns" to R.color.colorTNS,
                "eth" to R.color.colorETH,
                "btc" to R.color.colorBTC
            )

        }//end colors

        /**
         * getColor
         */
        fun  getColor(ctx: Context, coin: String): Int{
            return ContextCompat.getColor(
                    ctx,
                    colors[coin] ?: R.color.colorPrimaryDark
            )
        }

    }
}

