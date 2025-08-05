package com.ucb.pawapp.citizen.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ucb.pawapp.R

class DonateFragment : Fragment(R.layout.fragment_donate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tv_donate).text = "Support our causes ❤️"
    }
}
