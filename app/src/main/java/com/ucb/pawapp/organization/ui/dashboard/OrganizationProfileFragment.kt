// organization/src/main/java/com/ucb/pawapp/organization/ui/dashboard/OrganizationProfileFragment.kt
package com.ucb.pawapp.organization.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.ucb.pawapp.LandingActivity
import com.ucb.pawapp.R
import com.ucb.pawapp.databinding.FragmentOrganizationProfileBinding

class OrganizationProfileFragment : Fragment(R.layout.fragment_organization_profile) {

    private var _binding: FragmentOrganizationProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentOrganizationProfileBinding.bind(view)

        // … your existing setup code …

        binding.btnLogout.setOnClickListener {
            // 1️⃣ Sign out
            FirebaseAuth.getInstance().signOut()

            // 2️⃣ Launch LandingActivity clearing the entire task stack
            Intent(requireContext(), LandingActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
 