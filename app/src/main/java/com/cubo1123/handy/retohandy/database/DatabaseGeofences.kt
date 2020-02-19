package com.cubo1123.handy.retohandy.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SavedGeofence::class], version = 1, exportSchema = false)
abstract class DatabaseGeofences : RoomDatabase() {

    abstract val trackerDatabaseDao: SavedGeofenceDao

    companion object {

        @Volatile
        private var INSTANCE: DatabaseGeofences? = null

        fun getInstance(context: Context): DatabaseGeofences {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            DatabaseGeofences::class.java,
                            "saved_geofences_database"
                    )
                            .fallbackToDestructiveMigration()
                            .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}