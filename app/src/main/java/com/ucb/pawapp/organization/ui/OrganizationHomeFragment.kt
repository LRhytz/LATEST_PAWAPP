package com.ucb.pawapp.organization.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ucb.pawapp.R
import com.ucb.pawapp.organization.viewmodel.OrganizationHomeViewModel

class OrganizationHomeFragment : Fragment() {

    private val viewModel: OrganizationHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val welcomeText = view.findViewById<TextView>(R.id.tv_welcome)

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            welcomeText.text = "Welcome, $name 👥"
        }

        return view
    }
}
