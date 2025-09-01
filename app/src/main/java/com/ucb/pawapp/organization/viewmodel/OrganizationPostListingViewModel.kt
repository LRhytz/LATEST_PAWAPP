// File: app/src/main/java/com/ucb/pawapp/organization/viewmodel/OrganizationPostListingViewModel.kt
package com.ucb.pawapp.organization.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucb.pawapp.organization.repository.OrganizationAdoptionRepository
import com.ucb.pawapp.shared.model.AdoptionListing
import kotlinx.coroutines.launch

class OrganizationPostListingViewModel : ViewModel() {
    private val repo = OrganizationAdoptionRepository()

    fun postListing(
        listing: AdoptionListing,
        onResult: (Boolean, String?) -> Unit
    ) = viewModelScope.launch {
        runCatching { repo.postListing(listing) }
            .onSuccess { id -> onResult(true, id) }
            .onFailure { e -> onResult(false, e.message) }
    }
}
