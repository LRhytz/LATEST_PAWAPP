package com.ucb.pawapp.citizen.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ucb.pawapp.R
import com.ucb.pawapp.citizen.model.Incident
import com.ucb.pawapp.databinding.ItemIncidentBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class IncidentsAdapter(
    private val onIncidentClicked: (Incident) -> Unit
) : ListAdapter<Incident, IncidentsAdapter.IncidentViewHolder>(IncidentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidentViewHolder {
        val binding = ItemIncidentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IncidentViewHolder(binding, parent.context, onIncidentClicked)
    }

    override fun onBindViewHolder(holder: IncidentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class IncidentViewHolder(
        private val binding: ItemIncidentBinding,
        private val context: Context,
        private val onIncidentClicked: (Incident) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(incident: Incident) = with(binding) {
            // Text setup
            incidentTitle.text = incident.title
            incidentLocation.text = incident.location
            incidentType.text = incident.type
            incidentTimestamp.text = getRelativeTimeSpan(incident.timestamp)

            // Image
            incidentImage.load(incident.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.error_image)
                transformations(RoundedCornersTransformation(8f))
            }

            // Status indicator
            val statusColor = when (incident.status) {
                "NEW" -> R.color.status_new
                "IN_PROGRESS" -> R.color.status_in_progress
                "RESOLVED" -> R.color.status_resolved
                "URGENT" -> R.color.status_urgent
                else -> R.color.status_new
            }
            statusIndicator.setBackgroundColor(ContextCompat.getColor(context, statusColor))

            // Type chip color
            val chipColor = when (incident.type) {
                "Stray Animal" -> R.color.chip_stray
                "Injured Animal" -> R.color.chip_injured
                "Lost Pet" -> R.color.chip_lost
                "Adoption Request" -> R.color.chip_adopt
                else -> R.color.chip_background
            }
            incidentType.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(context, chipColor)
            )

            // Click listener
            root.setOnClickListener { onIncidentClicked(incident) }
        }

        private fun getRelativeTimeSpan(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            return when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
                diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
                diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
                else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }
}

class IncidentDiffCallback : DiffUtil.ItemCallback<Incident>() {
    override fun areItemsTheSame(oldItem: Incident, newItem: Incident): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Incident, newItem: Incident): Boolean =
        oldItem == newItem
}
