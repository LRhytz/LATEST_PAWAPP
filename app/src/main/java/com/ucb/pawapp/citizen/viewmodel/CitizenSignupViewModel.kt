package com.ucb.pawapp.citizen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ucb.pawapp.citizen.model.CitizenSignupData
import com.ucb.pawapp.citizen.repository.CitizenAuthRepository

class CitizenSignupViewModel : ViewModel() {

    private val repository = CitizenAuthRepository()

    private val _signupSuccess = MutableLiveData<Boolean>()
    val signupSuccess: LiveData<Boolean> get() = _signupSuccess

    private val _signupError = MutableLiveData<String?>()
    val signupError: LiveData<String?> get() = _signupError

    fun registerCitizen(data: CitizenSignupData, password: String, confirmPassword: String) {
        if (data.email.isBlank() || password.isBlank() || data.fullName.isBlank()) {
            _signupError.value = "Please fill in all required fields"
            return
        }

        if (password != confirmPassword) {
            _signupError.value = "Passwords do not match"
            return
        }

        repository.signupCitizen(
            data,
            password,
            onSuccess = {
                _signupSuccess.value = true
            },
            onFailure = {
                _signupError.value = it.message
            }
        )
    }
}
