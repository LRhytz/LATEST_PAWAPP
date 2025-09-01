// File: app/src/main/java/com/ucb/pawapp/citizen/ui/CitizenOnboardingActivity.kt
package com.ucb.pawapp.citizen.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.ucb.pawapp.R
import com.ucb.pawapp.citizen.viewmodel.CitizenOnboardingViewModel
import kotlinx.coroutines.launch

class CitizenOnboardingActivity : AppCompatActivity() {

    private val vm: CitizenOnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_citizen_onboarding)

        // ---- Species ----
        val cbDog  = findViewById<CheckBox>(R.id.cbDog)
        val cbCat  = findViewById<CheckBox>(R.id.cbCat)
        val cbBoth = findViewById<CheckBox>(R.id.cbBoth)

        // ---- Sizes ----
        val cbSmall  = findViewById<CheckBox>(R.id.cbSmall)
        val cbMedium = findViewById<CheckBox>(R.id.cbMedium)
        val cbLarge  = findViewById<CheckBox>(R.id.cbLarge)

        // ---- Age (currently unused in save call, harmless to keep UI) ----
        val cbPuppy  = findViewById<CheckBox>(R.id.cbPuppy)
        val cbYoung  = findViewById<CheckBox>(R.id.cbYoung)
        val cbAdult  = findViewById<CheckBox>(R.id.cbAdult)
        val cbSenior = findViewById<CheckBox>(R.id.cbSenior)

        // ---- Topics ----
        val cbDogTraining  = findViewById<CheckBox>(R.id.cbDogTraining)
        val cbDogHealth    = findViewById<CheckBox>(R.id.cbDogHealth)
        val cbDogNutrition = findViewById<CheckBox>(R.id.cbDogNutrition)
        val cbDogGrooming  = findViewById<CheckBox>(R.id.cbDogGrooming)
        val cbCatHealth    = findViewById<CheckBox>(R.id.cbCatHealth)
        val cbCatBehavior  = findViewById<CheckBox>(R.id.cbCatBehavior)
        val cbCatNutrition = findViewById<CheckBox>(R.id.cbCatNutrition)
        val cbCatLitter    = findViewById<CheckBox>(R.id.cbCatLitter)

        // ---- Actions ----
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val tvSkip      = findViewById<TextView>(R.id.tvSkip)

        // If already completed, bounce to dashboard
        lifecycleScope.launch {
            if (!vm.needsOnboarding()) {
                goToDashboard(); return@launch
            }
        }

        btnContinue.setOnClickListener {
            // Species
            val species: List<String> = if (cbBoth.isChecked) {
                listOf("dog", "cat")
            } else {
                buildList {
                    if (cbDog.isChecked) add("dog")
                    if (cbCat.isChecked) add("cat")
                }
            }
            if (species.isEmpty()) {
                Toast.makeText(this, "Pick at least one species", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sizes
            val sizes = buildList {
                if (cbSmall.isChecked) add("small")
                if (cbMedium.isChecked) add("medium")
                if (cbLarge.isChecked) add("large")
            }

            // Topics -> tag_*
            val topics = buildList {
                if (cbDogTraining.isChecked)  add("tag_dog_training")
                if (cbDogHealth.isChecked)    add("tag_dog_health")
                if (cbDogNutrition.isChecked) add("tag_dog_nutrition")
                if (cbDogGrooming.isChecked)  add("tag_dog_grooming")
                if (cbCatHealth.isChecked)    add("tag_cat_health")
                if (cbCatBehavior.isChecked)  add("tag_cat_behavior")
                if (cbCatNutrition.isChecked) add("tag_cat_nutrition")
                if (cbCatLitter.isChecked)    add("tag_cat_litter")
            }

            // Save using your existing VM signature
            vm.save(species, sizes, topics)
        }

        // "Skip for now" — do NOT set onboardingCompleted; will prompt again on next app open/login
        tvSkip.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseDatabase.getInstance()
                    .getReference("userPrefs")
                    .child(uid)
                    .updateChildren(mapOf("lastSkipAt" to ServerValue.TIMESTAMP))
            }
            goToDashboard()
        }

        vm.done.observe(this) { ok ->
            if (ok == true) {
                Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show()
                goToDashboard()
            } else if (ok == false) {
                Toast.makeText(this, "Failed to save. Check network.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToDashboard() {
        startActivity(Intent(this, com.ucb.pawapp.citizen.ui.dashboard.CitizenDashboardActivity::class.java))
        finish()
    }
}
