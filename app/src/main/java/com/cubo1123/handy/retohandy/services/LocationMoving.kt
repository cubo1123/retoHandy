package com.cubo1123.handy.retohandy.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.cubo1123.handy.retohandy.MainActivity
import com.cubo1123.handy.retohandy.R
import com.cubo1123.handy.retohandy.Utils.Companion.CHANNEL_ID_LOCATION
import com.cubo1123.handy.retohandy.Utils.Companion.JOB_LOCATION
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LocationMoving : JobIntentService() {

    private val TAG = LocationMoving::class.java.simpleName
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private lateinit var mCurrentLocation: Location
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mSettingsClient: SettingsClient
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 18000
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private fun startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener {
                Log.i(TAG, "All location settings are satisfied.")
                mFusedLocationClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback, Looper.myLooper()
                )
            }
            .addOnFailureListener{

            }
    }

    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
    }
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.fastestInterval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onHandleWork(intent: Intent) {
        Log.i(TAG,"Start the service")
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)
        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()
        uiScope.launch {
            object : CountDownTimer(180000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.i(TAG,"tick")
                }

                override fun onFinish() {
                    Log.i(TAG,"finished semaforo")
                    stopSelf()
                }
            }.start()
        }
        startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG,"Destroying")
    }

    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                Log.i(TAG, locationResult?.lastLocation.toString())
                if (::mCurrentLocation.isInitialized){
                    val distance = mCurrentLocation.distanceTo(locationResult!!.lastLocation)
                    Log.i(TAG,"distance $distance")
                    if (distance>10){
                        Log.i(TAG,"In movement, show notification")
                        sendNotification(getString(R.string.geofence_transition_exited))
                    }else{
                        Toast.makeText(applicationContext,"no hay movimiento",Toast.LENGTH_SHORT).show()
                    }
                }
                mCurrentLocation = locationResult?.lastLocation ?: mCurrentLocation
            }
        }
    }

    private fun sendNotification(notificationDetails: String) {
        Log.e(TAG,notificationDetails)
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val mChannel =
                NotificationChannel(CHANNEL_ID_LOCATION, name, NotificationManager.IMPORTANCE_DEFAULT)

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
            builder.setChannelId(CHANNEL_ID_LOCATION)
        }
        builder.setAutoCancel(true)
        mNotificationManager.notify(0, builder.build())
    }
    companion object {

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                LocationMoving::class.java, JOB_LOCATION, intent
            )
        }
    }
}

