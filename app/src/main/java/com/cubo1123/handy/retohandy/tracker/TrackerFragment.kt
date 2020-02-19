package com.cubo1123.handy.retohandy.tracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.cubo1123.handy.retohandy.R
import com.cubo1123.handy.retohandy.adapters.TrackersAdapter
import com.cubo1123.handy.retohandy.database.DatabaseGeofences
import com.cubo1123.handy.retohandy.databinding.FragmentTrackerBinding
import com.cubo1123.handy.retohandy.receivers.DailyAlarm
import com.cubo1123.handy.retohandy.services.LocationMoving

class TrackerFragment : Fragment(){

    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent
    private lateinit var trackerViewModel : TrackerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val binding: FragmentTrackerBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_tracker, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = DatabaseGeofences.getInstance(application).trackerDatabaseDao
        val viewModelFactory = TrackerViewModelFactory(dataSource,application)
        trackerViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(TrackerViewModel::class.java)
        binding.trackerViewModel = trackerViewModel

        val adapter = TrackersAdapter(application,trackerViewModel)
        trackerViewModel.points.observe(viewLifecycleOwner, Observer {
            adapter.data = it
        })

        val sharedPref = activity?.getSharedPreferences("shared",Context.MODE_PRIVATE)
        if (sharedPref!!.getBoolean(getString(R.string.initialized),false).not()){
            trackerViewModel.initDB()
            sharedPref.edit().putBoolean(getString(R.string.initialized),true).apply()
        }else if (sharedPref.getBoolean(getString(R.string.geo_status),false).not()){
            if (sharedPref.getBoolean(getString(R.string.initialized),false)){
                trackerViewModel.setRemaining()
            }
        }
        binding.list.adapter = adapter


        return binding.root
    }

}