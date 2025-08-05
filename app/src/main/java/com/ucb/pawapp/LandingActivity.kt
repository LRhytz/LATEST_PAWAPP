// File: app/src/main/java/com/ucb/pawapp/LandingActivity.kt
package com.ucb.pawapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ucb.pawapp.citizen.ui.dashboard.CitizenDashboardActivity
import com.ucb.pawapp.databinding.ActivityLandingBinding

class LandingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAnimations()
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()

        // Auto‐route signed‑in citizens via Realtime Database
        FirebaseAuth.getInstance().currentUser?.let { user ->
            binding.loadingOverlay.visibility = View.VISIBLE

            val usersRef = FirebaseDatabase
                .getInstance()
                .getReference("users")

            usersRef.child(user.uid).child("role")
                .get()
                .addOnSuccessListener { snap ->
                    binding.loadingOverlay.visibility = View.GONE
                    val role = snap.getValue(String::class.java)
                    if (role.equals("citizen", ignoreCase = true)) {
                        startCitizenDashboard()
                    }
                }
                .addOnFailureListener {
                    binding.loadingOverlay.visibility = View.GONE
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(
                        this,
                        "Unable to verify your login. Please sign in again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun startCitizenDashboard() {
        Intent(this, CitizenDashboardActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    private fun setupAnimations() {
        // initial hide
        binding.appLogo.alpha = 0f
        binding.landingTitle.alpha = 0f
        binding.landingSubtitle.alpha = 0f
        binding.buttonCard.alpha = 0f
        binding.topWave.alpha = 0f
        binding.bottomWave.alpha = 0f

        // fade in logo and waves and text
        Handler(Looper.getMainLooper()).postDelayed({
            binding.appLogo.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1000)
                .withEndAction {
                    binding.landingTitle.animate().alpha(1f).setDuration(500).start()
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.landingSubtitle.animate().alpha(1f).setDuration(500).start()
                    }, 100)
                }
                .start()

            binding.topWave.animate().alpha(1f).setDuration(1000).start()
            binding.bottomWave.animate().alpha(1f).setDuration(1000).start()
        }, 300)

        // slide up the card
        Handler(Looper.getMainLooper()).postDelayed({
            val slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
            binding.buttonCard.startAnimation(slideUp)
            binding.buttonCard.alpha = 1f
            ViewCompat.setElevation(binding.buttonCard, 16f)
        }, 800)

        // touch‐scale effect
        val touchScaleListener = { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }
        binding.citizenLoginBtn.setOnTouchListener(touchScaleListener)
        binding.organizationLoginBtn.setOnTouchListener(touchScaleListener)
    }

    private fun setupClickListeners() {
        binding.citizenLoginBtn.setOnClickListener {
            it.isPressed = true
            Handler(Looper.getMainLooper()).postDelayed({
                it.isPressed = false
                startActivity(
                    Intent(this, LoginActivity::class.java)
                        .putExtra("userType", "Citizen")
                )
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }, 100)
        }
        binding.organizationLoginBtn.setOnClickListener {
            it.isPressed = true
            Handler(Looper.getMainLooper()).postDelayed({
                it.isPressed = false
                startActivity(
                    Intent(this, LoginActivity::class.java)
                        .putExtra("userType", "Organization")
                )
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }, 100)
        }
    }
}
