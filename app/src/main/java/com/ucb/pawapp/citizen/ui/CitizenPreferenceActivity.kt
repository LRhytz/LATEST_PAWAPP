package com.ucb.pawapp.citizen.ui

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ucb.pawapp.R
import com.ucb.pawapp.citizen.model.CitizenPreferences
import com.ucb.pawapp.citizen.viewmodel.CitizenPreferenceViewModel

class CitizenPreferenceActivity : AppCompatActivity() {

    private lateinit var viewModel: CitizenPreferenceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_citizen_preference)

        val dogCheckBox = findViewById<CheckBox>(R.id.cb_dogs)
        val catCheckBox = findViewById<CheckBox>(R.id.cb_cats)
        val notifyCheckBox = findViewById<CheckBox>(R.id.cb_notify)
        val saveButton = findViewById<Button>(R.id.btn_save)

        saveButton.setOnClickListener {
            val selectedSpecies = mutableListOf<String>()
            if (dogCheckBox.isChecked) selectedSpecies.add("dogs")
            if (catCheckBox.isChecked) selectedSpecies.add("cats")

            val prefs = CitizenPreferences(
                preferredSpecies = selectedSpecies,
                receiveNotifications = notifyCheckBox.isChecked
            )

            viewModel.savePreferences(prefs)
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save preferences", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadPreferences()
        viewModel.preferences.observe(this) { prefs ->
            dogCheckBox.isChecked = "dogs" in prefs.preferredSpecies
            catCheckBox.isChecked = "cats" in prefs.preferredSpecies
            notifyCheckBox.isChecked = prefs.receiveNotifications
        }
    }
}
