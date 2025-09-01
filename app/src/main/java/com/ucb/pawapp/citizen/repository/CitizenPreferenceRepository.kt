package com.ucb.pawapp.citizen.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ucb.pawapp.citizen.model.CitizenPreferences
import kotlinx.coroutines.tasks.await

class CitizenPreferenceRepository {

    private val db = Firebase.database.reference
    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

    suspend fun loadPreferences(): CitizenPreferences {
        val snap = db.child("userPrefs").child(uid).get().await()
        return snap.getValue(CitizenPreferences::class.java) ?: CitizenPreferences()
    }

    suspend fun saveExplicitPreferences(preferredSpecies: List<String>, receive: Boolean) {
        val updates = mapOf(
            "preferredSpecies" to preferredSpecies,
            "receiveNotifications" to receive
        )
        db.child("userPrefs").child(uid).updateChildren(updates).await()
    }

    // === Onboarding ===
    suspend fun needsOnboarding(): Boolean {
        val snap = db.child("userPrefs").child(uid).child("onboardingCompleted").get().await()
        return !(snap.getValue(Boolean::class.java) ?: false)
    }

    suspend fun saveOnboardingAnswers(
        selectedSpecies: List<String>,
        selectedSizes: List<String>,
        selectedTopics: List<String>
    ) {
        val now = System.currentTimeMillis()

        val adoptionW = mutableMapOf<String, Double>()
        selectedSpecies.forEach { s -> adoptionW["species_${s}"] = 0.6 } // strong seed
        selectedSizes.forEach { z -> adoptionW["size_${z}"] = 0.4 }     // medium seed

        val articleW = mutableMapOf<String, Double>()
        selectedTopics.forEach { t -> articleW[t] = 0.5 }

        val updates = mapOf<String, Any?>(
            "preferredSpecies" to selectedSpecies,
            "adoptionWeights" to adoptionW,
            "articleWeights" to articleW,
            "onboardingCompleted" to true,
            "lastUpdated" to now
        )
        db.child("userPrefs").child(uid).updateChildren(updates).await()
    }

    // === Learning with decay on write ===
    suspend fun applyWeightDeltas(
        tagDeltas: Map<String, Double>,
        orgDeltas: Map<String, Double> = emptyMap()
    ) {
        val ref = db.child("userPrefs").child(uid)
        val snap = ref.get().await()

        val now = System.currentTimeMillis()
        val last = snap.child("lastUpdated").getValue(Long::class.java) ?: now
        val days = ((now - last) / 86_400_000L).coerceAtLeast(0)
        val decay = Math.pow(0.99, days.toDouble()) // 1%/day

        fun readDoubles(path: String) =
            (snap.child(path).value as? Map<String, Any?>)
                ?.mapValues { (_, v) -> (v as Number).toDouble() }
                ?.toMutableMap() ?: mutableMapOf()

        val adoptionW = readDoubles("adoptionWeights").apply { keys.toList().forEach { this[it] = this[it]!! * decay } }
        val articleW  = readDoubles("articleWeights").apply { keys.toList().forEach { this[it] = this[it]!! * decay } }
        val orgAff    = readDoubles("orgAffinity").apply { keys.toList().forEach { this[it] = this[it]!! * decay } }

        tagDeltas.forEach { (k, dv) ->
            if (k.startsWith("tag_")) articleW[k] = ((articleW[k] ?: 0.0) + dv).coerceIn(-1.0, 3.0)
            else adoptionW[k] = ((adoptionW[k] ?: 0.0) + dv).coerceIn(-1.0, 3.0)
        }
        orgDeltas.forEach { (orgId, dv) ->
            orgAff[orgId] = ((orgAff[orgId] ?: 0.0) + dv).coerceIn(0.0, 2.0)
        }

        ref.updateChildren(
            mapOf(
                "adoptionWeights" to adoptionW,
                "articleWeights" to articleW,
                "orgAffinity" to orgAff,
                "lastUpdated" to now
            )
        ).await()
    }
}
