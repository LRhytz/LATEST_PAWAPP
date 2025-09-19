package com.ucb.pawapp.citizen.ui.adopt

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.ucb.pawapp.citizen.repository.FavoriteRepository
import com.ucb.pawapp.databinding.FragmentAdoptBinding

class AdoptFragment : Fragment() {

    private var _binding: FragmentAdoptBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseDatabase
    private lateinit var rootRef: DatabaseReference

    private var petsQuery: Query? = null
    private var petsListener: ValueEventListener? = null

    private lateinit var favRepo: FavoriteRepository
    private var favListener: ValueEventListener? = null

    private lateinit var adapter: PetsAdapter

    private val ADOPTIONS_NODE = "adoptions"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdoptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseDatabase.getInstance()
        rootRef = db.reference
        favRepo = FavoriteRepository()

        adapter = PetsAdapter(
            onPetClick = { pet ->
                val id = pet.id
                if (id.isNullOrBlank()) {
                    Log.w("Adopt", "Clicked pet has no id; ignoring")
                } else {
                    startActivity(
                        Intent(requireContext(), PetDetailsActivity::class.java)
                            .putExtra(PetDetailsActivity.EXTRA_PET_ID, id)
                    )
                }
            },
            onFavoriteToggle = { pet, makeFav ->
                val id = pet.id ?: return@PetsAdapter
                favRepo.setFavorite(requireContext(), id, makeFav)
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to update favorite", Toast.LENGTH_SHORT).show()
                    }
            }
        )

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@AdoptFragment.adapter
        }

        // Observe favorites -> adapter hearts stay in sync.
        favListener = favRepo.observeFavorites(requireContext()) { ids ->
            adapter.setFavorites(ids)
        }

        binding.swipe.setOnRefreshListener { reload() }
        reload()
    }

    private fun reload() {
        showLoading(true)
        detachListener()

        petsQuery = rootRef.child(ADOPTIONS_NODE)
            .orderByChild("createdAt")
            .limitToLast(200)

        petsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<AdoptionPet>(snapshot.childrenCount.toInt())
                for (child in snapshot.children) {
                    val pet = child.getValue(AdoptionPet::class.java) ?: AdoptionPet()
                    pet.id = pet.id ?: child.key
                    if (pet.createdAt == null) {
                        pet.createdAt = child.longOrNull("createdAt")
                    }
                    list.add(pet)
                }
                list.sortByDescending { it.createdAt ?: 0L }
                adapter.submitList(list)
                showLoading(false)
                binding.empty.isVisible = list.isEmpty()
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                binding.empty.isVisible = true
                binding.empty.text = error.message
            }
        }

        petsQuery!!.addValueEventListener(petsListener!!)
    }

    private fun detachListener() {
        petsListener?.let { listener -> petsQuery?.removeEventListener(listener) }
        petsListener = null
        petsQuery = null
    }

    private fun showLoading(loading: Boolean) = with(binding) {
        progress.isVisible = loading
        swipe.isRefreshing = false
        recycler.isGone = loading
        empty.isGone = loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        detachListener()
        favRepo.removeObserver(requireContext(), favListener)
        favListener = null
        binding.recycler.adapter = null
        _binding = null
    }
}

/** Safely read a Long (handles Number or String values) */
private fun DataSnapshot.longOrNull(key: String): Long? {
    val v = child(key).value
    return when (v) {
        is Number -> v.toLong()
        is String -> v.toLongOrNull()
        else -> null
    }
}
