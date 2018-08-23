package com.transcodium.app

/**
 * Created by dr_success on 12/13/2017.
 */
data class DrawerListModel(
        val menuIcon: Int,
        val menuTitle: String,
        val targetActivity: Class<*>)