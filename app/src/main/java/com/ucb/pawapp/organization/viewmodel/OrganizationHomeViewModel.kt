package com.ucb.pawapp.organization.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OrganizationHomeViewModel : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                _userName.value = doc.getString("orgName") ?: "Organization"
            }
            .addOnFailureListener {
                _userName.value = "Unknown Org"
            }
    }
}
