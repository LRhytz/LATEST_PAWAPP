package com.ucb.pawapp.citizen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucb.pawapp.citizen.repository.CitizenPreferenceRepository
import kotlinx.coroutines.launch

class CitizenOnboardingViewModel : ViewModel() {
    private val repo = CitizenPreferenceRepository()

    private val _done = MutableLiveData<Boolean>()
    val done: LiveData<Boolean> = _done

    suspend fun needsOnboarding(): Boolean = repo.needsOnboarding()

    fun save(species: List<String>, sizes: List<String>, topics: List<String>) =
        viewModelScope.launch {
            runCatching { repo.saveOnboardingAnswers(species, sizes, topics) }
                .onSuccess { _done.value = true }
                .onFailure { _done.value = false }
        }
}
