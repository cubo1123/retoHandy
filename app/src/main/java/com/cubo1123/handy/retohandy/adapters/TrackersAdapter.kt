package com.cubo1123.handy.retohandy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cubo1123.handy.retohandy.R
import com.cubo1123.handy.retohandy.database.SavedGeofence
import com.cubo1123.handy.retohandy.tracker.TrackerViewModel
import kotlinx.android.synthetic.main.item_geofence.view.*
import java.text.DateFormat
import java.util.*

class TrackersAdapter(val context: Context, val viewModel: TrackerViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.name.text = item.name
        holder.itemView.lastVisit.text = DateFormat.getDateTimeInstance().format(Date(item.lasVisitEnd)).toString()
        if (item.onVisit){
            holder.itemView.timeLastVisit.text = context.getString(R.string.onVisit)
            holder.itemView.onVisit.setBackgroundColor(context.resources.getColor(R.color.colorPrimaryDark))
        }else{
            holder.itemView.timeLastVisit.text = getElapsedTime(item.lasVisitEnd - item.lasVisitStart)
            holder.itemView.onVisit.setBackgroundColor(context.resources.getColor(R.color.colorAccent))
        }
        holder.itemView.onVisit.setOnClickListener {
            viewModel.updateGeofence(id = item.id)
        }

    }
    var data = listOf<SavedGeofence>()
        set(value) {
            field = value
            notifyDataSetChanged() }


    fun getElapsedTime(time : Long):String{
        val minutes = time / 1000 / 60
        val seconds = time / 1000 % 60
        return "$minutes : ${context.getString(R.string.minutes)} $seconds ${context.getString(R.string.seconds)}"
    }
    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_geofence, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val timeLastVisit: TextView = itemView.findViewById(R.id.timeLastVisit)
        val name: TextView = itemView.findViewById(R.id.name)
        val lastVisit: TextView = itemView.findViewById(R.id.lastVisit)

    }

}