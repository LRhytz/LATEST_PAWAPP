// File: app/src/main/java/com/ucb/pawapp/LoginActivity.kt
package com.ucb.pawapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ucb.pawapp.citizen.ui.CitizenSignupActivity
import com.ucb.pawapp.citizen.ui.dashboard.CitizenDashboardActivity
import com.ucb.pawapp.databinding.ActivityLoginBinding
import com.ucb.pawapp.organization.ui.OrganizationSignupActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var userType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Simple Fade‑In Animation
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.loginTitle.startAnimation(fadeIn)
        binding.loginCard.startAnimation(fadeIn)

        // Extract whether this is Citizen or Organization login
        userType = intent.getStringExtra("userType")
        if (userType.isNullOrBlank()) {
            Toast.makeText(this, "No user type specified!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Tweak UI per user type
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

        // 🔐 Login click
        binding.loginButton.setOnClickListener {
            val email = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            // Validate
            binding.usernameLayout.error = null
            binding.passwordLayout.error = null
            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty())   binding.usernameLayout.error = "Email required"
                if (password.isEmpty()) binding.passwordLayout.error = "Password required"
                return@setOnClickListener
            }

            // Show progress
            binding.progressBar.visibility = View.VISIBLE
            binding.loginButton.isEnabled = false

            // Firebase sign‑in
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid
                    if (uid == null) {
                        onLoginError("UID not found")
                        return@addOnSuccessListener
                    }

                    // Fetch role from Realtime Database
                    FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .child("role")
                        .get()
                        .addOnSuccessListener { snap ->
                            binding.progressBar.visibility = View.GONE
                            binding.loginButton.isEnabled = true

                            val role     = snap.getValue(String::class.java)?.lowercase()
                            val expected = userType!!.lowercase()
                            Log.d("LoginActivity", "DB role=$role expected=$expected")

                            if (role == expected) {
                                Toast.makeText(this, "$userType login successful", Toast.LENGTH_SHORT).show()
                                Intent(this, CitizenDashboardActivity::class.java)
                                    .apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    .also { startActivity(it) }
                            } else {
                                FirebaseAuth.getInstance().signOut()
                                onLoginError("Access denied. Wrong portal.")
                            }
                        }
                        .addOnFailureListener { e ->
                            onLoginError("Failed to fetch user data: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    onLoginError("Login failed: ${e.message}")
                }
        }

        // “Forgot password” stub
        binding.forgotPasswordText.setOnClickListener {
            Toast.makeText(this, "Forgot password tapped", Toast.LENGTH_SHORT).show()
        }

        // “Register” link
        binding.registerText.setOnClickListener {
            val signupClazz = if (userType == "Citizen")
                CitizenSignupActivity::class.java
            else
                OrganizationSignupActivity::class.java

            startActivity(Intent(this, signupClazz))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun onLoginError(msg: String) {
        binding.progressBar.visibility = View.GONE
        binding.loginButton.isEnabled = true
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
