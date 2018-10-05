package com.transcodium.tnsmoney.db.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.transcodium.tnsmoney.db.entities.AssetAddresses
import com.transcodium.tnsmoney.db.entities.AssetStats

@Dao
abstract class AssetAddressesDao {

    //select one
    @Query("Select * From asset_addresses WHERE asset = :asset ORDER BY id DESC LIMIT 1")
    abstract fun findLatest(asset: String): AssetAddresses

    //select all
    @Query("Select * From asset_addresses WHERE asset = :asset ORDER BY id DESC")
    abstract fun findAll(asset: String): List<AssetAddresses>

    //findOneLive
    @Query("Select * From asset_addresses WHERE asset = :asset ORDER BY id DESC LIMIT 1")
    abstract fun findLatestLive(asset: String): LiveData<AssetStats>

    //insert
    @Insert
    abstract fun insert(data: AssetAddresses)

}