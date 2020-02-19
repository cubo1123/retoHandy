package com.cubo1123.handy.retohandy.tracker

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cubo1123.handy.retohandy.database.DatabaseGeofences
import com.cubo1123.handy.retohandy.database.SavedGeofenceDao

class TrackerViewModelFactory (
    private val dataSource: SavedGeofenceDao,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackerViewModel::class.java)) {
            return TrackerViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}