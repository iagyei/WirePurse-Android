package com.transcodium.app


data class DrawerListModel(
        val tagName: String,
        val menuIcon: Int,
        val menuTitle: String,
        val targetActivity: Class<*>)