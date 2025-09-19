package com.ucb.pawapp.citizen.repository

import android.content.Context
import android.provider.Settings
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

class FavoriteRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private fun userId(context: Context): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (!uid.isNullOrBlank()) return uid
        // device-scoped fallback if not signed in
        val androidId = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ANDROID_ID
        )
        return "anon-$androidId"
    }

    private fun favoritesRef(context: Context): DatabaseReference =
        db.reference.child("userFavorites").child(userId(context))

    /** Observe the full favorite set for the current user. */
    fun observeFavorites(
        context: Context,
        onChange: (Set<String>) -> Unit
    ): ValueEventListener {
        val ref = favoritesRef(context)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ids = snapshot.children
                    .mapNotNull { if (it.getValue(Boolean::class.java) == true) it.key else null }
                    .toSet()
                onChange(ids)
            }
            override fun onCancelled(error: DatabaseError) {
                onChange(emptySet())
            }
        }
        ref.addValueEventListener(listener)
        return listener
    }

    /** Observe a single pet's favorite state. */
    fun observeFavoritePet(
        context: Context,
        petId: String,
        onChange: (Boolean) -> Unit
    ): ValueEventListener {
        val ref = favoritesRef(context).child(petId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onChange(snapshot.getValue(Boolean::class.java) == true)
            }
            override fun onCancelled(error: DatabaseError) { /* ignore */ }
        }
        ref.addValueEventListener(listener)
        return listener
    }

    fun removeObserver(context: Context, listener: ValueEventListener?) {
        if (listener != null) favoritesRef(context).removeEventListener(listener)
    }

    fun removePetObserver(context: Context, petId: String, listener: ValueEventListener?) {
        if (listener != null) favoritesRef(context).child(petId).removeEventListener(listener)
    }

    /** Write favorite toggle. */
    fun setFavorite(context: Context, petId: String, favorite: Boolean): Task<Void> {
        val node = favoritesRef(context).child(petId)
        return if (favorite) node.setValue(true) else node.removeValue()
    }

    /** Read once (not used by list, handy for one-off checks). */
    suspend fun isFavoriteOnce(context: Context, petId: String): Boolean {
        val snap = favoritesRef(context).child(petId).get().await()
        return snap.getValue(Boolean::class.java) == true
    }
}
