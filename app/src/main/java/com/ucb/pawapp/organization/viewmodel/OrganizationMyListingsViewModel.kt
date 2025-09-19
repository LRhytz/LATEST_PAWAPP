// app/src/main/java/com/ucb/pawapp/organization/viewmodel/OrganizationMyListingsViewModel.kt
package com.ucb.pawapp.organization.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ucb.pawapp.organization.repository.OrganizationAdoptionRepository
import com.ucb.pawapp.shared.model.AdoptionListing
import kotlinx.coroutines.launch

class OrganizationMyListingsViewModel : ViewModel() {

    private val repo = OrganizationAdoptionRepository()

    // Realtime listener bits
    private val db = Firebase.database.reference
    private var query: Query? = null
    private var listener: ValueEventListener? = null

    // Exposed state
    private val _items = MutableLiveData<List<AdoptionListing>>(emptyList())
    val items: LiveData<List<AdoptionListing>> = _items

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean> = _busy

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Start realtime listening for the current org’s listings.
     * Safe to call multiple times; it’ll no-op if already started.
     */
    fun startListening() {
        if (listener != null) return  // already active

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            _error.value = "Not signed in as an organization."
            return
        }

        _busy.value = true

        query = db.child("adoptions")
            .orderByChild("orgId")
            .equalTo(uid)

        listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val list = buildList {
                    for (c in snap.children) {
                        c.getValue(AdoptionListing::class.java)?.let { add(it.copy(id = c.key ?: it.id)) }
                    }
                }.sortedByDescending { it.createdAt }

                _items.postValue(list)
                _busy.postValue(false)
                _error.postValue(null)
            }

            override fun onCancelled(error: DatabaseError) {
                _busy.postValue(false)
                _error.postValue(error.message)
            }
        }

        query!!.addValueEventListener(listener!!)
    }

    /**
     * Stop realtime listening. Call from Fragment.onStop().
     */
    fun stopListening() {
        listener?.let { l -> query?.removeEventListener(l) }
        listener = null
        query = null
    }

    /**
     * Manual one-shot refresh using the repository (uses get()/await()).
     * You can keep calling this from onResume() if you don’t want realtime,
     * but if you use startListening()/stopListening(), you can remove your
     * onResume() refresh() call.
     */
    fun refresh() = viewModelScope.launch {
        _busy.value = true
        runCatching { repo.loadMyListings() }
            .onSuccess { _items.value = it; _error.value = null }
            .onFailure { _items.value = emptyList(); _error.value = it.message }
        _busy.value = false
    }

    fun delete(id: String, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        runCatching { repo.deleteListing(id) }
            .onSuccess {
                onDone(true)
                // If realtime is active, the list will update automatically.
                // If not, fall back to a manual refresh.
                if (listener == null) refresh()
            }
            .onFailure {
                onDone(false)
                _error.value = it.message
            }
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}
