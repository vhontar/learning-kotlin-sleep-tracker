package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ItemSleepTrackerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

private const val ITEM_VIEW_SLEEP_NIGHT = 0
private const val ITEM_VIEW_HEADER = 1

class SleepTrackerAdapter(val sleepNightClickListener: SleepNightClickListener): ListAdapter<DataItem, RecyclerView.ViewHolder>(SleepNightDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addHeaderAndSubmitList(list: List<SleepNight>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map {
                    DataItem.SleepNightItem(it)
                }
            }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ITEM_VIEW_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_SLEEP_NIGHT -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind((getItem(position) as DataItem.SleepNightItem).night, sleepNightClickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is DataItem.SleepNightItem -> ITEM_VIEW_SLEEP_NIGHT
            is DataItem.Header -> ITEM_VIEW_HEADER
            else -> throw ClassCastException("Unknown item")
        }
    }

    class TextViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header, parent, false)
                return TextViewHolder(view)
            }
        }
    }

    class ViewHolder private constructor(private val binding: ItemSleepTrackerBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SleepNight, sleepNightClickListener: SleepNightClickListener) {
            binding.item = item
            binding.clickListener = sleepNightClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = ItemSleepTrackerBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
                return ViewHolder(view)
            }
        }
    }
}

class SleepNightDiffCallback(): DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem) = oldItem == newItem
}

class SleepNightClickListener(val callback: (sleepId: Long) -> Unit) {
    fun onClick(sleepNight: SleepNight) = callback(sleepNight.nightId)
}

sealed class DataItem() {
    class SleepNightItem(val night: SleepNight): DataItem() {
        override val id: Long
            get() =  night.nightId
    }
    object Header: DataItem() {
        override val id: Long
            get() = Long.MIN_VALUE
    }

    abstract val id: Long
}