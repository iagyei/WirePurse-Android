package com.transcodium.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.transcodium.tnsmoney.R


/**
 * Created by dr_success on 12/13/2017.
 */
class DrawerListAdapter(
        val ctx: Context,
        val data: MutableList<DrawerListModel>,
        var listRowLayout: Int = R.layout.drawer_list_item_row)
    : ArrayAdapter<DrawerListModel>(ctx,listRowLayout,data) {


    //viewHolder Inner Class
     data class viewHolder(
                    var menuIcon: ImageView,
                    var menuTitle: TextView
    )

    //last position
    private var lastPosition = -1

    //getView
    override fun getView(position: Int,
                         convertView: View?,
                         parent: ViewGroup): View{

          //row data
          var rowData = getItem(position)

          var vh: viewHolder

          var rowView = convertView

          //sometimes convertView is empty
          if(rowView == null) {

              //if empty we should inflate view
              rowView = LayoutInflater.from(ctx)
                            .inflate(listRowLayout,parent,false)
          }//end if

         //lets set the data
         rowView!!.findViewById<ImageView>(R.id.menuIcon)
                 .setImageDrawable(ContextCompat.getDrawable(ctx,rowData.menuIcon))

        //lets set title
        rowView.findViewById<TextView>(R.id.menuTitle)
                .text = rowData.menuTitle

        return rowView
      }//end get view


}//end