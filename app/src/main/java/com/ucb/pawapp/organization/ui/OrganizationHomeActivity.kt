package com.ucb.pawapp.organization.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ucb.pawapp.R

class OrganizationHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_container)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OrganizationHomeFragment())
            .commit()
    }
}
