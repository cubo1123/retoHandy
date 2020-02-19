package com.cubo1123.handy.retohandy.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cubo1123.handy.retohandy.services.GeofenceTransitionsJobIntentService

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("1123","geo received")
        GeofenceTransitionsJobIntentService.enqueueWork(context!!, intent!!)
    }
}