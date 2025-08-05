package com.ucb.pawapp.citizen.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import com.ucb.pawapp.R
import com.ucb.pawapp.citizen.ui.report.ReportIncidentActivity
import com.ucb.pawapp.databinding.ActivityCitizenDashboardBinding

@AndroidEntryPoint
class CitizenDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitizenDashboardBinding
    private val viewModel: CitizenDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1️⃣ Inflate
        binding = ActivityCitizenDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2️⃣ NavHost ↔ NavController
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_citizen) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNav.setupWithNavController(navController)

        // 4️⃣ FAB: launch report
        binding.fabReport.setOnClickListener {
            navigateToReportIncident()
        }

        // 5️⃣ (Optional) observe ViewModel for badges, errors…
        viewModel.uiState.observe(this) { ui ->
            // e.g. show badge on Profile icon:
            val badge = binding.bottomNav.getOrCreateBadge(R.id.profileFragment)
            badge.isVisible = ui.notificationCount > 0
            badge.number = ui.notificationCount
        }
    }

    fun navigateToReportIncident() {
        startActivity(Intent(this, ReportIncidentActivity::class.java))
    }
}
