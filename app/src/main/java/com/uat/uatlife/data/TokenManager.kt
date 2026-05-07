package com.uat.uatlife.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear el DataStore una sola vez por Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "uatlife_prefs")

/**
 * Maneja la persistencia segura del token JWT y datos de sesión
 * usando Jetpack DataStore (reemplazo moderno de SharedPreferences).
 */
class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_MATRICULA_KEY = stringPreferencesKey("user_matricula")
        private val USER_TYPE_KEY = stringPreferencesKey("user_type")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val BAN_PERMANENTE_KEY = stringPreferencesKey("ban_permanente")
        private val SUSPENSION_HASTA_KEY = stringPreferencesKey("suspension_hasta")
    }

    /**
     * Obtiene el token JWT guardado (o null si no hay sesión).
     */
    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    /**
     * Guarda el token JWT y datos básicos del usuario.
     */
    suspend fun saveSession(
        token: String, 
        nombre: String, 
        matricula: String, 
        tipoUsuario: String, 
        userId: Int,
        banPermanente: Boolean = false,
        suspensionHasta: String? = null
    ) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_NAME_KEY] = nombre
            preferences[USER_MATRICULA_KEY] = matricula
            preferences[USER_TYPE_KEY] = tipoUsuario
            preferences[USER_ID_KEY] = userId.toString()
            preferences[BAN_PERMANENTE_KEY] = banPermanente.toString()
            preferences[SUSPENSION_HASTA_KEY] = suspensionHasta ?: ""
        }
    }

    /**
     * Obtiene el ID del usuario guardado.
     */
    fun getUserId(): Flow<Int?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]?.toIntOrNull()
        }
    }

    /**
     * Obtiene el nombre del usuario guardado.
     */
    fun getUserName(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_NAME_KEY]
        }
    }

    /**
     * Obtiene la matrícula guardada.
     */
    fun getUserMatricula(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_MATRICULA_KEY]
        }
    }

    /**
     * Obtiene el tipo de usuario (ej. "alumno" o "moderador").
     */
    fun getUserType(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_TYPE_KEY]
        }
    }

    /**
     * Cambia el tipo de usuario en caliente.
     */
    suspend fun setUserType(tipoUsuario: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_TYPE_KEY] = tipoUsuario
        }
    }

    /**
     * Obtiene si el usuario tiene ban permanente.
     */
    fun getBanPermanente(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[BAN_PERMANENTE_KEY] == "true"
        }
    }

    /**
     * Obtiene la fecha de suspensión.
     */
    fun getSuspensionHasta(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            val s = preferences[SUSPENSION_HASTA_KEY]
            if (s.isNullOrBlank()) null else s
        }
    }

    /**
     * Actualiza el estado de sanción.
     */
    suspend fun updateSanctionStatus(ban: Boolean, suspension: String?) {
        context.dataStore.edit { preferences ->
            preferences[BAN_PERMANENTE_KEY] = ban.toString()
            preferences[SUSPENSION_HASTA_KEY] = suspension ?: ""
        }
    }

    /**
     * Cierra la sesión eliminando todos los datos guardados.
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
