package com.ucb.pawapp.organization.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ucb.pawapp.organization.model.OrganizationSignupData
import com.ucb.pawapp.organization.repository.OrganizationAuthRepository

class OrganizationSignupViewModel : ViewModel() {
    private val repo = OrganizationAuthRepository()

    private val _signupSuccess = MutableLiveData<Boolean>()
    val signupSuccess: LiveData<Boolean> = _signupSuccess

    private val _signupError = MutableLiveData<String?>()
    val signupError: LiveData<String?> = _signupError

    fun registerOrganization(
        data: OrganizationSignupData,
        password: String,
        confirmPassword: String
    ) {
        // basic validation
        if (data.email.isBlank() ||
            data.organizationName.isBlank() ||
            password.isBlank() ||
            confirmPassword.isBlank()
        ) {
            _signupError.value = "Please fill in all required fields"
            return
        }
        if (password != confirmPassword) {
            _signupError.value = "Passwords do not match"
            return
        }

        repo.signupOrganization(
            data, password,
            onSuccess = { _signupSuccess.value = true },
            onFailure = { _signupError.value = it.message }
        )
    }
}
