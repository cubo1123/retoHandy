package com.cubo1123.handy.retohandy.tracker

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.cubo1123.handy.retohandy.database.SavedGeofence
import com.cubo1123.handy.retohandy.database.SavedGeofenceDao
import com.cubo1123.handy.retohandy.receivers.DailyAlarm
import kotlinx.coroutines.*

class TrackerViewModel(
    val database: SavedGeofenceDao,
    application: Application
) : AndroidViewModel(application) {

    var points = database.getAllGeoFences()
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        uiScope.launch {
            getAll()
        }

    }

    private suspend fun getAll() {
        withContext(Dispatchers.IO) {
            val data = database.getAllGeoFences()
            points = data
        }
    }

    private suspend fun insert(point: SavedGeofence) {
        withContext(Dispatchers.IO) {
            database.insert(point)
        }
    }

    private suspend fun updateStatus(point: Long) {
        withContext(Dispatchers.IO) {
            val checkPrev = database.getCurrentVisit()
            if (checkPrev?.onVisit == true){
                checkPrev.onVisit = checkPrev.onVisit.not()
                checkPrev.lasVisitEnd = System.currentTimeMillis()
                database.update(checkPrev)
            }
            val currentGeofence = database.get(point)
            if(checkPrev != currentGeofence || checkPrev?.onVisit == true){
                currentGeofence?.onVisit = currentGeofence!!.onVisit.not()
                currentGeofence.lasVisitStart = System.currentTimeMillis()
                database.update(currentGeofence)
            }
        }
    }

    fun updateGeofence(id: Long) {
        uiScope.launch {
            updateStatus(id)
        }
    }

    fun initDB(){
        uiScope.launch {
            val alarm = DailyAlarm()
            var point = SavedGeofence(latitude =20.622439,longitude =  -103.261452,name =  "Cliente 1",lasVisitStart = System.currentTimeMillis(),lasVisitEnd = System.currentTimeMillis())
            insert(point)
            point = SavedGeofence(latitude = 20.630612,longitude =  -103.266323,name =  "Cliente 2",lasVisitStart = System.currentTimeMillis(),lasVisitEnd = System.currentTimeMillis())
            insert(point)
            point = SavedGeofence(latitude = 20.659363,longitude =  -103.323656,name =  "Cliente 3",lasVisitStart = System.currentTimeMillis(),lasVisitEnd = System.currentTimeMillis())
            insert(point)
            point = SavedGeofence(latitude = 20.6745029,longitude =  -103.4282778,name =  "Cliente 4",lasVisitStart = System.currentTimeMillis(),lasVisitEnd = System.currentTimeMillis())
            insert(point)
            point = SavedGeofence(latitude = 20.672654,longitude =  -103.4217406,name =  "Cliente 5",lasVisitStart = System.currentTimeMillis(),lasVisitEnd = System.currentTimeMillis())
            insert(point)
            alarm.setAlarm(getApplication())
            alarm.setRemaining(getApplication())
        }
    }

    fun setRemaining() {
        val alarm = DailyAlarm()
        alarm.setRemaining(getApplication())
        alarm.cancelAlarm(getApplication())
        alarm.setAlarm(getApplication())
    }
}