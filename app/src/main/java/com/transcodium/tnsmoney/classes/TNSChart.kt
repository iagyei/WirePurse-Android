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
#  created_at 23/09/2018
 **/

package com.transcodium.tnsmoney.classes

import android.app.Activity
import android.content.Context
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.firebase.jobdispatcher.Constraint
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.google.gson.JsonObject
import com.transcodium.tnsmoney.R
import kotlinx.android.synthetic.main.chart_marker_view.view.*
import kotlinx.android.synthetic.main.home_coin_info.*
import org.json.JSONArray
import org.json.JSONObject
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*


class TNSChart {


   // var mChart: LineChart? = null

    val timezone by lazy {
        TimeZone.getDefault()
    }

    val serverDateTimePattern by lazy {
        
    }

    val userDateTimePattern  by lazy {
         SimpleDateFormat.getInstance()
    }

    fun processHomeCoinInfoGraph(
            activity: Activity,
            data: JSONArray
    ){


        val dataSize = data.length() - 1

        activity.apply{

            val entries = mutableListOf<Entry>()

            val circleColors = mutableListOf<Int>()

            val timeList = mutableListOf<String>()

            val whiteAlpha70 = ContextCompat.getColor(activity,R.color.whiteAlpha70)

            for(i in 0..dataSize){

                val dataObj = data[i] as JSONObject

                val price = dataObj.optDouble("price",0.0)

                entries.add(Entry(i.toFloat(),price.toFloat()))

                circleColors.add(whiteAlpha70)
            }

            //entries.reverse()

            //println(entries)

            val dataSet = LineDataSet(entries,null)

            dataSet.disableDashedLine()

            dataSet.setDrawFilled(true)
            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
            dataSet.circleColors = circleColors
            dataSet.fillColor = ContextCompat.getColor(activity,R.color.whiteAlpha50P)
            dataSet.setDrawValues(false)
            dataSet.isHighlightEnabled = true


            val lineData = LineData(dataSet)

            val mChart = coinInfoChart

            mChart.setDragEnabled(true)
            mChart.setScaleEnabled(true)
            mChart.setPinchZoom(false)

            val xAxis = mChart.xAxis

            xAxis.setDrawAxisLine(false)
            xAxis.setDrawLabels(false)
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.axisLineColor = whiteAlpha70

            xAxis.setAvoidFirstLastClipping(true)

            //disable legend
            mChart.legend.isEnabled = false

            val axisLeft = mChart.axisLeft
            axisLeft.setDrawGridLines(false)
            axisLeft.textColor = ContextCompat.getColor(activity,R.color.whiteAlpha70)

            mChart.axisRight.isEnabled = false

            mChart.description.isEnabled = false

            val marker = graphMarkerView(activity,data)

            marker.chartView = mChart

            mChart.marker = marker

            mChart.setDrawMarkers(true)

            mChart.data = lineData

            mChart.setNoDataText("")

            mChart.setDrawBorders(false)

            mChart.invalidate()

        }//ane apply activity

    }//end fun


    /**
     * markerClass
     */
    open class graphMarkerView(
            mContext: Context,
            private val dataArray: JSONArray,
            layoutId: Int? = R.layout.chart_marker_view
    ): MarkerView(mContext,layoutId!!){

        private val priceTextView by lazy { findViewById<TextView>(R.id.priceTextView) }
        private val timeTextView by lazy { findViewById<TextView>(R.id.timeTextView) }
        /**
         * refreshContent
         */
        override fun refreshContent(e: Entry, highlight: Highlight?) {

            val price = "\$${e.y}"

            val rowData = dataArray[e.x.toInt()] as JSONObject

           val date = rowData.getJSONObject("date")

            val time = "${date.getInt("hour")}:${date.getInt("minute")}"

            priceTextView.text = price

            timeTextView.text = time

             super.refreshContent(e, highlight)
        }//end fun


        override fun getOffset(): MPPointF {
            return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
        }

    }

}//end class