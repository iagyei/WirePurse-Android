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
#  created_at 28/08/2018
 **/

package com.transcodium.tnsmoney.classes

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.transcodium.tnsmoney.*
import com.transcodium.tnsmoney.db.AppDB
import org.json.JSONObject


class HomeCoinListAdapter(
        val activity: Activity,
        val dataSet: MutableList<JSONObject>
) : RecyclerView.Adapter<HomeCoinListAdapter.RViewHolder>() {

    val context: Context by lazy{
       activity as Context
    }

    var selectedItemPos = 0


    class RViewHolder(card: CardView): RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RViewHolder {

        //context = parent.context

        //activity = (context as Activity)

        val card = LayoutInflater
                    .from(context)
                    .inflate(
                            R.layout.home_coins_list_layout,
                            parent,
                            false) as CardView


        return RViewHolder(card)
    }

    override fun onBindViewHolder(holder: RViewHolder, position: Int) {

        val coinInfo = dataSet[position]

        val coinName = coinInfo.getString("name")

        val symbol   = coinInfo.getString("symbol").toLowerCase()


        val card = holder.itemView

        card.setOnClickListener{v-> handleCardClick(v,position)}

         activity.vibrate()

        val coinColor = WalletCore.getColor(activity!!,symbol)

        //val coinColorLight = coinColor.lighten(0.1)

        val coinColorAlpha = ColorUtils.setAlphaComponent(coinColor,200)

        val coinIcon = WalletCore.getIcon(symbol)

        val resources = activity!!.resources


        val bgImg = ResourcesCompat.getDrawable(
                resources,
                coinIcon,
                null
        )

        bgImg?.alpha = 180


        card.background = bgImg


        card.findViewById<ConstraintLayout>(R.id.contentMain)
                .setBackgroundColor(coinColorAlpha)

        card.findViewById<LinearLayout>(R.id.coinNameWrapper)
            .setBackgroundColor(coinColor)

        val coinNameTvText = "$coinName ($symbol)"

        card.findViewById<TextView>(R.id.coinNameTv).text = coinNameTvText

    }//en fun

    /**
     * handleCardClick
     */
    fun handleCardClick(v: View,position: Int){

       if(selectedItemPos == position){
           return
       }

        //change current coin
        selectedItemPos = position

       val coinInfo = dataSet[position]

       WalletCore.homeUpdateCurrentAssetInfo(activity,coinInfo)
    }//end fun

    override fun getItemCount() = dataSet.size
}