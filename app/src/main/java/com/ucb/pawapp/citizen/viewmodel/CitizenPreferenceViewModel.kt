package com.ucb.pawapp.citizen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ucb.pawapp.citizen.model.CitizenPreferences
import com.ucb.pawapp.citizen.repository.CitizenPreferenceRepository

class CitizenPreferenceViewModel : ViewModel() {

    private val repository = CitizenPreferenceRepository()

    private val _preferences = MutableLiveData<CitizenPreferences>()
    val preferences: LiveData<CitizenPreferences> get() = _preferences

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> get() = _saveSuccess

    fun savePreferences(prefs: CitizenPreferences) {
        repository.savePreferences(
            prefs,
            onSuccess = { _saveSuccess.value = true },
            onFailure = { _saveSuccess.value = false }
        )
    }

    fun loadPreferences() {
        repository.loadPreferences(
            onSuccess = { prefs -> prefs?.let { _preferences.value = it } },
            onFailure = { /* log or handle */ }
        )
    }
}
