package com.rykersoft.appmanager.entitlements

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

private const val ADMIN_EMAIL = "heavensounds@gmail.com"

data class HubAccountState(
    val configured: Boolean,
    val googleConfigured: Boolean,
    val user: FirebaseUser?,
    val email: String?,
    val hasGoogleProvider: Boolean = false,
    val hasPasswordProvider: Boolean = false,
    val entitlements: Map<String, Boolean> = emptyMap(),
    val isAdmin: Boolean = false
)

data class AdminManagedUser(
    val uid: String,
    val email: String,
    val displayName: String = "",
    val createdAtMillis: Long? = null,
    val entitlements: Map<String, Boolean> = emptyMap()
)

data class AdminCredentialField(
    val field: String,
    val label: String,
    val provider: String,
    val required: Boolean = true
)

data class AdminManagedApp(
    val packageId: String,
    val displayName: String,
    val credentialFields: List<AdminCredentialField> = emptyList(),
    val configuredFields: Set<String> = emptySet()
)

class EntitlementRepository(private val context: Context) {

    fun isConfigured(): Boolean = RykerSoftFirebase.isConfigured()

    fun isGoogleConfigured(): Boolean = RykerSoftFirebase.isGoogleConfigured()

    fun isAdminEmail(email: String?): Boolean = email?.lowercase() == ADMIN_EMAIL.lowercase()

