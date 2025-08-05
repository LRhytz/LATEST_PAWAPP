// File: app/src/main/java/com/ucb/pawapp/citizen/repository/UserRepository.kt
package com.ucb.pawapp.citizen.repository

import com.ucb.pawapp.citizen.ui.dashboard.ReportStats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase
        .getInstance()
        .getReference("users")

    /** Returns the current Firebase UID */
    suspend fun getCurrentUid(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("No signed‑in user")

    /** Fetches the saved photoUrl (string URI) from Realtime DB */
    suspend fun getSavedProfilePhotoUrl(): String? {
        val uid = getCurrentUid()
        val snap = usersRef.child(uid).child("photoUrl").get().await()
        return snap.getValue(String::class.java)
    }

    /** Persists a new photoUrl back into Realtime DB */
    suspend fun updateProfilePhotoUrl(url: String) {
        val uid = getCurrentUid()
        usersRef.child(uid).child("photoUrl").setValue(url).await()
    }

    /** Reads the user’s role from Realtime DB */
    suspend fun getUserRole(): String? {
        val uid = getCurrentUid()
        val snap = usersRef.child(uid).child("role").get().await()
        return snap.getValue(String::class.java)
    }

    /** Stubbed–out extras */
    suspend fun getUnreadNotificationCount(): Int = 3
    suspend fun markNotificationAsRead(notificationId: String) { /* … */ }
    suspend fun getReportStatsForCurrentUser(): ReportStats =
        ReportStats(total = 10, open = 4, closed = 6)
    fun getProfileCompletionPercent(): Int = 70
}
