package com.cubo1123.handy.retohandy.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.cubo1123.handy.retohandy.MainActivity
import com.cubo1123.handy.retohandy.R
import com.cubo1123.handy.retohandy.Utils.Companion.CHANNEL_ID
import com.cubo1123.handy.retohandy.Utils.Companion.JOB_ID
import com.cubo1123.handy.retohandy.database.DatabaseGeofences
import com.cubo1123.handy.retohandy.database.SavedGeofence
import com.cubo1123.handy.retohandy.database.SavedGeofenceDao
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.*

class GeofenceTransitionsJobIntentService : JobIntentService() {

    private val TAG = GeofenceTransitionsJobIntentService::class.java.simpleName
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            val errorMessage = getErrorString(
                this,
                geofencingEvent.errorCode
            )
            Log.e(TAG, errorMessage)
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        val triggeringGeofences = geofencingEvent.triggeringGeofences

        getGeofenceTransitionDetails(
            geofenceTransition,
            triggeringGeofences
        )
    }

    fun getErrorString(context: Context, errorCode: Int): String {
        val mResources = context.resources
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> mResources.getString(R.string.geofence_not_available)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> mResources.getString(R.string.geofence_too_many_geofences)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> mResources.getString(R.string.geofence_too_many_pending_intents)
            else -> mResources.getString(R.string.unknown_geofence_error)
        }
    }

    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ) {

        val geofenceTransitionString = getTransitionString(geofenceTransition)

        val dataSource = baseContext.let { DatabaseGeofences.getInstance(it).trackerDatabaseDao }
        Log.i(TAG,geofenceTransitionString)
        Log.i(TAG,triggeringGeofences.toString())
        if(geofenceTransitionString == getString(R.string.geofence_transition_entered)){
            checkGeofence(dataSource,triggeringGeofences,geofenceTransitionString)
        } else if(geofenceTransitionString == getString(R.string.geofence_transition_exited)){
            checkCurrentClient(dataSource,triggeringGeofences,geofenceTransitionString)
        }

    }


    private fun checkCurrentClient(
        dataSource: SavedGeofenceDao?,
        triggeringGeofences: List<Geofence>,
        geofenceTransitionString: String
    ) {
        uiScope.launch {
            val currentClient = checkCurrentVisit(dataSource)
            triggeringGeofences.forEach {
                if (currentClient?.id == it.requestId.toLong()){
                    Log.i(TAG,"Notification sended")
                    sendNotification(geofenceTransitionString + currentClient.name)
                    updateNotification(dataSource,it.requestId.toLong())
                }
            }

        }
    }

    private suspend fun checkCurrentVisit(dataSource: SavedGeofenceDao?): SavedGeofence? {
        return withContext(Dispatchers.IO) {
            dataSource?.getCurrentVisit()
        }
    }

    private fun checkGeofence(
        dataSource: SavedGeofenceDao?,
        triggeringGeofences: List<Geofence>,
        geofenceTransitionString: String
    ) {
        uiScope.launch {
            var clients = ""
            var id = 0L
            triggeringGeofences.forEach {
                clients += getGeofence(dataSource,it.requestId.toLong())?.name
                clients +=","
                id = it.requestId.toLong()
            }
            if (checkLastNotification(dataSource)){
                Log.i(TAG,"Notification sended")
                sendNotification(geofenceTransitionString + clients)
                updateNotification(dataSource,id)
            }
        }
    }

    private suspend fun checkLastNotification(dataSource: SavedGeofenceDao?): Boolean {
        return withContext(Dispatchers.IO) {
            val point = dataSource?.getLastNotification()
            val diff = System.currentTimeMillis() - point!!.lastNotification
            Log.i(TAG,diff.toString())
            diff > 1800000
        }
    }

    private suspend fun updateNotification(dataSource: SavedGeofenceDao?, id : Long): SavedGeofence? {
        return withContext(Dispatchers.IO) {
            val point = dataSource?.get(id)
            point?.lastNotification = System.currentTimeMillis()
            dataSource?.update(point!!)
            point
        }
    }

    private suspend fun getGeofence(dataSource: SavedGeofenceDao?, id : Long): SavedGeofence? {
        return withContext(Dispatchers.IO) {
            dataSource?.get(id)
        }
    }
    private fun sendNotification(notificationDetails: String) {
        Log.e(TAG,notificationDetails)
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val mChannel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)

            mNotificationManager.createNotificationChannel(mChannel)
        }

        val notificationIntent = Intent(applicationContext, MainActivity::class.java)

        val stackBuilder = TaskStackBuilder.create(this)

        stackBuilder.addParentStack(MainActivity::class.java)

        stackBuilder.addNextIntent(notificationIntent)

        val notificationPendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this)

        builder.setSmallIcon(R.drawable.ic_launcher_background)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.ic_launcher_background
                )
            )
            .setColor(Color.RED)
            .setContentTitle(notificationDetails)
            .setContentText(getString(R.string.geofence_transition_notification_text))
            .setContentIntent(notificationPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID) // Channel ID
        }

        builder.setAutoCancel(true)
        mNotificationManager.notify(0, builder.build())
    }

    private fun getTransitionString(transitionType: Int): String {
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_DWELL -> return getString(R.string.geofence_transition_entered)
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                return getString(R.string.geofence_transition_exited)
            }
            else -> return getString(R.string.unknown_geofence_transition)
        }
    }

    companion object {

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID, intent
            )
        }
    }
}
