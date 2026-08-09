package com.rykersoft.appmanager.entitlements

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

data class HubAccountState(
    val configured: Boolean,
    val googleConfigured: Boolean,
    val user: FirebaseUser?,
    val email: String?,
    val hasGoogleProvider: Boolean = false,
    val hasPasswordProvider: Boolean = false,
    val entitlements: Map<String, Boolean> = emptyMap()
)

class EntitlementRepository(private val context: Context) {

    fun isConfigured(): Boolean = RykerSoftFirebase.isConfigured()

    fun isGoogleConfigured(): Boolean = RykerSoftFirebase.isGoogleConfigured()

    fun accountState(): Flow<HubAccountState> = callbackFlow {
        if (!RykerSoftFirebase.ensureInitialized(context)) {
            trySend(HubAccountState(configured = false, googleConfigured = false, user = null, email = null))
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
                trySend(
                    HubAccountState(
                        configured = true,
                        googleConfigured = RykerSoftFirebase.isGoogleConfigured(),
                        user = null,
                        email = null
                    )
                )
                return
            }
            val providers = user.providerData.map { it.providerId }.toSet()
            val doc = db.collection("users").document(user.uid)
                .collection("entitlements").document("apps")
            entitlementReg = doc.addSnapshotListener { snap, error ->
                if (error != null) {
                    trySend(
                        HubAccountState(
                            configured = true,
                            googleConfigured = RykerSoftFirebase.isGoogleConfigured(),
                            user = user,
                            email = user.email,
                            hasGoogleProvider = GoogleAuthProvider.PROVIDER_ID in providers,
                            hasPasswordProvider = "password" in providers,
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
                        googleConfigured = RykerSoftFirebase.isGoogleConfigured(),
                        user = user,
                        email = user.email,
                        hasGoogleProvider = GoogleAuthProvider.PROVIDER_ID in providers,
                        hasPasswordProvider = "password" in providers,
                        entitlements = map
                    )
                )
            }
        }

        val authListener = FirebaseAuth.IdTokenListener { firebaseAuth ->
            listenEntitlements(firebaseAuth.currentUser)
        }
        auth.addIdTokenListener(authListener)
        listenEntitlements(auth.currentUser)

        awaitClose {
            auth.removeIdTokenListener(authListener)
            entitlementReg?.remove()
        }
    }.distinctUntilChanged()

    suspend fun signInLegacy(email: String, password: String) {
        val auth = requireAuth()
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        ensureUserProfile()
    }

    suspend fun signInWithGoogle(idToken: String) {
        val auth = requireAuth()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        try {
            auth.signInWithCredential(credential).await()
        } catch (error: FirebaseAuthUserCollisionException) {
            throw IllegalStateException(
                "This email belongs to a legacy RykerSoft account. Use the migration panel, sign in with its password, then link Google.",
                error
            )
        }
        ensureUserProfile()
    }

    suspend fun linkGoogleAccount(idToken: String) {
        val auth = requireAuth()
        val user = auth.currentUser
            ?: throw IllegalStateException("Sign in to the legacy account before linking Google.")
        if (user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }) return
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        try {
            user.linkWithCredential(credential).await()
            user.reload().await()
        } catch (error: FirebaseAuthUserCollisionException) {
            throw IllegalStateException(
                "That Google account is already linked to another RykerSoft account. No accounts were merged; contact support to resolve ownership safely.",
                error
            )
        }
        ensureUserProfile()
    }

    suspend fun sendPasswordReset(email: String) {
        if (email.isBlank()) throw IllegalArgumentException("Enter the legacy account email.")
        requireAuth().sendPasswordResetEmail(email.trim()).await()
    }

    fun signOut() {
        RykerSoftFirebase.auth(context)?.signOut()
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
