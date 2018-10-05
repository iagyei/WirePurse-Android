package com.transcodium.tnsmoney.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
        tableName = "asset_addresses"
)
data class AssetAddresses(
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id: Long? = 0L,
        @ColumnInfo(name = "asset") var asset: String,
        @ColumnInfo(name = "address") var address: String,
        @ColumnInfo(name = "remote_id") var remote_id: String
)
