// File: app/src/main/java/com/ucb/pawapp/organization/viewmodel/OrganizationHomeViewModel.kt
package com.ucb.pawapp.organization.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ucb.pawapp.organization.model.OrganizationSignupData

class OrganizationHomeViewModel : ViewModel() {

    private val _userName = MutableLiveData<String>()
    /** Expose as immutable LiveData */
    val userName: LiveData<String> = _userName

    // Add the items property that your Fragment is trying to observe
    private val _items = MutableLiveData<List<OrganizationSignupData>>()
    /** Expose as immutable LiveData */
    val items: LiveData<List<OrganizationSignupData>> = _items

    init {
        fetchUserData()
        loadItems() // Initialize with empty list or load actual data
    }

    /**
     * Loads the current org's name from Realtime Database
     * under `/users/{uid}/organizationName`.
     */
    private fun fetchUserData() {
        val uid = FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid
            ?: return  // if not signed in, bail

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .child("organizationName")
            .get()
            .addOnSuccessListener { snap ->
                // snap.value should be your org name
                _userName.value = snap.getValue(String::class.java)
                    ?: "Organization"
            }
            .addOnFailureListener {
                _userName.value = "Unknown Org"
            }
    }

    /**
     * Load items data - replace this with your actual data loading logic
     */
    private fun loadItems() {
        // Initialize with empty list for now
        // Replace this with your actual data loading logic
        _items.value = emptyList()

        // Example: Load items from Firebase or other data source
        // val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // FirebaseDatabase.getInstance()
        //     .getReference("organizations")
        //     .child(uid)
        //     .child("items")
        //     .get()
        //     .addOnSuccessListener { snap ->
        //         val itemsList = snap.children.mapNotNull {
        //             it.getValue(OrganizationSignupData::class.java)
        //         }
        //         _items.value = itemsList
        //     }
        //     .addOnFailureListener {
        //         _items.value = emptyList()
        //     }
    }
}