package com.ucb.pawapp.organization.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucb.pawapp.organization.repository.OrganizationAdoptionRepository
import com.ucb.pawapp.shared.model.AdoptionListing
import kotlinx.coroutines.launch

class OrganizationMyListingsViewModel : ViewModel() {
    private val repo = OrganizationAdoptionRepository()

    private val _items = MutableLiveData<List<AdoptionListing>>(emptyList())
    val items: LiveData<List<AdoptionListing>> = _items

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean> = _busy

    fun refresh() = viewModelScope.launch {
        _busy.value = true
        runCatching { repo.loadMyListings() }
            .onSuccess { _items.value = it }
            .onFailure { _items.value = emptyList() }
        _busy.value = false
    }

    fun delete(id: String, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        runCatching { repo.deleteListing(id) }
            .onSuccess { onDone(true); refresh() }
            .onFailure { onDone(false) }
    }
}
