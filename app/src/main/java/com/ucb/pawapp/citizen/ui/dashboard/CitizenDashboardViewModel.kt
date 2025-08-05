// File: app/src/main/java/com/ucb/pawapp/citizen/ui/dashboard/CitizenDashboardViewModel.kt
package com.ucb.pawapp.citizen.ui.dashboard

import android.net.Uri
import androidx.lifecycle.*
import com.ucb.pawapp.citizen.repository.UserRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** Simple holder for report counts */
data class ReportStats(val total: Int, val open: Int, val closed: Int)

/** UI state for notifications/loading, etc */
data class CitizenDashboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val notificationCount: Int = 0
)

@HiltViewModel
class CitizenDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // ── Dashboard UI state ────────────────────────────────────────────
    private val _uiState = MutableStateFlow(CitizenDashboardUiState())
    val uiState: LiveData<CitizenDashboardUiState> = _uiState.asLiveData()

    // ── Notification badge ────────────────────────────────────────────
    private val _notificationCount = MutableLiveData<Int>()
    val notificationCount: LiveData<Int> = _notificationCount

    // ── Report stats for Profile screen ───────────────────────────────
    private val _reportStats = MutableLiveData<ReportStats>()
    val reportStats: LiveData<ReportStats> = _reportStats

    // ── Profile completion % ──────────────────────────────────────────
    private val _profileCompletion = MutableLiveData<Int>()
    val profileCompletion: LiveData<Int> = _profileCompletion

    // ── Profile photo URL ─────────────────────────────────────────────
    private val _profilePhotoUrl = MutableLiveData<String?>()
    val profilePhotoUrl: LiveData<String?> = _profilePhotoUrl

    init {
        fetchNotifications()
        loadReportStats()
        loadProfileCompletion()
        loadProfilePhotoUrl()
    }

    private fun fetchNotifications() {
        viewModelScope.launch {
            try {
                val count = userRepository.getUnreadNotificationCount()
                _notificationCount.value = count
                _uiState.update { it.copy(notificationCount = count) }
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            userRepository.markNotificationAsRead(notificationId)
            fetchNotifications()
        }
    }

    private fun loadReportStats() {
        viewModelScope.launch {
            val stats = userRepository.getReportStatsForCurrentUser()
            _reportStats.postValue(stats)
        }
    }

    private fun loadProfileCompletion() {
        _profileCompletion.value = userRepository.getProfileCompletionPercent()
    }

    private fun loadProfilePhotoUrl() {
        viewModelScope.launch {
            val url = userRepository.getSavedProfilePhotoUrl()
            _profilePhotoUrl.value = url
        }
    }

    /**
     * Uploads a new profile photo, saves it to Cloud Storage + Realtime DB,
     * and updates LiveData. Uses the classic FirebaseStorage API.
     */
    fun uploadNewProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1️⃣ Get current user ID
                val uid = userRepository.getCurrentUid()

                // 2️⃣ Use classic FirebaseStorage.getInstance()
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.getReference("profile_photos/$uid.jpg")

                // 3️⃣ Upload the file
                storageRef.putFile(uri).await()

                // 4️⃣ Retrieve the download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 5️⃣ Save URL in your Realtime DB via the repo
                userRepository.updateProfilePhotoUrl(downloadUrl)

                // 6️⃣ Publish to UI
                _profilePhotoUrl.value = downloadUrl
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
