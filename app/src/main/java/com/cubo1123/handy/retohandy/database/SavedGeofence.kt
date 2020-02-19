package com.cubo1123.handy.retohandy.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_geo_fences")
data class SavedGeofence  (
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,
        @ColumnInfo(name = "name")
        val name: String= "",
        @ColumnInfo(name = "latitude")
        val latitude: Double = 0.0,
        @ColumnInfo(name = "longitude")
        var longitude: Double = 0.0,
        @ColumnInfo(name = "last_notification")
        var lastNotification: Long = 0L,
        @ColumnInfo(name = "last_visit_end")
        var lasVisitEnd: Long = 0L,
        @ColumnInfo(name = "last_visit_start")
        var lasVisitStart: Long = 0L,
        @ColumnInfo(name = "on_visit")
        var onVisit: Boolean = false
)