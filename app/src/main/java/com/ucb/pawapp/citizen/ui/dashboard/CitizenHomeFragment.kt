package com.ucb.pawapp.citizen.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ucb.pawapp.citizen.viewmodel.CitizenHomeViewModel
import com.ucb.pawapp.databinding.FragmentHomeBinding

class CitizenHomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CitizenHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnReportIncident.setOnClickListener {
            (activity as? CitizenDashboardActivity)?.navigateToReportIncident()
        }
    }

    private fun observeViewModel() {
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvWelcome.text = "Welcome, $name! 🐾"
        }

        viewModel.incidents.observe(viewLifecycleOwner) { incidents ->
            val adapter = IncidentsAdapter { incident ->
                // TODO: Handle click on incident here (navigate to details or show dialog)
                // Example:
                // Toast.makeText(requireContext(), "Clicked: ${incident.title}", Toast.LENGTH_SHORT).show()
            }
            adapter.submitList(incidents)
            binding.incidentsRecyclerView.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
