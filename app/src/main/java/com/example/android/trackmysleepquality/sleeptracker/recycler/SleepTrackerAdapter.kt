package com.example.android.trackmysleepquality.sleeptracker.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.convertDurationToFormatted
import com.example.android.trackmysleepquality.convertNumericQualityToString
import com.example.android.trackmysleepquality.database.SleepNight

class SleepTrackerAdapter: RecyclerView.Adapter<SleepTrackerAdapter.ViewHolder>() {
    var data = listOf<SleepNight>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    class ViewHolder private constructor(view: View): RecyclerView.ViewHolder(view) {
        private val ivSleepQuality: ImageView = view.findViewById(R.id.ivSleepQuality)
        private val tvSleepDateTime: TextView = view.findViewById(R.id.tvSleepDateTime)
        private val tvSleepQuality: TextView = view.findViewById(R.id.tvSleepQuality)

        fun bind(item: SleepNight) {
            val res = itemView.context.resources
            ivSleepQuality.setImageResource(when (item.sleepQuality) {
                0 -> R.drawable.ic_sleep_0
                1 -> R.drawable.ic_sleep_1
                2 -> R.drawable.ic_sleep_2
                3 -> R.drawable.ic_sleep_3
                4 -> R.drawable.ic_sleep_4
                5 -> R.drawable.ic_sleep_5
                else -> R.drawable.ic_launcher_sleep_tracker_foreground
            })

            tvSleepQuality.text = convertNumericQualityToString(item.sleepQuality, res)
            tvSleepDateTime.text = convertDurationToFormatted(item.startTimeMillis, item.endTimeMillis, res)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = inflater.inflate(R.layout.item_sleep_tracker, parent, false)
                return ViewHolder(view)
            }
        }
    }
}