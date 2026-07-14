package com.example.ui.screens

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.BuildConfig

suspend fun doRealGoogleSignIn(context: Context): FirebaseUserResult? {
    try {
        val credentialManager = CredentialManager.create(context)
        val webClientId = context.getString(com.example.R.string.default_web_client_id)
        if (webClientId.isEmpty()) {
            Log.e("GoogleSignIn", "Web Client ID not found in google-services.json")
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )

        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            
            // Sign in to Firebase Auth using this idToken
            val firebaseAuth = FirebaseAuth.getInstance()
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(authCredential).await()
            val user = authResult.user
            if (user != null) {
                return FirebaseUserResult(
                    displayName = user.displayName ?: "Usuario de Google",
                    email = user.email ?: "",
                    avatarUrl = user.photoUrl?.toString() ?: "",
                    uid = user.uid
                )
            }
        }
    } catch (e: Exception) {
        Log.e("GoogleSignIn", "Google Sign in failed", e)
        throw e
    }
    return null
}

data class FirebaseUserResult(
    val displayName: String,
    val email: String,
    val avatarUrl: String,
    val uid: String
)
