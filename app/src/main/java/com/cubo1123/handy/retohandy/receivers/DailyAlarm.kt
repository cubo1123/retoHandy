package com.cubo1123.handy.retohandy.receivers

import android.annotation.SuppressLint
import android.content.Context.ALARM_SERVICE
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import com.cubo1123.handy.retohandy.database.DatabaseGeofences
import com.cubo1123.handy.retohandy.database.SavedGeofenceDao
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import com.cubo1123.handy.retohandy.R




class DailyAlarm : BroadcastReceiver(), OnCompleteListener<Void> {

    private lateinit var context: Context
    private lateinit var mGeofencingClient: GeofencingClient
    private lateinit var mGeofenceList: ArrayList<Geofence>
    private lateinit var mGeofencePendingIntent: PendingIntent
    private val TAG = DailyAlarm::class.java.simpleName
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onComplete(task: Task<Void>) {
        if (task.isSuccessful) {
            val sharedPref = context.getSharedPreferences("shared",Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean(context.getString(R.string.geo_status),true).apply()
            Log.i(TAG,"setted fine")
        } else {
            val errorMessage = getErrorString(context, task.exception.hashCode())
            Log.e(TAG, "error setting$errorMessage")
        }
    }

    fun getErrorString(context: Context, errorCode: Int): String {
        val mResources = context.resources
        when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> return mResources.getString(R.string.geofence_not_available)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> return mResources.getString(R.string.geofence_too_many_geofences)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> return mResources.getString(R.string.geofence_too_many_pending_intents)
            else -> return mResources.getString(R.string.unknown_geofence_error)
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG,"receive")
        this.context = context!!
        mGeofenceList = arrayListOf()
        val dataSource = context.let { DatabaseGeofences.getInstance(it).trackerDatabaseDao }
        mGeofencingClient = LocationServices.getGeofencingClient(context)
        fillGeofences(dataSource, context)
    }

    private fun fillGeofences(
        dataSource: SavedGeofenceDao?,
        context: Context, remaining: Boolean = false) {
        uiScope.launch {
            getGeofences(dataSource,remaining)
            Log.e(TAG,"added")
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent(context))
                .addOnCompleteListener(this@DailyAlarm)
        }
    }

    private suspend fun getGeofences(dataSource: SavedGeofenceDao?,remaining : Boolean = false) {
        withContext(Dispatchers.IO) {
            var durationTime = 32400000L
            if (remaining){

                val start: Calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                }

                val end: Calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, 18)
                    set(Calendar.MINUTE, 0)
                }
                val diff = end.timeInMillis - start.timeInMillis
                Log.e(TAG,diff.toString())
                durationTime = diff
            }
            Log.i(TAG,"duration"+durationTime.toString())
            dataSource?.getCurrentGeoFences()?.forEach {
                Log.e(TAG,it.toString())
                mGeofenceList.add(
                    Geofence.Builder()
                        .setRequestId(it.id.toString())
                        .setCircularRegion(
                            it.latitude,
                            it.longitude,
                            300f
                        )
                        .setLoiteringDelay(180000)
                        .setExpirationDuration(durationTime)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build()
                )
            }
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        val builder = GeofencingRequest.Builder()
        builder.setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL )
        builder.addGeofences(mGeofenceList)
        return builder.build()
    }


    private fun getGeofencePendingIntent(context: Context): PendingIntent {
        if (::mGeofencePendingIntent.isInitialized) {
            return mGeofencePendingIntent
        }
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        mGeofencePendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return mGeofencePendingIntent
    }

    fun setAlarm(context: Context) {
        this.context = context
        val alarmMgr = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, DailyAlarm::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, 0)
        }
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.DAY_OF_MONTH,(this.get(Calendar.DAY_OF_MONTH)+1))
            set(Calendar.MINUTE, 0)
        }
        alarmMgr.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmIntent
        )
    }

    fun cancelAlarm(context: Context) {
        val intent = Intent(context, DailyAlarm::class.java)
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
    }

    fun setRemaining(context: Context) {
        this.context = context
        mGeofenceList = arrayListOf()
        val dataSource = context.let { DatabaseGeofences.getInstance(it).trackerDatabaseDao }
        mGeofencingClient = LocationServices.getGeofencingClient(context)
        fillGeofences(dataSource, context,true)
    }
}