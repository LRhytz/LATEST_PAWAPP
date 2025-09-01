package com.ucb.pawapp.organization.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ucb.pawapp.databinding.FragmentOrganizationDonateBinding

class OrganizationDonateFragment : Fragment() {

    private var _binding: FragmentOrganizationDonateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrganizationDonateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: implement donation workflow
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
