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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.example.android.trackmysleepquality.sleeptracker.recycler.SleepTrackerAdapter
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    private lateinit var viewDataBinding: FragmentSleepTrackerBinding
    private lateinit var viewModel: SleepTrackerViewModel

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        viewDataBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)
        viewDataBinding.lifecycleOwner = this

        val application = requireNotNull(activity).application
        val sleepDatabaseDao = SleepDatabase.getInstance(application).sleepDatabaseDao
        viewModel = ViewModelProvider(this, SleepTrackerViewModelFactory(sleepDatabaseDao, application)).get(SleepTrackerViewModel::class.java)

        val adapter = SleepTrackerAdapter()

        viewDataBinding.apply {
            viewmodel = viewModel
            recyclerView.adapter = adapter
        }

        viewModel.nights.observe(viewLifecycleOwner, Observer {
            adapter.data = it
        })

        viewModel.navigateToSleepQuality.observe(this.viewLifecycleOwner, Observer { sleepNight ->
            sleepNight?.let {
                findNavController().navigate(SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(it.nightId))
                viewModel.doneNavigation()
            }
        })

        viewModel.showClearedSnackBar.observe(this.viewLifecycleOwner, Observer {
            if (it) {
                Snackbar.make(activity!!.findViewById(android.R.id.content), R.string.cleared_message, Snackbar.LENGTH_LONG).show()
                viewModel.doneShowingSnackBar()
            }
        })

        return viewDataBinding.root
    }
}
