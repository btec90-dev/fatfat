package com.example.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * AuthenticationManager utilizes the Android Credential Manager API (androidx.credentials)
 * to handle Google Sign-In and authenticate users with Firebase Authentication.
 */
class AuthenticationManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun signInWithGoogle(serverClientId: String): Result<FirebaseUser> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(serverClientId)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            handleCredential(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun handleCredential(result: GetCredentialResponse): Result<FirebaseUser> {
        val credential = result.credential
        if (credential is androidx.credentials.CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleCredential = GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken,
                    null
                )
                val authResult = firebaseAuth.signInWithCredential(googleCredential).await()
                val user = authResult.user
                if (user != null) {
                    return Result.success(user)
                } else {
                    return Result.failure(Exception("Firebase user is null after sign-in."))
                }
            } catch (e: Exception) {
                return Result.failure(e)
            }
        } else {
            return Result.failure(Exception("Unexpected credential type received."))
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
