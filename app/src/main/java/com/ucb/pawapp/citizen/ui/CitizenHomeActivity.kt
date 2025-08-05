package com.ucb.pawapp.citizen.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ucb.pawapp.R
import com.ucb.pawapp.citizen.ui.dashboard.CitizenHomeFragment

class CitizenHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_container)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CitizenHomeFragment())
            .commit()
    }
}
