package com.example.entitlements

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

data class HubAccountState(
    val configured: Boolean,
    val user: FirebaseUser?,
    val email: String?,
    val entitlements: Map<String, Boolean> = emptyMap()
)

class EntitlementRepository(private val context: Context) {

    fun isConfigured(): Boolean = RykerSoftFirebase.isConfigured()

    fun accountState(): Flow<HubAccountState> = callbackFlow {
        if (!RykerSoftFirebase.ensureInitialized(context)) {
            trySend(HubAccountState(configured = false, user = null, email = null))
            awaitClose { }
            return@callbackFlow
        }

        val auth = RykerSoftFirebase.auth(context)!!
        val db = RykerSoftFirebase.db(context)!!
        var entitlementReg: ListenerRegistration? = null

        fun listenEntitlements(user: FirebaseUser?) {
            entitlementReg?.remove()
            entitlementReg = null
            if (user == null) {
                trySend(HubAccountState(configured = true, user = null, email = null))
                return
            }
            val doc = db.collection("users").document(user.uid)
                .collection("entitlements").document("apps")
            entitlementReg = doc.addSnapshotListener { snap, error ->
                if (error != null) {
                    trySend(
                        HubAccountState(
                            configured = true,
                            user = user,
                            email = user.email,
                            entitlements = emptyMap()
                        )
                    )
                    return@addSnapshotListener
                }
                val map = mutableMapOf<String, Boolean>()
                snap?.data?.forEach { (key, value) ->
                    if (value is Boolean) map[key] = value
                }
                trySend(
                    HubAccountState(
                        configured = true,
                        user = user,
                        email = user.email,
                        entitlements = map
                    )
                )
            }
        }

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            listenEntitlements(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(authListener)
        listenEntitlements(auth.currentUser)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            entitlementReg?.remove()
        }
    }.distinctUntilChanged()

    suspend fun signIn(email: String, password: String) {
        val auth = requireAuth()
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        ensureUserProfile()
    }

    suspend fun signUp(email: String, password: String) {
        val auth = requireAuth()
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
        ensureUserProfile()
    }

    fun signOut() {
        RykerSoftFirebase.auth(context)?.signOut()
    }

    /**
     * Looks up SHA-256(code) under unlockCodes/{hash} and merges granted packages
     * into the signed-in user's entitlements document.
     * @param packageName if set, only unlock that package (must be listed on the code).
     */
    suspend fun unlockWithCode(rawCode: String, packageName: String? = null): List<String> {
        val auth = requireAuth()
        val user = auth.currentUser ?: throw IllegalStateException("Sign in to unlock apps.")
        val db = RykerSoftFirebase.db(context) ?: throw IllegalStateException("Firebase not configured.")

        if (rawCode.isBlank()) throw IllegalArgumentException("Enter an unlock code.")
        val hash = UnlockCodeHasher.sha256Hex(rawCode)

        val snap = db.collection("unlockCodes").document(hash).get().await()
        if (!snap.exists()) {
            throw IllegalArgumentException("Invalid unlock code.")
        }

        @Suppress("UNCHECKED_CAST")
        val packages = (snap.get("packages") as? List<*>)
            ?.mapNotNull { it as? String }
            ?.filter { AiUnlockPackages.isUnlockable(it) }
            ?: emptyList()

        if (packages.isEmpty()) {
            throw IllegalStateException("Unlock code has no packages configured.")
        }

        val toGrant = if (packageName != null) {
            if (packageName !in packages) {
                throw IllegalArgumentException("This code does not unlock that app.")
            }
            listOf(packageName)
        } else {
            packages
        }

        val updates = HashMap<String, Any>()
        toGrant.forEach { updates[it] = true }
        updates["updatedAt"] = Timestamp.now()

        db.collection("users").document(user.uid)
            .collection("entitlements").document("apps")
            .set(updates, SetOptions.merge())
            .await()

        ensureUserProfile()
        return toGrant
    }

    private suspend fun ensureUserProfile() {
        val auth = RykerSoftFirebase.auth(context) ?: return
        val user = auth.currentUser ?: return
        val db = RykerSoftFirebase.db(context) ?: return
        db.collection("users").document(user.uid).set(
            mapOf(
                "email" to (user.email ?: ""),
                "updatedAt" to Timestamp.now()
            ),
            SetOptions.merge()
        ).await()
    }

    private fun requireAuth(): FirebaseAuth {
        if (!RykerSoftFirebase.ensureInitialized(context)) {
            throw IllegalStateException("Add RykerSoft Firebase keys to .env (see firebase/SEED.md).")
        }
        return RykerSoftFirebase.auth(context)
            ?: throw IllegalStateException("Firebase Auth unavailable.")
    }
}
