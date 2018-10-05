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
#  created_at 20/09/2018
 **/

package com.transcodium.tnsmoney.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.transcodium.tnsmoney.APP_DB_NAME
import com.transcodium.tnsmoney.db.daos.AssetAddressesDao
import com.transcodium.tnsmoney.db.daos.AssetStatsDao
import com.transcodium.tnsmoney.db.daos.UserAssetsDao
import com.transcodium.tnsmoney.db.entities.AssetAddresses
import com.transcodium.tnsmoney.db.entities.AssetStats
import com.transcodium.tnsmoney.db.entities.UserAssets

@Database(
        entities = [AssetStats::class, UserAssets::class, AssetAddresses::class],
        exportSchema = false,
        version = 	3
)

abstract class AppDB : RoomDatabase() {

    abstract fun assetStatsDao(): AssetStatsDao
    abstract fun userAssetsDao(): UserAssetsDao
    abstract fun assetAddressDao(): AssetAddressesDao

    companion object {

        private var instance: AppDB? = null


        /**
         * singleton class
         * @return AppDB
         */
        @Synchronized
        fun getInstance(context: Context): AppDB {

            if(instance == null){
                instance = Room.databaseBuilder(
                                context.applicationContext,
                                AppDB::class.java,
                                APP_DB_NAME
                )
                 .fallbackToDestructiveMigration()
                 .build()
            }


            return instance!!
        }//end fun

    }

}