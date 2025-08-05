// File: app/src/main/java/com/ucb/pawapp/citizen/ui/dashboard/ProfileFragment.kt
package com.ucb.pawapp.citizen.ui.dashboard

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import coil.transform.CircleCropTransformation
import com.ucb.pawapp.R
import com.ucb.pawapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Share the same dashboard VM instance (Hilt‑provided)
    private val vm: CitizenDashboardViewModel by viewModels({ requireActivity() })

    // Launcher to pick a new profile image
    private val pickImageLauncher = registerForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let {
            // TODO: implement upload in your ViewModel
            vm.uploadNewProfilePhoto(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 0️⃣ Profile photo: observe URL and load with Coil
        vm.profilePhotoUrl.observe(viewLifecycleOwner) { url ->
            val img = binding.imgProfilePhoto
            if (url.isNullOrBlank()) {
                img.setImageResource(R.drawable.avatar_placeholder)
            } else {
                img.load(url) {
                    placeholder(R.drawable.avatar_placeholder)
                    error(R.drawable.avatar_placeholder)
                    transformations(CircleCropTransformation())
                }
            }
        }

        // Tap the avatar to pick a new image
        binding.cardAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 1️⃣ Observe report stats
        vm.reportStats.observe(viewLifecycleOwner) { stats ->
            binding.tvTotalReports.text  = stats.total.toString()
            binding.tvOpenReports.text   = stats.open.toString()
            binding.tvClosedReports.text = stats.closed.toString()
        }

        // 2️⃣ Observe profile completion
        vm.profileCompletion.observe(viewLifecycleOwner) { percent ->
            binding.profileCompletionBar.progress = percent
            binding.tvCompletionPercent.text = "$percent% Complete"
        }

        // 3️⃣ Quick‑action buttons
        binding.btnSettings.setOnClickListener {
            // TODO: launch your Settings screen
        }
        binding.btnHelp.setOnClickListener {
            // TODO: launch Help & FAQ
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
