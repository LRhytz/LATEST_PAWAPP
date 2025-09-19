// File: app/src/main/java/com/ucb/pawapp/citizen/viewmodel/CitizenHomeViewModel.kt
package com.ucb.pawapp.citizen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.ucb.pawapp.citizen.model.Incident

class CitizenHomeViewModel : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _incidents = MutableLiveData<List<Incident>>()
    val incidents: LiveData<List<Incident>> = _incidents

    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var incidentsListener: ValueEventListener? = null

    init {
        fetchUserData()
        listenForIncidents()
    }

    private fun fetchUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.getReference("users").child(uid).child("fullName")
            .get()
            .addOnSuccessListener { snap ->
                _userName.value = snap.getValue(String::class.java) ?: "Citizen"
            }
            .addOnFailureListener {
                _userName.value = "Citizen"
            }
    }

    private fun listenForIncidents() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = db.getReference("incidents").child(uid)

        // remove old listener if any
        incidentsListener?.let { ref.removeEventListener(it) }

        incidentsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Incident>()
                snapshot.children.forEach { child ->
                    child.getValue(Incident::class.java)?.let { list += it }
                }
                // newest first
                _incidents.value = list.sortedByDescending { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {
                _incidents.value = emptyList()
            }
        }
        ref.addValueEventListener(incidentsListener as ValueEventListener)
    }

    override fun onCleared() {
        super.onCleared()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        incidentsListener?.let { db.getReference("incidents").child(uid).removeEventListener(it) }
    }
}
