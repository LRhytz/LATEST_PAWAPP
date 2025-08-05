package com.ucb.pawapp.citizen.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

data class Pet(
    val id: Int,
    val name: String,
    val type: String,
    val breed: String,
    val age: String,
    val location: String,
    val adoptionFee: String,
    val imageUrl: String
)

class PetAdoptionViewModel : ViewModel() {
    private val _pets = mutableStateOf(samplePets())
    val pets: State<List<Pet>> = _pets

    private val _favorites = mutableStateOf(setOf<Int>())
    val favorites: State<Set<Int>> = _favorites

    fun toggleFavorite(petId: Int) {
        _favorites.value = if (_favorites.value.contains(petId)) {
            _favorites.value - petId
        } else {
            _favorites.value + petId
        }
    }

    private fun samplePets() = listOf(
        Pet(
            1, "Luna", "Dog", "Golden Retriever",
            "2 years", "Happy Paws Shelter, NYC", "$300",
            "https://images.unsplash.com/photo-1552053831-71594a27632d?w=400"
        ),
        Pet(
            2, "Whiskers", "Cat", "Maine Coon",
            "3 years", "Feline Friends Rescue, Brooklyn", "$150",
            "https://images.unsplash.com/photo-1574158622682-e40e69881006?w=400"
        ),
        Pet(
            3, "Buddy", "Dog", "Labrador Mix",
            "1 year", "City Animal Shelter, Manhattan", "$250",
            "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400"
        ),
        Pet(
            4, "Mittens", "Cat", "Persian",
            "4 years", "Whiskers & Tails, Queens", "$200",
            "https://images.unsplash.com/photo-1571566882372-1598d88abd90?w=400"
        )
    )
}
