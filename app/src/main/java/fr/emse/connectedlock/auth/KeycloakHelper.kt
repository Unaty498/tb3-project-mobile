package fr.emse.connectedlock.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import net.openid.appauth.*
import java.io.IOException
import androidx.core.net.toUri

object KeycloakHelper {

    private const val SERVER_URL = "http://10.0.2.2:8080/realms/connected-lock"
    private const val CLIENT_ID = "mobile-app"
    private const val REDIRECT_URI = "fr.emse.connectedlock:/oauth2redirect"

    private lateinit var authState: AuthState
    @SuppressLint("StaticFieldLeak")
    private lateinit var authService: AuthorizationService

    fun init(context: Context) {
        authState = AuthState()
        authService = AuthorizationService(context)
    }

    fun login(context: Context) {
        val serviceConfig = AuthorizationServiceConfiguration(
            "$SERVER_URL/protocol/openid-connect/auth".toUri(),
            "$SERVER_URL/protocol/openid-connect/token".toUri()
        )

        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            REDIRECT_URI.toUri()
        )

        val authRequest = authRequestBuilder
            .setScope("openid profile email")
            .build()

        val authIntent = authService.getAuthorizationRequestIntent(authRequest)

        // Use a CustomTabsIntent to launch the authorization request
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, authIntent.data!!)
    }

    fun handleAuthorizationResponse(intent: Intent, callback: (Result<Unit>) -> Unit) {
        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)

        authState.update(resp, ex)

        if (resp != null) {
            authService.performTokenRequest(resp.createTokenExchangeRequest()) { tokenResponse, exception ->
                if (tokenResponse != null) {
                    authState.update(tokenResponse, exception)
                    callback(Result.success(Unit))
                } else {
                    callback(Result.failure(exception ?: IOException("Unknown token exchange error")))
                }
            }
        } else {
            callback(Result.failure(ex ?: IOException("Unknown authorization error")))
        }
    }

    fun getAccessToken(): String? {
        return authState.accessToken
    }
}
