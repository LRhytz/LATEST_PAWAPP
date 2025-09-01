package com.ucb.pawapp.citizen.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ucb.pawapp.shared.model.AdoptionListing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptScreen(
    pets: List<AdoptionListing>,
    onFavorite: (AdoptionListing) -> Unit,
    onViewOrg: (String) -> Unit,
    onOpen: (AdoptionListing) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Adopt — Recommended for you") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pets, key = { it.id }) { pet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(pet) },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(pet.name.ifBlank { "Unnamed ${pet.species}" }, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("${pet.species} • ${pet.size} • ${pet.breed ?: "mixed"}")
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onFavorite(pet) }) { Text("❤ Save") }
                            Button(onClick = { onViewOrg(pet.orgId) }) { Text("View org") }
                        }
                    }
                }
            }
        }
    }
}
