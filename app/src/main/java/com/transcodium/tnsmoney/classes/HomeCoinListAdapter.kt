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
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RotateDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import com.transcodium.tnsmoney.R
import com.transcodium.tnsmoney.rotate
import org.json.JSONObject


class HomeCoinListAdapter(val dataSet: MutableList<JSONObject>)
    : RecyclerView.Adapter<HomeCoinListAdapter.RViewHolder>() {

    var context: Context? = null

    class RViewHolder(card: CardView): RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RViewHolder {

        context = parent.context

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

        val activity = (context as Activity)

        val card = holder.itemView

        val coinColor = CoinsCore.getColor(activity,symbol)

        val coinIcon = CoinsCore.getIcon(symbol)

        val r = activity.resources

        val bgImg = BitmapFactory
                            .decodeResource(r,coinIcon)
                            .rotate(45f,50f,30f)
                            .toDrawable(r)

        bgImg.alpha = 180

        card.background = bgImg

        card.findViewById<ConstraintLayout>(R.id.contentMain)
                .setBackgroundColor(coinColor)

        card.findViewById<LinearLayout>(R.id.coinNameWrapper)
            .setBackgroundColor(coinColor)

        val coinNameTvText = "$coinName ($symbol)"

        card.findViewById<TextView>(R.id.coinName)
                .text = coinNameTvText
    }

    override fun getItemCount() = dataSet.size
}