package com.cubo1123.handy.retohandy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cubo1123.handy.retohandy.services.GeofenceTransitionsJobIntentService
import com.cubo1123.handy.retohandy.services.LocationMoving
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private val REQUEST_LOCATION: Int = 1123
    lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
    }

    fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_LOCATION)
        } else {
            setContentView(R.layout.activity_main)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    setContentView(R.layout.activity_main)
                } else {
                    Toast.makeText(this,getString(R.string.give_permission),Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

}
