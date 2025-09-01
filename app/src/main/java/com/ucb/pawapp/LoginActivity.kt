// File: app/src/main/java/com/ucb/pawapp/LoginActivity.kt
package com.ucb.pawapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ucb.pawapp.citizen.ui.dashboard.CitizenDashboardActivity
import com.ucb.pawapp.citizen.ui.CitizenSignupActivity
import com.ucb.pawapp.citizen.ui.CitizenOnboardingActivity   // keep
import com.ucb.pawapp.databinding.ActivityLoginBinding
import com.ucb.pawapp.organization.ui.OrganizationDashboardActivity
import com.ucb.pawapp.organization.ui.OrganizationSignupActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 0️⃣ — ViewBinding setup
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1️⃣ — If already signed in, skip straight to the right dashboard:
        FirebaseAuth.getInstance().currentUser?.let { user ->
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.uid)
                .child("role")
                .get()
                .addOnSuccessListener { snap ->
                    val role = snap.getValue(String::class.java)?.lowercase()
                    // Check onboarding for citizens inside routeToDashboard()
                    routeToDashboard(role)
                }
                .addOnFailureListener { e ->
                    // Could not fetch role — fallback to login form
                    Log.w("LoginActivity", "Failed to fetch role: ${e.message}")
                    showLoginForm()
                }
            return
        }

        // 2️⃣ — No user signed in: show login form
        showLoginForm()
    }

    /** Populates and hooks up your login UI. */
    private fun showLoginForm() {
        // Fade-in animations
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.loginTitle.startAnimation(fadeIn)
        binding.loginCard.startAnimation(fadeIn)

        // Decide portal by extra (you must still pass this when launching LoginActivity)
        val userType = intent.getStringExtra("userType")
        if (userType.isNullOrBlank()) {
            Toast.makeText(this, "No user type specified!", Toast.LENGTH_LONG).show()
            finish(); return
        }
        binding.loginTitle.text = "Login as $userType"
        val iconRes = if (userType == "Citizen") R.drawable.ic_person else R.drawable.ic_organization
        binding.userTypeIcon.setImageResource(iconRes)
        val themeColor = if (userType == "Citizen") R.color.citizen_theme else R.color.organization_theme
        binding.loginButton.setBackgroundColor(ContextCompat.getColor(this, themeColor))

        // Back arrow
        binding.backButton.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Forgot password stub
        binding.forgotPasswordText.setOnClickListener {
            Toast.makeText(this, "Forgot password tapped", Toast.LENGTH_SHORT).show()
        }

        // Register link
        binding.registerText.setOnClickListener {
            val cls = if (userType == "Citizen")
                CitizenSignupActivity::class.java
            else
                OrganizationSignupActivity::class.java
            startActivity(Intent(this, cls))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Login button
        binding.loginButton.setOnClickListener {
            attemptLogin(userType)
        }
    }

    /** Try signing in with FirebaseAuth and then route. */
    private fun attemptLogin(userType: String) {
        val email = binding.usernameEditText.text.toString().trim()
        val pwd   = binding.passwordEditText.text.toString().trim()

        // Basic validation
        binding.usernameLayout.error = null
        binding.passwordLayout.error = null
        if (email.isEmpty() || pwd.isEmpty()) {
            if (email.isEmpty())   binding.usernameLayout.error = "Email required"
            if (pwd.isEmpty())     binding.passwordLayout.error = "Password required"
            return
        }

        // UI state
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled  = false

        // Sign in
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, pwd)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid.isNullOrBlank()) {
                    onLoginError("User ID not found"); return@addOnSuccessListener
                }
                // Fetch role from RTDB
                FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("role")
                    .get()
                    .addOnSuccessListener { snap ->
                        binding.progressBar.visibility = View.GONE
                        binding.loginButton.isEnabled  = true

                        val role = snap.getValue(String::class.java)?.lowercase()
                        if (role == userType.lowercase()) {
                            Toast.makeText(this, "$userType login successful", Toast.LENGTH_SHORT).show()
                            // Route (with onboarding check inside)
                            routeToDashboard(role)
                        } else {
                            FirebaseAuth.getInstance().signOut()
                            onLoginError("Access denied. Use the correct portal.")
                        }
                    }
                    .addOnFailureListener { e ->
                        onLoginError("Failed to fetch role: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onLoginError("Login failed: ${e.message}")
            }
    }

    /** Start the proper next screen based on role (Citizen: may go to Onboarding first). */
    private fun routeToDashboard(role: String?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            onLoginError("User not found"); return
        }

        when (role) {
            "citizen" -> {
                // Check one-time onboarding flag: /userPrefs/{uid}/onboardingCompleted
                FirebaseDatabase.getInstance()
                    .getReference("userPrefs")
                    .child(uid)
                    .child("onboardingCompleted")
                    .get()
                    .addOnSuccessListener { snap ->
                        val completed = snap.getValue(Boolean::class.java) ?: false
                        val target = if (completed)
                            CitizenDashboardActivity::class.java
                        else
                            CitizenOnboardingActivity::class.java

                        startActivity(
                            Intent(this, target)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        finish()
                    }
                    .addOnFailureListener { e ->
                        // 🔁 CHANGE: if we can't read the flag, default to showing onboarding (safer)
                        Log.w("LoginActivity", "onboardingCompleted read failed: ${e.message}. Showing onboarding.")
                        startActivity(
                            Intent(this, CitizenOnboardingActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        finish()
                    }
            }
            "organization" -> {
                val target = OrganizationDashboardActivity::class.java
                startActivity(
                    Intent(this, target)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
            }
            else -> onLoginError("Unknown role: $role")
        }
    }

    private fun onLoginError(msg: String) {
        binding.progressBar.visibility = View.GONE
        binding.loginButton.isEnabled  = true
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
