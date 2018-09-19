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

package com.transcodium.tnsmoney.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.transcodium.tnsmoney.db.entities.AssetStats

@Dao
interface AssetStatsDao {

    @get:Query("Select * FROM asset_stats")
    val all: List<AssetStatsDao>

    /**
     * fetch  by type
     */
    @Query("Select * From asset_stats WHERE type = :type")
    fun findByType(type: String): List<AssetStatsDao>

    /**
     * insert
     */
    @Insert(onConflict = REPLACE)
    fun addOne(data: AssetStats)


    @Insert(onConflict = REPLACE)
    fun addAll(data: List<AssetStats>)
}