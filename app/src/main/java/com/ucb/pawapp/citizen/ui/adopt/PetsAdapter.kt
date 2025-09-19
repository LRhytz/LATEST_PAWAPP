package com.ucb.pawapp.citizen.ui.adopt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.ucb.pawapp.R
import com.ucb.pawapp.databinding.ItemPetBinding
import java.util.Locale

class PetsAdapter(
    private val onPetClick: (AdoptionPet) -> Unit = {},
    private val onFavoriteToggle: (AdoptionPet, Boolean) -> Unit = { _, _ -> }
) : ListAdapter<AdoptionPet, PetsAdapter.PetVH>(DIFF) {

    /** IDs that are currently favorited (kept in sync by AdoptFragment.observeFavorites) */
    private var favoriteIds: Set<String> = emptySet()

    /** Called by AdoptFragment when favorites change */
    fun setFavorites(ids: Set<String>) {
        favoriteIds = ids
        // items don’t change, only visuals; full refresh is fine and simple
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetVH {
        val binding = ItemPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PetVH(binding, ::isFav, onPetClick, onFavoriteToggle)
    }

    override fun onBindViewHolder(holder: PetVH, position: Int) {
        holder.bind(getItem(position))
    }

    private fun isFav(id: String?): Boolean = id != null && favoriteIds.contains(id)

    class PetVH(
        private val binding: ItemPetBinding,
        private val isFav: (String?) -> Boolean,
        private val onClick: (AdoptionPet) -> Unit,
        private val onFavoriteToggle: (AdoptionPet, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdoptionPet) = with(binding) {
            // Name
            tvName.text = item.name?.takeIf { it.isNotBlank() }
                ?: item.breed?.takeIf { it.isNotBlank() }
                        ?: item.species?.cap()
                        ?: "Unnamed"

            // Badges (tiny pills)
            val species = item.species?.trim()?.uppercase(Locale.getDefault())
            tvSpecies.isVisible = !species.isNullOrBlank()
            tvSpecies.text = species ?: ""

            val sizeAbbr = item.size?.let { abbrevSize(it) }
            tvSize.isVisible = !sizeAbbr.isNullOrBlank()
            tvSize.text = sizeAbbr ?: ""

            // Basic info
            val breed = item.breed?.takeIf { it.isNotBlank() } ?: item.species?.cap()
            val gender = item.gender?.cap()
            val age    = item.ageMonths?.let { formatAge(it) }
            val weight = item.weightLbs?.takeIf { it > 0 }?.let { "$it lbs" }
            tvBasicInfo.text = listOfNotNull(breed, gender, age, weight)
                .joinToString(" • ")
                .ifBlank { "—" }

            // Location
            tvLocation.text = item.location?.takeIf { it.isNotBlank() } ?: "Location unknown"

            // Photo
            Glide.with(ivPhoto)
                .load(item.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(ObjectKey(item.createdAt ?: 0L))
                .centerCrop()
                .into(ivPhoto)

            // Favorite icon (uses only btnFavorite; no dependency on favPill id)
            val fav = isFav(item.id)
            btnFavorite.setImageResource(
                if (fav) R.drawable.ic_favorite_24dp else R.drawable.ic_favorite_border_24dp
            )
            btnFavorite.alpha = if (fav) 1f else 0.9f

            // Clicks
            root.setOnClickListener { onClick(item) }
            btnFavorite.setOnClickListener { onFavoriteToggle(item, !isFav(item.id)) }
        }

        private fun String.cap(): String =
            replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        private fun abbrevSize(s: String): String = when (s.trim().lowercase(Locale.getDefault())) {
            "small", "sm", "s"  -> "SML"
            "medium", "md", "m" -> "MED"
            "large", "lg", "l"  -> "LRG"
            else -> s.uppercase(Locale.getDefault())
        }

        private fun formatAge(monthsRaw: Int): String {
            val months = monthsRaw.coerceAtLeast(0)
            val years = months / 12
            val remMonths = months % 12
            return when {
                years > 0 && remMonths > 0 -> "${years} ${if (years == 1) "yr" else "yrs"} ${remMonths} mo"
                years > 0 -> "${years} ${if (years == 1) "yr" else "yrs"}"
                else -> "$remMonths mo"
            }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<AdoptionPet>() {
            override fun areItemsTheSame(oldItem: AdoptionPet, newItem: AdoptionPet) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: AdoptionPet, newItem: AdoptionPet) =
                oldItem == newItem
        }
    }
}
