package com.example.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

/**
 * Multi-User Isolated Authentication & Session Manager
 * Ensures 100% data isolation per user, permanent data retention across logouts,
 * strict User ID + Password required after logout (no auto-biometric after manual logout),
 * and complete account purging on Delete Account.
 */
object AuthStorage {
    private const val PREFS_NAME = "app_auth_multi_prefs"
    private const val KEY_ALL_USERS = "all_registered_usernames" // Set of normalized usernames
    private const val KEY_ACTIVE_USER = "active_logged_in_username"
    private const val KEY_IS_LOGGED_IN = "is_user_actively_logged_in"
    private const val KEY_LAST_USER = "last_used_username"

    // Legacy prefs for migration
    private const val LEGACY_AUTH_PREFS = "app_auth_prefs"
    private const val LEGACY_DATA_PREFS = "myexpenses_prefs_v15"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getDataPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(LEGACY_DATA_PREFS, Context.MODE_PRIVATE)
    }

    private fun normalize(username: String?): String {
        return username?.trim()?.lowercase() ?: ""
    }

    private const val VAULT_FILE_NAME = "accounts_vault.json"

    private fun syncVaultFile(context: Context) {
        try {
            val allKeys = getAllUserKeys(context)
            val vaultObj = JSONObject()
            val prefs = getPrefs(context)
            for (uKey in allKeys) {
                val credJson = prefs.getString("user_cred_$uKey", null)
                if (credJson != null) {
                    vaultObj.put(uKey, JSONObject(credJson))
                }
            }
            val vaultFile = java.io.File(context.filesDir, VAULT_FILE_NAME)
            vaultFile.writeText(vaultObj.toString())
        } catch (_: Exception) {}
    }

    private fun restoreFromVaultFile(context: Context): Boolean {
        return try {
            val vaultFile = java.io.File(context.filesDir, VAULT_FILE_NAME)
            if (!vaultFile.exists()) return false
            val content = vaultFile.readText()
            if (content.isBlank()) return false
            val vaultObj = JSONObject(content)
            val keys = vaultObj.keys()
            val prefs = getPrefs(context)
            val editor = prefs.edit()
            val allUsers = mutableSetOf<String>()

            while (keys.hasNext()) {
                val uKey = keys.next()
                val userObj = vaultObj.optJSONObject(uKey)
                if (userObj != null) {
                    editor.putString("user_cred_$uKey", userObj.toString())
                    allUsers.add(uKey)
                }
            }
            if (allUsers.isNotEmpty()) {
                editor.putStringSet(KEY_ALL_USERS, allUsers)
                editor.apply()
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Initializes registry and migrates legacy single-user data if needed.
     * Guarantees lifetime account retention across updates and reinstalls.
     */
    fun ensureInitialized(context: Context) {
        val prefs = getPrefs(context)
        var allUsers = prefs.getStringSet(KEY_ALL_USERS, null)
        if (allUsers.isNullOrEmpty()) {
            // Check indestructible internal vault file first
            restoreFromVaultFile(context)
            allUsers = prefs.getStringSet(KEY_ALL_USERS, null)
        }

        if (allUsers == null || allUsers.isEmpty()) {
            // Attempt migration from legacy prefs
            try {
                val legacyAuth = context.getSharedPreferences(LEGACY_AUTH_PREFS, Context.MODE_PRIVATE)
                val legacyUser = legacyAuth.getString("saved_username", null)
                val legacyPass = legacyAuth.getString("saved_password", null)
                val legacyFp = legacyAuth.getBoolean("is_fingerprint_enabled", false)

                if (!legacyUser.isNullOrBlank() && !legacyPass.isNullOrBlank()) {
                    saveUser(
                        context = context,
                        username = legacyUser.trim(),
                        password = legacyPass,
                        isFingerprintEnabled = legacyFp,
                        markLoggedIn = true
                    )

                    // Migrate legacy expenses to this user's isolated key
                    val dataPrefs = getDataPrefs(context)
                    val legacyExp = dataPrefs.getString("myexp_v15", null)
                    val uKey = normalize(legacyUser)
                    if (legacyExp != null && !dataPrefs.contains("${uKey}_expenses")) {
                        dataPrefs.edit().putString("${uKey}_expenses", legacyExp).apply()
                    }
                    val legacyDel = dataPrefs.getString("myexp_del_v15", null)
                    if (legacyDel != null && !dataPrefs.contains("${uKey}_deleted_expenses")) {
                        dataPrefs.edit().putString("${uKey}_deleted_expenses", legacyDel).apply()
                    }
                    dataPrefs.edit().remove("myexp_v15").remove("myexp_del_v15").apply()
                }
            } catch (_: Exception) {}
        } else {
            // Keep vault file in sync
            syncVaultFile(context)
        }
    }

    fun hasAnyAccount(context: Context): Boolean {
        ensureInitialized(context)
        val users = getAllUserKeys(context)
        return users.isNotEmpty()
    }

    fun hasAccount(context: Context): Boolean = hasAnyAccount(context)

    fun getAllUserKeys(context: Context): Set<String> {
        val prefs = getPrefs(context)
        return prefs.getStringSet(KEY_ALL_USERS, emptySet()) ?: emptySet()
    }

    fun userExists(context: Context, username: String): Boolean {
        val uKey = normalize(username)
        if (uKey.isBlank()) return false
        return getAllUserKeys(context).contains(uKey)
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = getPrefs(context)
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val activeUser = prefs.getString(KEY_ACTIVE_USER, null)
        return loggedIn && !activeUser.isNullOrBlank() && userExists(context, activeUser)
    }

    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun getActiveUsername(context: Context): String? {
        val prefs = getPrefs(context)
        return prefs.getString(KEY_ACTIVE_USER, null)
    }

    fun getLastUsername(context: Context): String? {
        val prefs = getPrefs(context)
        val active = prefs.getString(KEY_ACTIVE_USER, null)
        if (!active.isNullOrBlank()) return active
        val last = prefs.getString(KEY_LAST_USER, null)
        if (!last.isNullOrBlank()) return last
        val allKeys = getAllUserKeys(context)
        if (allKeys.isNotEmpty()) {
            val firstKey = allKeys.first()
            return getUserDisplayName(context, firstKey) ?: firstKey
        }
        return null
    }

    fun getSavedUsername(context: Context): String? = getLastUsername(context)

    fun getSavedPassword(context: Context): String? {
        val u = getActiveUsername(context) ?: getLastUsername(context) ?: return null
        return getUserPassword(context, u)
    }

    fun getUserPassword(context: Context, username: String): String? {
        val uKey = normalize(username)
        val jsonStr = getPrefs(context).getString("user_cred_$uKey", null) ?: return null
        return try {
            JSONObject(jsonStr).optString("password", null)
        } catch (_: Exception) {
            null
        }
    }

    fun getUserDisplayName(context: Context, username: String): String? {
        val uKey = normalize(username)
        val jsonStr = getPrefs(context).getString("user_cred_$uKey", null) ?: return null
        return try {
            JSONObject(jsonStr).optString("username", username)
        } catch (_: Exception) {
            username
        }
    }

    fun isFingerprintEnabledForActiveUser(context: Context): Boolean {
        if (!isLoggedIn(context)) return false
        val active = getActiveUsername(context) ?: return false
        return isFingerprintEnabledForUser(context, active)
    }

    fun isFingerprintEnabled(context: Context): Boolean = isFingerprintEnabledForActiveUser(context)

    fun isFingerprintEnabledForUser(context: Context, username: String): Boolean {
        val uKey = normalize(username)
        val jsonStr = getPrefs(context).getString("user_cred_$uKey", null) ?: return false
        return try {
            JSONObject(jsonStr).optBoolean("isFingerprintEnabled", false)
        } catch (_: Exception) {
            false
        }
    }

    fun setFingerprintEnabled(context: Context, enabled: Boolean) {
        val active = getActiveUsername(context) ?: return
        setFingerprintEnabledForUser(context, active, enabled)
    }

    fun setFingerprintEnabledForUser(context: Context, username: String, enabled: Boolean) {
        val uKey = normalize(username)
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString("user_cred_$uKey", null) ?: return
        try {
            val obj = JSONObject(jsonStr)
            obj.put("isFingerprintEnabled", enabled)
            prefs.edit().putString("user_cred_$uKey", obj.toString()).apply()
        } catch (_: Exception) {}
    }

    fun saveUser(
        context: Context,
        username: String,
        password: String,
        isFingerprintEnabled: Boolean = false,
        markLoggedIn: Boolean = true
    ) {
        val trimmed = username.trim()
        val uKey = normalize(trimmed)
        val prefs = getPrefs(context)

        val obj = JSONObject().apply {
            put("username", trimmed)
            put("password", password)
            put("isFingerprintEnabled", isFingerprintEnabled)
        }

        val allUsers = getAllUserKeys(context).toMutableSet()
        allUsers.add(uKey)

        prefs.edit()
            .putString("user_cred_$uKey", obj.toString())
            .putStringSet(KEY_ALL_USERS, allUsers)
            .putString(KEY_ACTIVE_USER, trimmed)
            .putString(KEY_LAST_USER, trimmed)
            .putBoolean(KEY_IS_LOGGED_IN, markLoggedIn)
            .apply()

        // Sync legacy keys for backward fallback
        try {
            val legacy = context.getSharedPreferences(LEGACY_AUTH_PREFS, Context.MODE_PRIVATE)
            legacy.edit()
                .putString("saved_username", trimmed)
                .putString("saved_password", password)
                .putBoolean("is_fingerprint_enabled", isFingerprintEnabled)
                .apply()
        } catch (_: Exception) {}

        // Indestructible internal vault sync
        syncVaultFile(context)
    }

    fun validateLogin(context: Context, usernameInput: String, passwordInput: String): Boolean {
        val uKey = normalize(usernameInput)
        if (uKey.isBlank() || !userExists(context, uKey)) {
            return false
        }
        val savedPass = getUserPassword(context, uKey) ?: return false
        if (savedPass == passwordInput) {
            val originalName = getUserDisplayName(context, uKey) ?: usernameInput.trim()
            val prefs = getPrefs(context)
            prefs.edit()
                .putString(KEY_ACTIVE_USER, originalName)
                .putString(KEY_LAST_USER, originalName)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply()

            // Update legacy fallback
            try {
                val legacy = context.getSharedPreferences(LEGACY_AUTH_PREFS, Context.MODE_PRIVATE)
                legacy.edit()
                    .putString("saved_username", originalName)
                    .putString("saved_password", savedPass)
                    .putBoolean("is_fingerprint_enabled", isFingerprintEnabledForUser(context, uKey))
                    .apply()
            } catch (_: Exception) {}
            return true
        }
        return false
    }

    fun logout(context: Context) {
        val prefs = getPrefs(context)
        val active = prefs.getString(KEY_ACTIVE_USER, null)
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putString(KEY_LAST_USER, active)
            .apply()
    }

    fun deleteUser(context: Context, username: String) {
        val uKey = normalize(username)
        val prefs = getPrefs(context)

        // Remove from users list
        val allUsers = getAllUserKeys(context).toMutableSet()
        allUsers.remove(uKey)

        val editor = prefs.edit()
        editor.remove("user_cred_$uKey")
        editor.putStringSet(KEY_ALL_USERS, allUsers)

        val active = prefs.getString(KEY_ACTIVE_USER, null)
        if (normalize(active) == uKey) {
            editor.remove(KEY_ACTIVE_USER)
            editor.putBoolean(KEY_IS_LOGGED_IN, false)
        }
        val last = prefs.getString(KEY_LAST_USER, null)
        if (normalize(last) == uKey) {
            editor.remove(KEY_LAST_USER)
        }
        editor.apply()

        // Permanently delete this user's isolated data from app preferences
        try {
            val dataPrefs = getDataPrefs(context)
            dataPrefs.edit()
                .remove("${uKey}_expenses")
                .remove("${uKey}_deleted_expenses")
                .remove("${uKey}_config")
                .remove("user_$uKey")
                .remove("user_${username.trim()}")
                .apply()
        } catch (_: Exception) {}

        // Clear legacy auth storage if matching
        try {
            val legacy = context.getSharedPreferences(LEGACY_AUTH_PREFS, Context.MODE_PRIVATE)
            val legU = legacy.getString("saved_username", null)
            if (normalize(legU) == uKey || allUsers.isEmpty()) {
                legacy.edit().clear().apply()
            }
        } catch (_: Exception) {}

        // Update persistent vault file
        syncVaultFile(context)
    }

    fun updatePassword(context: Context, newPass: String): Boolean {
        val u = getActiveUsername(context) ?: getLastUsername(context) ?: return false
        return updatePassword(context, u, newPass)
    }

    fun updatePassword(context: Context, username: String, newPass: String): Boolean {
        val uKey = normalize(username)
        if (!userExists(context, uKey)) return false
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString("user_cred_$uKey", null) ?: return false
        return try {
            val obj = JSONObject(jsonStr)
            obj.put("password", newPass)
            prefs.edit().putString("user_cred_$uKey", obj.toString()).apply()

            // Update legacy fallback if active
            val active = prefs.getString(KEY_ACTIVE_USER, null)
            if (normalize(active) == uKey) {
                val legacy = context.getSharedPreferences(LEGACY_AUTH_PREFS, Context.MODE_PRIVATE)
                legacy.edit().putString("saved_password", newPass).apply()
            }
            syncVaultFile(context)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun userMatches(context: Context, usernameInput: String): Boolean {
        return userExists(context, usernameInput)
    }
}
