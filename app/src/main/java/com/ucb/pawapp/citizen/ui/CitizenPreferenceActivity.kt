package com.ucb.pawapp.citizen.ui

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ucb.pawapp.R
import com.ucb.pawapp.citizen.model.CitizenPreferences
import com.ucb.pawapp.citizen.viewmodel.CitizenPreferenceViewModel

class CitizenPreferenceActivity : AppCompatActivity() {
    private val viewModel: CitizenPreferenceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_citizen_preference)

        val dogCheckBox = findViewById<CheckBox>(R.id.cb_dogs)
        val catCheckBox = findViewById<CheckBox>(R.id.cb_cats)
        val notifyCheckBox = findViewById<CheckBox>(R.id.cb_notify)
        val saveButton = findViewById<Button>(R.id.btn_save)

        saveButton.setOnClickListener {
            val species = buildList {
                if (dogCheckBox.isChecked) add("dog")
                if (catCheckBox.isChecked) add("cat")
            }
            val prefs = CitizenPreferences(
                preferredSpecies = species,
                receiveNotifications = notifyCheckBox.isChecked
            )
            viewModel.savePreferences(prefs)
        }

        viewModel.saveSuccess.observe(this) { success ->
            Toast.makeText(
                this,
                if (success) "Preferences saved!" else "Failed to save preferences",
                Toast.LENGTH_SHORT
            ).show()
            if (success) finish()
        }

        viewModel.preferences.observe(this) { prefs ->
            dogCheckBox.isChecked = "dog" in prefs.preferredSpecies
            catCheckBox.isChecked = "cat" in prefs.preferredSpecies
            notifyCheckBox.isChecked = prefs.receiveNotifications
        }

        viewModel.loadPreferences()
    }
}
