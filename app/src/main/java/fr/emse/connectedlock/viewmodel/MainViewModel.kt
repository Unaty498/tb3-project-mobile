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
import fr.emse.connectedlock.data.AccessRule
import fr.emse.connectedlock.service.RetrofitClient
import fr.emse.connectedlock.service.ActivateBadgeRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val user = mutableStateOf<User?>(null)
    val badges = mutableStateOf<List<Badge>>(emptyList())
    val doors = mutableStateOf<List<Door>>(emptyList())
    val accessRules = mutableStateOf<List<AccessRule>>(emptyList())
    val isAuthenticated = mutableStateOf(false)
    val loginError = mutableStateOf<String?>(null)
    val isRefreshing = mutableStateOf(false)
    val isActivating = mutableStateOf(false)

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
                loginError.value = "Login failed: ${e.message}"
            }
        }
    }

    fun fetchData() {
        Log.d("MainViewModel", "Fetching data...")
        viewModelScope.launch {
            try {
                val currentUser = apiService.getCurrentUser()
                user.value = currentUser
                badges.value = apiService.getBadges(currentUser.id)
                doors.value = apiService.getDoors()
                accessRules.value = apiService.getAccess(currentUser.id)
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    Log.w("MainViewModel", "Unauthorized (401). Logging out.")
                    isAuthenticated.value = false
                    authManager.logout()
                } else {
                    Log.e("MainViewModel", "HttpException fetching data", e)
                }
            } catch (e: Exception) {
                // Handle data fetching error
                Log.e("MainViewModel", "Error fetching data", e)
            } finally {
                isRefreshing.value = false
            }
        }
    }

    fun refresh() {
        isRefreshing.value = true
        fetchData()
    }

    fun activateBadge(badgeId: String) {
        viewModelScope.launch {
            isActivating.value = true
            try {
                apiService.activateBadge(badgeId, ActivateBadgeRequest(physicallyMapped = true))
                fetchData() // Refresh list to show updated status
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error activating badge", e)
                // You might want to expose an error state here
            } finally {
                isActivating.value = false
            }
        }
    }

    fun logout() {
        isAuthenticated.value = false
        authManager.logout()
    }
}
