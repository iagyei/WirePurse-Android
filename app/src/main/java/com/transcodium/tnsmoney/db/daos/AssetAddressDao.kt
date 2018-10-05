package com.transcodium.tnsmoney.db.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.transcodium.tnsmoney.db.entities.AssetAddress
import com.transcodium.tnsmoney.db.entities.AssetStats
import androidx.room.OnConflictStrategy.REPLACE


@Dao
abstract class AssetAddressDao {

    //select one
    @Query("Select * From asset_addresses WHERE chain = :chain ORDER BY id DESC LIMIT 1")
    abstract fun findOne(chain: String): AssetAddress

    //findOneLive
    @Query("Select * From asset_addresses WHERE chain = :chain ORDER BY id DESC LIMIT 1")
    abstract fun findOneLive(chain: String): LiveData<AssetStats>

    //insert
    @Insert(onConflict = REPLACE)
    abstract fun insert(data: AssetAddress)

}