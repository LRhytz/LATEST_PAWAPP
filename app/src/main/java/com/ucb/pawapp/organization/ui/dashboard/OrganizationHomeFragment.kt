// file: app/src/main/java/com/ucb/pawapp/organization/ui/dashboard/OrganizationHomeFragment.kt
package com.ucb.pawapp.organization.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer // <-- Use Observer instead of observe
import com.ucb.pawapp.databinding.FragmentOrganizationHomeBinding
import com.ucb.pawapp.organization.viewmodel.OrganizationHomeViewModel

class OrganizationHomeFragment : Fragment() {

    private var _binding: FragmentOrganizationHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrganizationHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrganizationHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Solution 1: Use Observer class (recommended)
        viewModel.items.observe(viewLifecycleOwner, Observer { items ->
            // e.g. binding.recycler.adapter = MyAdapter(items)
        })

        // Alternative Solution 2: Remove the lifecycle KTX import and use standard observe
        // viewModel.items.observe(viewLifecycleOwner) { items ->
        //     // e.g. binding.recycler.adapter = MyAdapter(items)
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}