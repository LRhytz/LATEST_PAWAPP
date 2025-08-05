package com.ucb.pawapp.citizen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ucb.pawapp.citizen.model.Incident

class CitizenHomeViewModel : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _incidents = MutableLiveData<List<Incident>>()
    val incidents: LiveData<List<Incident>> = _incidents

    init {
        fetchUserData()
        fetchIncidents()
    }

    private fun fetchUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                _userName.value = doc.getString("fullName") ?: "Citizen"
            }
            .addOnFailureListener {
                _userName.value = "Unknown"
            }
    }

    private fun fetchIncidents() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("incidents")
            .whereEqualTo("reportedBy", uid)
            .get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { it.toObject(Incident::class.java) }
                _incidents.value = list
            }
            .addOnFailureListener {
                _incidents.value = emptyList() // fallback if fetch fails
            }
    }
}
