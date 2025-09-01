package com.ucb.pawapp.citizen.viewmodel

import androidx.lifecycle.*
import com.ucb.pawapp.citizen.model.CitizenPreferences
import com.ucb.pawapp.citizen.repository.CitizenPreferenceRepository
import kotlinx.coroutines.launch

class CitizenPreferenceViewModel : ViewModel() {
    private val repo = CitizenPreferenceRepository()

    private val _preferences = MutableLiveData<CitizenPreferences>()
    val preferences: LiveData<CitizenPreferences> = _preferences

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun loadPreferences() = viewModelScope.launch {
        runCatching { repo.loadPreferences() }
            .onSuccess { _preferences.value = it }
    }

    fun savePreferences(prefs: CitizenPreferences) = viewModelScope.launch {
        runCatching { repo.saveExplicitPreferences(prefs.preferredSpecies, prefs.receiveNotifications) }
            .onSuccess { _saveSuccess.value = true }
            .onFailure { _saveSuccess.value = false }
    }
}