    fun accountState(): Flow<HubAccountState> = callbackFlow {
        if (!RykerSoftFirebase.ensureInitialized(context)) {
            trySend(
                HubAccountState(
                    configured = false,
                    googleConfigured = false,
                    user = null,
                    email = null
                )
            )
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
                        email = null,
                        isAdmin = false
                    )
                )
                return
            }
            val isAdmin = isAdminEmail(user.email)
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
                            isAdmin = isAdmin,
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
                        isAdmin = isAdmin,
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

    suspend fun listUsersForAdmin(): List<AdminManagedUser> {
        val auth = RykerSoftFirebase.auth(context) ?: return emptyList()
        if (!isAdminEmail(auth.currentUser?.email)) return emptyList()
        val db = RykerSoftFirebase.db(context) ?: return emptyList()

        val usersSnap = db.collection("users").get().await()
        return usersSnap.documents.mapNotNull { userDoc ->
            val uid = userDoc.id
            val email = userDoc.getString("email")?.trim().orEmpty()
                .ifBlank { "Unknown" }
            val entitlementSnap = db.collection("users")
                .document(uid)
                .collection("entitlements")
                .document("apps")
                .get()
                .await()

            val entitlements = mutableMapOf<String, Boolean>()
            entitlementSnap.data?.forEach { (key, value) ->
                if (value is Boolean) entitlements[key] = value
            }
            AdminManagedUser(
                uid = uid,
                email = email,
                displayName = userDoc.getString("displayName").orEmpty(),
                createdAtMillis = userDoc.getTimestamp("createdAt")?.toDate()?.time,
                entitlements = entitlements
            )
        }.sortedWith(
            compareBy<AdminManagedUser> { it.email.lowercase() }.thenBy { it.uid }
        )
    }

    fun observeAdminUserDirectory(): Flow<List<AdminManagedUser>> = callbackFlow {
        val auth = RykerSoftFirebase.auth(context)
        val db = RykerSoftFirebase.db(context)
        if (auth == null || db == null || !isAdminEmail(auth.currentUser?.email)) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val users = snapshot?.documents.orEmpty().map { document ->
                AdminManagedUser(
                    uid = document.id,
                    email = document.getString("email").orEmpty().ifBlank { "Unknown" },
                    displayName = document.getString("displayName").orEmpty(),
                    createdAtMillis = document.getTimestamp("createdAt")?.toDate()?.time
                )
            }
            trySend(users)
        }
        awaitClose { registration.remove() }
    }.distinctUntilChanged()

    suspend fun listAppsForAdmin(): List<AdminManagedApp> {
        requireAdmin()
        val db = RykerSoftFirebase.db(context) ?: throw IllegalStateException("Firestore unavailable.")
        ensureInformantCapabilityManifest()
        val capabilityDocs = db.collection("appCapabilities").get().await().documents
        val apps = capabilityDocs.mapNotNull { document ->
            if (document.getBoolean("proEnabled") != true) return@mapNotNull null
            val credentialFields = (document.get("credentialFields") as? List<*>)
                .orEmpty()
                .mapNotNull { raw ->
                    val entry = raw as? Map<*, *> ?: return@mapNotNull null
                    val field = entry["field"] as? String ?: return@mapNotNull null
                    AdminCredentialField(
                        field = field,
                        label = (entry["label"] as? String).orEmpty().ifBlank { field },
                        provider = (entry["provider"] as? String).orEmpty().ifBlank { field },
                        required = entry["required"] as? Boolean ?: true
                    )
                }
            val configured = db.collection("providerKeys").document(document.id).get().await()
                .data.orEmpty()
                .filterValues { it is String && it.isNotBlank() }
                .keys
            AdminManagedApp(
                packageId = document.id,
                displayName = document.getString("displayName").orEmpty().ifBlank {
                    AiUnlockPackages.displayName(document.id)
                },
                credentialFields = credentialFields,
                configuredFields = configured
            )
        }
        if (apps.isNotEmpty()) return apps.sortedBy { it.displayName.lowercase() }
        return AiUnlockPackages.ORDERED.map {
            AdminManagedApp(packageId = it, displayName = AiUnlockPackages.displayName(it))
        }
    }

    /**
     * Keep INFORMANT's provider declaration current without replacing secrets or
     * unrelated manifest fields. The verified administrator is the only client
     * permitted by hub rules to perform this merge.
     */
    private suspend fun ensureInformantCapabilityManifest() {
        requireAdmin()
        val db = RykerSoftFirebase.db(context) ?: throw IllegalStateException("Firestore unavailable.")
        val ref = db.collection("appCapabilities").document(AiUnlockPackages.INFORMANT)
        val current = ref.get().await()
        val existingFields = (current.get("credentialFields") as? List<*>)
            .orEmpty()
            .mapNotNull { raw ->
                val entry = raw as? Map<*, *> ?: return@mapNotNull null
                val field = entry["field"] as? String ?: return@mapNotNull null
                AdminCredentialField(
                    field = field,
                    label = (entry["label"] as? String).orEmpty().ifBlank { field },
                    provider = (entry["provider"] as? String).orEmpty().ifBlank { field },
                    required = entry["required"] as? Boolean ?: true
                )
            }
        val mergedFields = (existingFields + AiUnlockPackages.INFORMANT_CREDENTIAL_FIELDS)
            .associateBy { it.field }
            .values
            .map { field ->
                mapOf(
                    "field" to field.field,
                    "label" to field.label,
                    "provider" to field.provider,
                    "required" to field.required
                )
            }
        val openAiAlreadyDeclared = existingFields.any { it.field == "openai" }
        if (current.exists() && openAiAlreadyDeclared) return

        ref.set(
            mapOf(
                "packageName" to AiUnlockPackages.INFORMANT,
                "displayName" to AiUnlockPackages.displayName(AiUnlockPackages.INFORMANT),
                "proEnabled" to true,
                "providerModel" to "trusted-family",
                "credentialFields" to mergedFields,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun setProviderKeys(packageId: String, values: Map<String, String>) {
        requireAdmin()
        val db = RykerSoftFirebase.db(context) ?: throw IllegalStateException("Firestore unavailable.")
        val capability = db.collection("appCapabilities").document(packageId).get().await()
        if (!capability.exists() || capability.getBoolean("proEnabled") != true) {
            throw IllegalArgumentException("No deployed Pro capability manifest exists for $packageId.")
        }
        val allowedFields = (capability.get("credentialFields") as? List<*>)
            .orEmpty()
            .mapNotNull { (it as? Map<*, *>)?.get("field") as? String }
            .toSet()
        val cleanValues = values.mapValues { it.value.trim() }
            .filterValues { it.isNotBlank() }
        if (cleanValues.isEmpty()) throw IllegalArgumentException("Enter at least one credential value.")
        if (!allowedFields.containsAll(cleanValues.keys)) {
            throw IllegalArgumentException("Credential fields do not match the deployed manifest for $packageId.")
        }
        val update = mutableMapOf<String, Any>()
        update.putAll(cleanValues)
        update["updatedAt"] = FieldValue.serverTimestamp()
        db.collection("providerKeys").document(packageId)
            .set(update, SetOptions.merge())
            .await()
    }

    suspend fun setUserAppProAccess(targetUid: String, packageId: String, enabled: Boolean) {
        val auth = RykerSoftFirebase.auth(context) ?: throw IllegalStateException("Firebase Auth unavailable.")
        if (!isAdminEmail(auth.currentUser?.email)) {
            throw IllegalStateException("Only the RykerSoft administrator can grant pro access.")
        }
        if (targetUid.isBlank()) {
            throw IllegalArgumentException("Missing target user UID.")
        }
        val db = RykerSoftFirebase.db(context) ?: throw IllegalStateException("Firestore unavailable.")
        val capability = db.collection("appCapabilities").document(packageId).get().await()
        if ((!capability.exists() || capability.getBoolean("proEnabled") != true) && packageId !in AiUnlockPackages.ALL) {
            throw IllegalArgumentException("This package does not support pro access: $packageId")
        }
        val targetUserDoc = db.collection("users").document(targetUid).get().await()
        if (!targetUserDoc.exists()) {
            throw IllegalArgumentException("No RykerSoft account exists for UID $targetUid.")
        }
        db.collection("users")
            .document(targetUid)
            .collection("entitlements")
            .document("apps")
            .set(mapOf(packageId to enabled), SetOptions.merge())
            .await()
    }

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
        val profile = db.collection("users").document(user.uid)
        db.runTransaction { transaction ->
            val existing = transaction.get(profile)
            val values = mutableMapOf<String, Any>(
                "email" to (user.email ?: ""),
                "displayName" to (user.displayName ?: ""),
                "updatedAt" to Timestamp.now()
            )
            if (!existing.exists() || existing.getTimestamp("createdAt") == null) {
                values["createdAt"] = Timestamp.now()
            }
            transaction.set(profile, values, SetOptions.merge())
        }.await()
    }

    private fun requireAdmin() {
        val auth = RykerSoftFirebase.auth(context) ?: throw IllegalStateException("Firebase Auth unavailable.")
        if (!isAdminEmail(auth.currentUser?.email)) {
            throw IllegalStateException("Only the RykerSoft administrator can perform this operation.")
        }
    }

    private fun requireAuth(): FirebaseAuth {
        if (!RykerSoftFirebase.ensureInitialized(context)) {
            throw IllegalStateException("Add RykerSoft Firebase keys to .env (see firebase/SEED.md).")
        }
        return RykerSoftFirebase.auth(context)
            ?: throw IllegalStateException("Firebase Auth unavailable.")
    }
}
