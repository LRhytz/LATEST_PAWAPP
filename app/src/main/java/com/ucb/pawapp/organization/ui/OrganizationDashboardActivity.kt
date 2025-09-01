package com.ucb.pawapp.organization.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ucb.pawapp.R
import com.ucb.pawapp.databinding.ActivityOrganizationDashboardBinding

class OrganizationDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrganizationDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrganizationDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_organization) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNavOrganization.setupWithNavController(navController)
    }
}
