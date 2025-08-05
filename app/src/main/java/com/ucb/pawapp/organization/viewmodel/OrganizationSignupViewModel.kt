package com.ucb.pawapp.organization.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ucb.pawapp.organization.model.OrganizationSignupData
import com.ucb.pawapp.organization.repository.OrganizationAuthRepository

class OrganizationSignupViewModel : ViewModel() {

    private val repository = OrganizationAuthRepository()

    private val _signupSuccess = MutableLiveData<Boolean>()
    val signupSuccess: LiveData<Boolean> get() = _signupSuccess

    private val _signupError = MutableLiveData<String?>()
    val signupError: LiveData<String?> get() = _signupError

    fun registerOrganization(data: OrganizationSignupData, password: String, confirmPassword: String) {
        if (data.email.isBlank() || password.isBlank() || data.organizationName.isBlank()) {
            _signupError.value = "Please fill in all required fields"
            return
        }

        if (password != confirmPassword) {
            _signupError.value = "Passwords do not match"
            return
        }

        repository.signupOrganization(
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
