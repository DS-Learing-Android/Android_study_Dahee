package com.example.prac_android.step6.sleeptracker

import android.app.Application
import android.provider.SyncStateContract.Helpers.update
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.prac_android.step6.database.SleepDatabaseDao
import com.example.prac_android.step6.database.SleepNight
import com.example.prac_android.step6.formatNights
import kotlinx.coroutines.launch

class SleepTrackerViewModel (
    private val database: SleepDatabaseDao,
    application: Application) : AndroidViewModel(application) {
    private var tonight = MutableLiveData<SleepNight?>()
    private val nights = database.getAllNights()

    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        viewModelScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }
    private suspend fun getTonightFromDatabase(): SleepNight? {
        var night = database.getTonight()
        if(night?.endTimeMilli != night?.startTimeMilli) {
            night = null
        }
        return night
    }
    fun onStartTracking() {
        viewModelScope.launch {
            val newNight = SleepNight()
            insert(newNight)
            tonight.value = getTonightFromDatabase()
        }
    }
    private suspend fun insert(night: SleepNight) {
        database.insert(night)
    }
    fun onStopTracking() {
        viewModelScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)
        }
    }
    private suspend fun update(night: SleepNight) {
        database.update(night)
    }
    fun onClear() {
        viewModelScope.launch {
            clear()
            tonight.value = null
        }
    }
    private suspend fun clear() {
        database.clear()
    }
}