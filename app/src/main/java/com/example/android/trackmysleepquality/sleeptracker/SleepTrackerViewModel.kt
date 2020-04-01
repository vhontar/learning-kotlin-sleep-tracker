/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application
) : AndroidViewModel(application) {
    private val sleepTrackerJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + sleepTrackerJob)

    private val tonight = MutableLiveData<SleepNight?>()
    val nights = database.getAllSleepNights()

    val nightsString = Transformations.map(nights) {
        formatNights(it, application.resources)
    }

    private val _navigateToSleepQuality = MutableLiveData<SleepNight?>()
    val navigateToSleepQuality: LiveData<SleepNight?> = _navigateToSleepQuality

    init {
        initializeTonight()
    }

    val startButtonEnabled = Transformations.map(tonight) {
        it == null
    }

    val stopButtonEnabled = Transformations.map(tonight) {
        it != null
    }

    val clearButtonEnabled = Transformations.map(nights) {
        it.isNotEmpty()
    }

    private val _showClearedSnackBar = MutableLiveData<Boolean>()
    val showClearedSnackBar: LiveData<Boolean> = _showClearedSnackBar

    fun doneShowingSnackBar() {
        _showClearedSnackBar.value = false;
    }

    override fun onCleared() {
        super.onCleared()
        sleepTrackerJob.cancel()
    }

    private fun initializeTonight() {
        uiScope.launch {
            tonight.value = getCurrentNight()
        }
    }

    fun doneNavigation() {
        _navigateToSleepQuality.value = null
    }

    private suspend fun getCurrentNight(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            if (night?.startTimeMillis != night?.endTimeMillis) {
                night = null
            }
            night
        }
    }

    fun onStartTracking() {
        uiScope.launch {
            val sleepNight = SleepNight()
            create(sleepNight)
            tonight.value = getCurrentNight()
        }
    }

    private suspend fun create(sleepNight: SleepNight) {
        withContext(Dispatchers.IO) {
            database.create(sleepNight)
        }
    }

    fun onStopTracking() {
        uiScope.launch {
            val nightSleep = tonight.value ?: return@launch
            nightSleep.endTimeMillis = System.currentTimeMillis()
            update(nightSleep)
            _navigateToSleepQuality.value = nightSleep
        }
    }

    private suspend fun update(sleepNight: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(sleepNight)
        }
    }

    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null
            _showClearedSnackBar.value = true
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }
}

