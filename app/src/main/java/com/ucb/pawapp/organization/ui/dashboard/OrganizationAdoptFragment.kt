package com.ucb.pawapp.organization.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ucb.pawapp.R
import com.ucb.pawapp.databinding.FragmentOrganizationAdoptBinding
import com.ucb.pawapp.databinding.ItemOrgListingBinding
import com.ucb.pawapp.organization.viewmodel.OrganizationMyListingsViewModel
import com.ucb.pawapp.shared.model.AdoptionListing

class OrganizationAdoptFragment : Fragment() {

    private var _binding: FragmentOrganizationAdoptBinding? = null
    private val binding get() = _binding!!

    private val vm: OrganizationMyListingsViewModel by viewModels()

    private val adapter = ListingsAdapter(
        onDelete = { id ->
            vm.delete(id) { ok ->
                Toast.makeText(
                    requireContext(),
                    if (ok) "Deleted" else "Delete failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrganizationAdoptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvListings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListings.setHasFixedSize(true)
        binding.rvListings.adapter = adapter

        binding.fabAdd.setOnClickListener {
            findNavController()
                .navigate(R.id.action_organizationAdoptFragment_to_organizationPostListingFragment)
        }

        vm.items.observe(viewLifecycleOwner) { list ->
            adapter.submit(list)
            binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        vm.busy.observe(viewLifecycleOwner) { busy ->
            binding.progress.visibility = if (busy) View.VISIBLE else View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        vm.startListening()
    }

    override fun onStop() {
        vm.stopListening()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/* ======================= RecyclerView adapter ======================= */

private class ListingsAdapter(
    private val onDelete: (String) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<ListingsAdapter.VH>() {

    private val items = mutableListOf<AdoptionListing>()

    fun submit(data: List<AdoptionListing>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOrgListingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding, onDelete)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class VH(
        private val binding: ItemOrgListingBinding,
        private val onDelete: (String) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdoptionListing) {
            // Image (HTTPS) with placeholder
            val url = item.photoUrl
            if (!url.isNullOrBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                Glide.with(binding.ivPetPhoto)
                    .load(url)
                    .centerCrop()
                    .placeholder(R.drawable.gray_rect)
                    .error(R.drawable.gray_rect)
                    .into(binding.ivPetPhoto)
            } else {
                binding.ivPetPhoto.setImageResource(R.drawable.gray_rect)
            }

            // Text fields
            binding.tvPetName.text = item.name.ifBlank { "Unnamed" }
            binding.tvSpecies.text = item.species.ifBlank { "—" }.uppercase()

            binding.tvAge.text = item.ageMonths?.let { m ->
                when {
                    m < 1  -> "0 mo"
                    m < 12 -> "$m ${if (m == 1) "month" else "months"}"
                    else   -> {
                        val y = m / 12
                        "$y ${if (y == 1) "year" else "years"}"
                    }
                }
            } ?: "—"

            binding.tvGender.text = item.gender?.replaceFirstChar { it.titlecase() } ?: "—"

            val breed = item.breed?.replaceFirstChar { it.titlecase() }
            val size  = item.size.replaceFirstChar { it.titlecase() }
            binding.tvBreedSize.text = listOfNotNull(breed, size).joinToString(" • ").ifBlank { "—" }

            binding.tvLocation.text =
                item.location?.takeIf { it.isNotBlank() }?.let { "📍 $it" } ?: "📍 —"

            binding.tvTimePosted.text =
                if (item.createdAt > 0)
                    DateUtils.getRelativeTimeSpanString(item.createdAt).toString()
                else ""

            binding.tvStatus.text = "ACTIVE"
            binding.llQuickStats.visibility = View.GONE

            // Row click -> details (pass id + fallback object)
            binding.root.setOnClickListener {
                val ctx = binding.root.context
                if (item.id.isNotEmpty()) {
                    ctx.startActivity(
                        Intent(ctx, OrganizationPetDetailsActivity::class.java)
                            .putExtra(OrganizationPetDetailsActivity.EXTRA_LISTING_ID, item.id)
                            .putExtra(OrganizationPetDetailsActivity.EXTRA_LISTING_FALLBACK, item) // OK here
                    )
                } else {
                    Toast.makeText(ctx, "Missing listing id", Toast.LENGTH_SHORT).show()
                }
            }

            // ⋮ menu -> Edit/Delete
            binding.btnMoreOptions.setOnClickListener { v ->
                PopupMenu(v.context, v).apply {
                    menu.add(0, 1, 0, "Edit")
                    menu.add(0, 2, 1, "Delete")
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            1 -> {
                                val ctx = v.context
                                if (item.id.isNotEmpty()) {
                                    // IMPORTANT: only pass the ID to the Edit screen
                                    ctx.startActivity(
                                        Intent(ctx, OrganizationEditListingActivity::class.java)
                                            .putExtra(OrganizationEditListingActivity.EXTRA_LISTING_ID, item.id)
                                    )
                                } else {
                                    Toast.makeText(ctx, "Missing listing id", Toast.LENGTH_SHORT).show()
                                }
                                true
                            }
                            2 -> { if (item.id.isNotEmpty()) onDelete(item.id); true }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}
