package com.ucb.pawapp.citizen.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ucb.pawapp.R
import com.ucb.pawapp.citizen.viewmodel.CitizenAdoptionViewModel

class AdoptFragment : Fragment(R.layout.fragment_adopt) {

    private val vm: CitizenAdoptionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.load()

        val cv = view.findViewById<ComposeView>(R.id.composeAdopt)
        cv.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        cv.setContent {
            MaterialTheme {
                val list by vm.recommended.collectAsState()
                AdoptScreen(
                    pets = list,
                    onFavorite = { vm.favorite(it) },
                    onViewOrg = { vm.viewedOrganization(it) },
                    onOpen = { /* navigate if you have details */ }
                )
            }
        }
    }
}
