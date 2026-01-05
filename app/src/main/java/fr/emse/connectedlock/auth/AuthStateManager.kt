package fr.emse.connectedlock.auth

import android.content.Context
import net.openid.appauth.AuthState
import androidx.core.content.edit

class AuthStateManager(context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun read(): AuthState {
        val stateJson = prefs.getString("stateJson", null)
        return if (stateJson != null) {
            AuthState.jsonDeserialize(stateJson)
        } else {
            AuthState()
        }
    }

    fun write(state: AuthState) {
        prefs.edit {
            putString("stateJson", state.jsonSerializeString())
        }
    }
}