package com.rykersoft.appmanager.entitlements

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

object GoogleSignInManager {
    suspend fun requestIdToken(context: Context, serverClientId: String): String {
        require(serverClientId.isNotBlank() && !serverClientId.startsWith("REPLACE_")) {
            "Google sign-in is not configured for this release."
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(serverClientId.trim())
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val credential = CredentialManager.create(context)
            .getCredential(context, request)
            .credential

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("Google did not return a supported sign-in credential.")
        }
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }

    suspend fun clearCredentialState(context: Context) {
        CredentialManager.create(context)
            .clearCredentialState(ClearCredentialStateRequest())
    }
}
