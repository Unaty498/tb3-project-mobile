package fr.emse.connectedlock.auth

import android.content.Context
import android.net.Uri
import fr.emse.connectedlock.service.ApiService
import fr.emse.connectedlock.service.RetrofitClient
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import androidx.core.net.toUri

class AuthManager(private val context: Context) {

    private val authStateManager = AuthStateManager(context)
    private val apiService: ApiService = RetrofitClient.getInstance(context)

    suspend fun login(username: String, password: String) {
        val tokenResponse = apiService.getToken(
            clientId = "mobile-app",
            username = username,
            password = password,
            grantType = "password"
        )

        // Build a proper AuthState object using the token response from the server.
        // This ensures the expiration time and other details are handled correctly by AppAuth.
        val serviceConfig = AuthorizationServiceConfiguration(
            "http://10.0.2.2/keycloak/realms/connected-lock/protocol/openid-connect/auth".toUri(), // Dummy endpoint, not used in this flow
            "http://10.0.2.2/keycloak/realms/connected-lock/protocol/openid-connect/token".toUri()
        )

        // Create a minimal TokenRequest; this is required by the TokenResponse.Builder
        val tokenRequest = TokenRequest.Builder(serviceConfig, "mobile-app")
            .setScope("openid")
            .setGrantType("password")
            .setAdditionalParameters(mapOf("username" to username, "password" to password))
            .build()

        val appAuthTokenResponse = TokenResponse.Builder(tokenRequest)
            .setAccessToken(tokenResponse.accessToken)
            .setAccessTokenExpirationTime(System.currentTimeMillis() + (tokenResponse.expiresIn * 1000))
            .setRefreshToken(tokenResponse.refreshToken)
            .setTokenType(tokenResponse.tokenType)
            .build()

        val authState = AuthState() // Create a new, empty state
        authState.update(appAuthTokenResponse, null) // Update it with our complete token response

        authStateManager.write(authState)
    }

    fun getAuthState(): AuthState {
        return authStateManager.read()
    }

    fun logout() {
        val authState = AuthState()
        authStateManager.write(authState)
        authStateManager.clear()
    }
}
