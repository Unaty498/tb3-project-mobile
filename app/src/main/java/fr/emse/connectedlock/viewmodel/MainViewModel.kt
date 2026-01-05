package fr.emse.connectedlock.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.emse.connectedlock.auth.AuthManager
import fr.emse.connectedlock.data.Badge
import fr.emse.connectedlock.data.Door
import fr.emse.connectedlock.data.User
import fr.emse.connectedlock.service.RetrofitClient
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val user = mutableStateOf<User?>(null)
    val badges = mutableStateOf<List<Badge>>(emptyList())
    val doors = mutableStateOf<List<Door>>(emptyList())
    val isAuthenticated = mutableStateOf(false)
    val loginError = mutableStateOf<String?>(null)

    private val apiService = RetrofitClient.getInstance(application)
    private val authManager = AuthManager(application)

    init {
        checkAuthentication()
    }

    private fun checkAuthentication() {
        isAuthenticated.value = authManager.getAuthState().isAuthorized
        if (isAuthenticated.value) {
            fetchData()
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loginError.value = null // Clear previous error
            try {
                Log.d("MainViewModel", "Attempting login for user: $username")
                authManager.login(username, password)
                Log.d("MainViewModel", "Login successful")
                isAuthenticated.value = true
                fetchData()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Login failed", e)
                if (e.message?.contains("iss claim is not valid") == true) {
                    loginError.value = "Login failed: Issuer mismatch. Keycloak issued a token for 'localhost' but app used '10.0.2.2'.\nFix: Set KC_HOSTNAME_STRICT=false in Keycloak or use 'adb reverse tcp:80 tcp:80'."
                } else {
                    loginError.value = "Login failed: ${e.message}"
                }
            }
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            try {
                user.value = apiService.getCurrentUser()
                badges.value = apiService.getBadges()
                doors.value = apiService.getDoors()
            } catch (e: Exception) {
                // Handle data fetching error
            }
        }
    }
}
