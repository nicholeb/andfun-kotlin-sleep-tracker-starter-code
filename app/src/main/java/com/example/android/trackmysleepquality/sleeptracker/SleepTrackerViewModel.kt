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
import android.provider.SyncStateContract.Helpers.insert
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

        //Job is required. It cancels all coroutines started by the ViewModel when the ViewModel is destroyed.
        private var viewModelJob = Job()


        //onCleared is called when the ViewModel is destroyed. In the override, the Job cancels all co-routines started by the ViewModel.
        override fun onCleared() {
                super.onCleared()
                viewModelJob.cancel()
        }

        //scope for the co-routines. Determines which thread the coroutine runs in and must be aware of the Job.
        //Creates an instance of CoroutineScope - requires a Dispatcher and a Job. Dispatcher.Main runs coroutines on the main thread.
        //Dispatcher.Main is often used with ViewModels because it involves update of the UI when the coroutine is done.
        //However this is out of date. Use a ViewModelScope.
        private var tonight = MutableLiveData<SleepNight?>()
        private val nights = database.getAllNights()
        val nightsString = Transformations.map(nights){ nights ->
                formatNights(nights, application.resources)
        }

        private val _navigateToSleepQuality = MutableLiveData<SleepNight>()

        val navigateToSleepQuality: LiveData<SleepNight>
            get() = _navigateToSleepQuality

        fun doneNavigating(){
            _navigateToSleepQuality.value = null
        }
        init {
            initializeTonight()
        }

        private fun initializeTonight(){
         viewModelScope.launch {
         tonight.value = getTonightFromDatabase()
         }
        }

        private suspend fun getTonightFromDatabase(): SleepNight?{
                        var night = database.getTonight()
                        if (night?.endTimeMilli != night?.startTimeMilli){
                                night = null
                        }
                        return night

        }

        fun onStartTracking(){
                viewModelScope.launch{
                  val newNight = SleepNight()
                        insert(newNight)
                        tonight.value = getTonightFromDatabase()
                }
        }

        private suspend fun insert(night: SleepNight) {
                        database.insert(night)
                }


        fun onStopTracking(){
                viewModelScope.launch{
                        val oldNight = tonight.value ?: return@launch
                        oldNight.endTimeMilli = System.currentTimeMillis()
                        update(oldNight)
                    _navigateToSleepQuality.value = oldNight
                }

        }
        private suspend fun update(night: SleepNight) {
                        database.update(night)
                }


        fun onClear(){
                viewModelScope.launch {
                        clear()
                        tonight.value = null
                }
        }

        suspend fun clear(){
                   database.clear()
                }


        }

