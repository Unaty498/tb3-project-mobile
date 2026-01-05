package fr.emse.connectedlock.auth

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {

    private val authStateManager = AuthStateManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Skip adding token for the token endpoint to avoid issues
        if (request.url.toString().contains("openid-connect/token")) {
            return chain.proceed(request)
        }

        val authState = authStateManager.read()

        Log.d(TAG, "Intercepting request for: ${request.url}")

        if (authState.isAuthorized) {
            val accessToken = authState.accessToken
            Log.d(TAG, "User is authorized. Adding token to header.")
            val newRequest = request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            return chain.proceed(newRequest)
        } else {
            Log.d(TAG, "User is not authorized. Proceeding without token.")
        }

        return chain.proceed(request)
    }

    companion object {
        private const val TAG = "AuthInterceptor"
    }
}