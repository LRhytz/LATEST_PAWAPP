package com.ucb.pawapp.citizen.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ucb.pawapp.R

class AdoptFragment : Fragment(R.layout.fragment_adopt) {
    private val viewModel: PetAdoptionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // find the ComposeView by its ID
        val cv = view.findViewById<ComposeView>(R.id.composeAdopt)
        cv.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        cv.setContent {
            MaterialTheme {
                AdoptScreen(
                    pets = viewModel.pets.value,
                    onViewDetails = { pet ->
                        // TODO: NavController.navigate to details screen
                    },
                    onToggleFavorite = { pet ->
                        viewModel.toggleFavorite(pet.id)
                    }
                )
            }
        }
    }
}
