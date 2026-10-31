package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppConfig
import com.example.model.Expense
import com.example.model.FeedbackItem
import com.example.model.PLANT_NAMES
import com.example.model.UserProfile
import com.example.util.AuthStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class ExpenseRepository(context: Context) {
    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences =
        context.getSharedPreferences("myexpenses_prefs_v15", Context.MODE_PRIVATE)

    var activeUserId: String = ""

    private fun getExpenseKey(): String {
        return if (activeUserId.isNotBlank()) "${activeUserId}_expenses" else "myexp_v15"
    }

    private fun getDeletedKey(): String {
        return if (activeUserId.isNotBlank()) "${activeUserId}_deleted_expenses" else "myexp_del_v15"
    }

    private fun getConfigKey(suffix: String): String {
        return if (activeUserId.isNotBlank()) "${activeUserId}_$suffix" else suffix
    }

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _deletedExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val deletedExpenses: StateFlow<List<Expense>> = _deletedExpenses.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _appConfig = MutableStateFlow(AppConfig())
    val appConfig: StateFlow<AppConfig> = _appConfig.asStateFlow()

    private val _feedbacks = MutableStateFlow<List<FeedbackItem>>(emptyList())
    val feedbacks: StateFlow<List<FeedbackItem>> = _feedbacks.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        AuthStorage.ensureInitialized(appContext)
        val activeUser = AuthStorage.getActiveUsername(appContext) ?: prefs.getString("logged_in_username", null)
        if (!activeUser.isNullOrBlank()) {
            activeUserId = activeUser.trim().lowercase()
        }

        // Load AppConfig per user with global fallback
        val currKey = getConfigKey("cfg_currency")
        val budgetKey = getConfigKey("cfg_budget")
        val langKey = getConfigKey("cfg_lang")
        val sumTypeKey = getConfigKey("cfg_sum_type")
        val darkModeKey = getConfigKey("cfg_dark_mode")
        val biometricKey = getConfigKey("cfg_biometric_enabled")
        val lastBackupKey = getConfigKey("cfg_last_backup")
        val updateUrlKey = getConfigKey("cfg_update_url")
        val autoUpdateKey = getConfigKey("cfg_auto_update")
        val defaultUpdateUrl = "https://raw.githubusercontent.com/shivashandliya1457-ux/My-expenses-update-/main/version.json"

        val curr = prefs.getString(currKey, prefs.getString("cfg_currency", "₹")) ?: "₹"
        val budget = prefs.getFloat(budgetKey, prefs.getFloat("cfg_budget", 50000f)).toDouble()
        val lang = prefs.getString(langKey, prefs.getString("cfg_lang", "en")) ?: "en"
        val sumType = prefs.getString(sumTypeKey, prefs.getString("cfg_sum_type", "monthly")) ?: "monthly"
        val darkMode = prefs.getBoolean(darkModeKey, prefs.getBoolean("cfg_dark_mode", true))
        val biometric = if (activeUserId.isNotBlank()) {
            AuthStorage.isFingerprintEnabledForUser(appContext, activeUserId)
        } else {
            prefs.getBoolean("cfg_biometric_enabled", false)
        }
        val lastBackup = prefs.getLong(lastBackupKey, prefs.getLong("cfg_last_backup", 0L))
        val updateUrl = prefs.getString(updateUrlKey, prefs.getString("cfg_update_url", defaultUpdateUrl)) ?: defaultUpdateUrl
        val autoUpdate = prefs.getBoolean(autoUpdateKey, prefs.getBoolean("cfg_auto_update", true))

        _appConfig.value = AppConfig(
            currency = curr,
            monthlyBudget = budget,
            language = lang,
            summaryType = sumType,
            isDarkMode = darkMode,
            isBiometricEnabled = biometric,
            lastBackupTimestamp = lastBackup,
            updateServerUrl = updateUrl,
            autoCheckUpdates = autoUpdate
        )

        // Load Current User
        if (activeUserId.isNotBlank()) {
            val userJsonStr = prefs.getString("user_$activeUserId", null)
            if (userJsonStr != null) {
                _currentUser.value = parseUserProfile(userJsonStr)
            } else {
                val displayName = AuthStorage.getUserDisplayName(appContext, activeUserId) ?: activeUserId
                _currentUser.value = UserProfile(
                    username = displayName,
                    password = AuthStorage.getUserPassword(appContext, activeUserId) ?: "",
                    isBiometricEnabled = biometric
                )
            }
        } else {
            _currentUser.value = null
        }

        // Load Expenses per user (strictly isolated with internal vault file fallback)
        val expKey = getExpenseKey()
        val expFile = if (activeUserId.isNotBlank()) java.io.File(appContext.filesDir, "user_exp_${activeUserId}.json") else null
        val expJson = if (prefs.contains(expKey)) {
            val fromPrefs = prefs.getString(expKey, "[]") ?: "[]"
            if (fromPrefs == "[]" && expFile != null && expFile.exists() && expFile.length() > 2) {
                try {
                    val fromFile = expFile.readText()
                    prefs.edit().putString(expKey, fromFile).apply()
                    fromFile
                } catch (_: Exception) { fromPrefs }
            } else {
                fromPrefs
            }
        } else if (expFile != null && expFile.exists() && expFile.length() > 2) {
            try {
                val fromFile = expFile.readText()
                prefs.edit().putString(expKey, fromFile).apply()
                fromFile
            } catch (_: Exception) { "[]" }
        } else if (activeUserId.isNotBlank() && prefs.contains("myexp_v15")) {
            // First time migrating legacy single-user data to this user
            val legacy = prefs.getString("myexp_v15", "[]") ?: "[]"
            prefs.edit().putString(expKey, legacy).apply()
            legacy
        } else {
            "[]"
        }
        _expenses.value = parseExpenses(expJson)

        // Load Deleted with 30-day auto-purge filter per user
        val delKey = getDeletedKey()
        val delFile = if (activeUserId.isNotBlank()) java.io.File(appContext.filesDir, "user_del_${activeUserId}.json") else null
        val delJson = if (prefs.contains(delKey)) {
            val fromPrefs = prefs.getString(delKey, "[]") ?: "[]"
            if (fromPrefs == "[]" && delFile != null && delFile.exists() && delFile.length() > 2) {
                try {
                    val fromFile = delFile.readText()
                    prefs.edit().putString(delKey, fromFile).apply()
                    fromFile
                } catch (_: Exception) { fromPrefs }
            } else {
                fromPrefs
            }
        } else if (delFile != null && delFile.exists() && delFile.length() > 2) {
            try {
                val fromFile = delFile.readText()
                prefs.edit().putString(delKey, fromFile).apply()
                fromFile
            } catch (_: Exception) { "[]" }
        } else if (activeUserId.isNotBlank() && prefs.contains("myexp_del_v15")) {
            val legacyDel = prefs.getString("myexp_del_v15", "[]") ?: "[]"
            prefs.edit().putString(delKey, legacyDel).apply()
            legacyDel
        } else {
            "[]"
        }
        val parsedDel = parseExpenses(delJson)
        val now = System.currentTimeMillis()
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000L
        val unexpiredDeleted = parsedDel.filter { item ->
            val delTime = item.deletedAt ?: item.timestamp
            (now - delTime) < thirtyDaysMs
        }
        _deletedExpenses.value = unexpiredDeleted
        if (unexpiredDeleted.size != parsedDel.size) {
            saveDeleted()
        }

        // Load Feedbacks
        val fbJson = prefs.getString("app_feedbacks", null)
        if (fbJson != null) {
            _feedbacks.value = parseFeedbacks(fbJson)
        } else {
            // Seed initial plant-named community reviews
            val initial = listOf(
                FeedbackItem(
                    id = 1,
                    plantName = "Tulsi 🌿",
                    rating = 5,
                    comment = "The Udhar ledger automatic balance calculation is super useful for tracking debts with friends!",
                    timestamp = System.currentTimeMillis() - 86400000L * 3
                ),
                FeedbackItem(
                    id = 2,
                    plantName = "Banyan 🍃",
                    rating = 5,
                    comment = "Love the Indian Army/Navy photo backgrounds in the monthly calendar! Very fast & smooth offline app.",
                    timestamp = System.currentTimeMillis() - 86400000L * 6
                ),
                FeedbackItem(
                    id = 3,
                    plantName = "Neem 🌱",
                    rating = 5,
                    comment = "Full screen options and real fingerprint lock make it very secure and private. 10/10.",
                    timestamp = System.currentTimeMillis() - 86400000L * 10
                )
            )
            _feedbacks.value = initial
            saveFeedbacks(initial)
        }
    }

    fun addOrUpdateExpense(
        amount: Double,
        category: String,
        note: String,
        paymentMethod: String,
        dateIso: String,
        timestamp: Long,
        person: String,
        given: Double,
        returned: Double
    ) {
        val currentList = _expenses.value.toMutableList()
        val netRemaining = if (category == "Udhar") given - returned else 0.0

        if (category == "Udhar" && person.isNotBlank()) {
            val existingIndex = currentList.indexOfFirst {
                it.category == "Udhar" && it.person.trim().equals(person.trim(), ignoreCase = true)
            }
            if (existingIndex != -1) {
                val existing = currentList[existingIndex]
                val newGiven = existing.given + given
                val newReturned = existing.returned + returned
                val newRemaining = newGiven - newReturned
                val updated = existing.copy(
                    given = newGiven,
                    returned = newReturned,
                    remaining = newRemaining,
                    amount = newRemaining,
                    dateIso = dateIso,
                    timestamp = timestamp,
                    paymentMethod = paymentMethod,
                    note = if (note.isNotBlank()) "${existing.note} | $note".trim() else existing.note
                )
                currentList[existingIndex] = updated
                _expenses.value = currentList
                saveExpenses()
                return
            }
        }

        val newExpense = Expense(
            id = System.currentTimeMillis(),
            amount = if (category == "Udhar") netRemaining else amount,
            category = category,
            note = note,
            paymentMethod = paymentMethod,
            dateIso = dateIso,
            timestamp = timestamp,
            person = person,
            given = given,
            returned = returned,
            remaining = netRemaining
        )
        currentList.add(0, newExpense)
        _expenses.value = currentList
        saveExpenses()
    }

    fun updateExpense(updated: Expense) {
        val currentList = _expenses.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            val netRemaining = if (updated.category.equals("Udhar", ignoreCase = true)) {
                updated.given - updated.returned
            } else {
                0.0
            }
            val finalExpense = updated.copy(
                amount = if (updated.category.equals("Udhar", ignoreCase = true)) netRemaining else updated.amount,
                remaining = netRemaining
            )
            currentList[index] = finalExpense
            _expenses.value = currentList
            saveExpenses()
        }
    }

    fun deleteExpense(expense: Expense) {
        val active = _expenses.value.toMutableList()
        active.removeIf { it.id == expense.id }
        _expenses.value = active
        saveExpenses()

        val deleted = _deletedExpenses.value.toMutableList()
        val item = expense.copy(isDeleted = true, deletedAt = System.currentTimeMillis())
        deleted.add(0, item)
        _deletedExpenses.value = deleted
        saveDeleted()
    }

    fun restoreExpense(expense: Expense) {
        val deleted = _deletedExpenses.value.toMutableList()
        deleted.removeIf { it.id == expense.id }
        _deletedExpenses.value = deleted
        saveDeleted()

        val active = _expenses.value.toMutableList()
        active.add(0, expense.copy(isDeleted = false, deletedAt = null))
        _expenses.value = active
        saveExpenses()
    }

    fun permanentlyDeleteExpense(expense: Expense) {
        val deleted = _deletedExpenses.value.toMutableList()
        deleted.removeIf { it.id == expense.id }
        _deletedExpenses.value = deleted
        saveDeleted()
    }

    fun emptyTrash() {
        _deletedExpenses.value = emptyList()
        saveDeleted()
    }

    fun updateConfig(config: AppConfig) {
        _appConfig.value = config
        val currKey = getConfigKey("cfg_currency")
        val budgetKey = getConfigKey("cfg_budget")
        val langKey = getConfigKey("cfg_lang")
        val sumTypeKey = getConfigKey("cfg_sum_type")
        val darkModeKey = getConfigKey("cfg_dark_mode")
        val biometricKey = getConfigKey("cfg_biometric_enabled")
        val lastBackupKey = getConfigKey("cfg_last_backup")
        val updateUrlKey = getConfigKey("cfg_update_url")
        val autoUpdateKey = getConfigKey("cfg_auto_update")

        prefs.edit()
            .putString(currKey, config.currency)
            .putFloat(budgetKey, config.monthlyBudget.toFloat())
            .putString(langKey, config.language)
            .putString(sumTypeKey, config.summaryType)
            .putBoolean(darkModeKey, config.isDarkMode)
            .putBoolean(biometricKey, config.isBiometricEnabled)
            .putLong(lastBackupKey, config.lastBackupTimestamp)
            .putString(updateUrlKey, config.updateServerUrl)
            .putBoolean(autoUpdateKey, config.autoCheckUpdates)
            .putString("cfg_currency", config.currency)
            .putFloat("cfg_budget", config.monthlyBudget.toFloat())
            .putString("cfg_lang", config.language)
            .putString("cfg_sum_type", config.summaryType)
            .putBoolean("cfg_dark_mode", config.isDarkMode)
            .putBoolean("cfg_biometric_enabled", config.isBiometricEnabled)
            .putLong("cfg_last_backup", config.lastBackupTimestamp)
            .putString("cfg_update_url", config.updateServerUrl)
            .putBoolean("cfg_auto_update", config.autoCheckUpdates)
            .apply()

        if (activeUserId.isNotBlank()) {
            AuthStorage.setFingerprintEnabledForUser(appContext, activeUserId, config.isBiometricEnabled)
        }
    }

    fun createBackup(): Long {
        val now = System.currentTimeMillis()
        val expArr = JSONArray()
        for (e in _expenses.value) expArr.put(serializeExpense(e))

        val delArr = JSONArray()
        for (d in _deletedExpenses.value) delArr.put(serializeExpense(d))

        val backupObj = JSONObject()
        backupObj.put("timestamp", now)
        backupObj.put("expenses", expArr)
        backupObj.put("deleted", delArr)

        prefs.edit()
            .putString("backup_snapshot_v15", backupObj.toString())
            .putLong("cfg_last_backup", now)
            .apply()

        updateConfig(_appConfig.value.copy(lastBackupTimestamp = now))
        return now
    }

    fun restoreFromBackup(): Boolean {
        val snapshotStr = prefs.getString("backup_snapshot_v15", null) ?: return false
        return try {
            val obj = JSONObject(snapshotStr)
            val expArr = obj.getJSONArray("expenses")
            val delArr = obj.getJSONArray("deleted")

            val restoredExpenses = parseExpenses(expArr.toString())
            val restoredDeleted = parseExpenses(delArr.toString())

            _expenses.value = restoredExpenses
            _deletedExpenses.value = restoredDeleted
            saveExpenses()
            saveDeleted()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun exportDataJson(): String {
        val exportObj = JSONObject()
        exportObj.put("app", "My Expenses Report")
        exportObj.put("version", "15.0")
        exportObj.put("exportedAt", System.currentTimeMillis())

        val expArr = JSONArray()
        for (e in _expenses.value) expArr.put(serializeExpense(e))
        exportObj.put("expenses", expArr)

        val cfgObj = JSONObject()
        cfgObj.put("currency", _appConfig.value.currency)
        cfgObj.put("monthlyBudget", _appConfig.value.monthlyBudget)
        exportObj.put("config", cfgObj)

        return exportObj.toString(2)
    }

    fun importDataJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            val expArr = if (root.has("expenses")) root.getJSONArray("expenses") else JSONArray(jsonStr)
            val imported = parseExpenses(expArr.toString())
            if (imported.isNotEmpty()) {
                val current = _expenses.value.toMutableList()
                val existingIds = current.map { it.id }.toSet()
                for (item in imported) {
                    if (!existingIds.contains(item.id)) {
                        current.add(item)
                    }
                }
                _expenses.value = current
                saveExpenses()
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }

    fun changePassword(oldPass: String, newPass: String): Pair<Boolean, String> {
        val user = _currentUser.value ?: return Pair(false, "No user logged in")
        if (user.password != oldPass) {
            return Pair(false, "Current password is incorrect")
        }
        val updated = user.copy(password = newPass)
        registerUser(updated)
        AuthStorage.updatePassword(appContext, user.username, newPass)
        return Pair(true, "Password updated successfully")
    }

    fun changeMobile(newMobile: String, countryCode: String): Boolean {
        val user = _currentUser.value ?: return false
        val updated = user.copy(mobile = newMobile, countryCode = countryCode, isVerified = true)
        registerUser(updated)
        return true
    }

    fun clearAllData() {
        _expenses.value = emptyList()
        _deletedExpenses.value = emptyList()
        saveExpenses()
        saveDeleted()
    }

    fun deleteAccount() {
        val user = _currentUser.value
        val usernameToDelete = user?.username ?: activeUserId
        if (usernameToDelete.isNotBlank()) {
            val uKey = usernameToDelete.trim().lowercase()
            prefs.edit()
                .remove("${uKey}_expenses")
                .remove("${uKey}_deleted_expenses")
                .remove("${uKey}_cfg_currency")
                .remove("${uKey}_cfg_budget")
                .remove("${uKey}_cfg_lang")
                .remove("${uKey}_cfg_sum_type")
                .remove("${uKey}_cfg_dark_mode")
                .remove("${uKey}_cfg_biometric_enabled")
                .remove("${uKey}_cfg_last_backup")
                .remove("user_$uKey")
                .remove("user_$usernameToDelete")
                .remove("logged_in_username")
                .apply()
            try {
                val fExp = java.io.File(appContext.filesDir, "user_exp_${uKey}.json")
                if (fExp.exists()) fExp.delete()
                val fDel = java.io.File(appContext.filesDir, "user_del_${uKey}.json")
                if (fDel.exists()) fDel.delete()
            } catch (_: Exception) {}
            AuthStorage.deleteUser(appContext, usernameToDelete)
        }
        _currentUser.value = null
        _expenses.value = emptyList()
        _deletedExpenses.value = emptyList()
        activeUserId = ""
    }

    fun addFeedback(rating: Int, comment: String): FeedbackItem {
        val randomPlant = PLANT_NAMES.random()
        val item = FeedbackItem(
            id = System.currentTimeMillis(),
            plantName = randomPlant,
            rating = rating,
            comment = comment.trim(),
            timestamp = System.currentTimeMillis()
        )
        val list = _feedbacks.value.toMutableList()
        list.add(0, item)
        _feedbacks.value = list
        saveFeedbacks(list)
        return item
    }

    fun registerUser(user: UserProfile) {
        val trimmed = user.username.trim()
        val uKey = trimmed.lowercase()
        activeUserId = uKey

        // Ensure new user gets 100% clean, empty isolated data
        val expKey = "${uKey}_expenses"
        if (!prefs.contains(expKey)) {
            prefs.edit().putString(expKey, "[]").apply()
        }
        val delKey = "${uKey}_deleted_expenses"
        if (!prefs.contains(delKey)) {
            prefs.edit().putString(delKey, "[]").apply()
        }

        prefs.edit()
            .putString("user_$uKey", serializeUserProfile(user))
            .putString("logged_in_username", trimmed)
            .apply()

        _currentUser.value = user
        loadData()
    }

    fun switchUser(username: String) {
        val trimmed = username.trim()
        activeUserId = trimmed.lowercase()
        prefs.edit().putString("logged_in_username", trimmed).apply()
        loadData()
    }

    fun getUser(username: String): UserProfile? {
        val uKey = username.trim().lowercase()
        val str = prefs.getString("user_$uKey", null) ?: prefs.getString("user_$username", null) ?: return null
        return parseUserProfile(str)
    }

    fun getAnyRegisteredUser(): UserProfile? {
        val all = prefs.all
        for ((key, value) in all) {
            if (key.startsWith("user_") && value is String) {
                return parseUserProfile(value)
            }
        }
        return null
    }

    fun loginUser(user: UserProfile) {
        val trimmed = user.username.trim()
        activeUserId = trimmed.lowercase()
        prefs.edit()
            .putString("logged_in_username", trimmed)
            .putString("user_$activeUserId", serializeUserProfile(user))
            .apply()
        _currentUser.value = user
        loadData()
    }

    fun logout() {
        AuthStorage.logout(appContext)
        prefs.edit().remove("logged_in_username").apply()
        _currentUser.value = null
        _expenses.value = emptyList()
        _deletedExpenses.value = emptyList()
        activeUserId = ""
    }

    private fun saveExpenses() {
        val array = JSONArray()
        for (e in _expenses.value) {
            array.put(serializeExpense(e))
        }
        val key = getExpenseKey()
        val jsonStr = array.toString()
        prefs.edit().putString(key, jsonStr).apply()

        if (activeUserId.isNotBlank()) {
            try {
                val f = java.io.File(appContext.filesDir, "user_exp_${activeUserId}.json")
                f.writeText(jsonStr)
            } catch (_: Exception) {}
        }
    }

    private fun saveDeleted() {
        val array = JSONArray()
        for (e in _deletedExpenses.value) {
            array.put(serializeExpense(e))
        }
        val key = getDeletedKey()
        val jsonStr = array.toString()
        prefs.edit().putString(key, jsonStr).apply()

        if (activeUserId.isNotBlank()) {
            try {
                val f = java.io.File(appContext.filesDir, "user_del_${activeUserId}.json")
                f.writeText(jsonStr)
            } catch (_: Exception) {}
        }
    }

    private fun saveFeedbacks(list: List<FeedbackItem>) {
        val array = JSONArray()
        for (f in list) {
            val obj = JSONObject()
            obj.put("id", f.id)
            obj.put("plantName", f.plantName)
            obj.put("rating", f.rating)
            obj.put("comment", f.comment)
            obj.put("timestamp", f.timestamp)
            array.put(obj)
        }
        prefs.edit().putString("app_feedbacks", array.toString()).apply()
    }

    private fun parseFeedbacks(jsonStr: String): List<FeedbackItem> {
        val list = mutableListOf<FeedbackItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FeedbackItem(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        plantName = obj.optString("plantName", "Tulsi 🌿"),
                        rating = obj.optInt("rating", 5),
                        comment = obj.optString("comment", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun serializeExpense(e: Expense): JSONObject {
        val obj = JSONObject()
        obj.put("id", e.id)
        obj.put("amount", e.amount)
        obj.put("category", e.category)
        obj.put("note", e.note)
        obj.put("paymentMethod", e.paymentMethod)
        obj.put("dateIso", e.dateIso)
        obj.put("timestamp", e.timestamp)
        obj.put("person", e.person)
        obj.put("given", e.given)
        obj.put("returned", e.returned)
        obj.put("remaining", e.remaining)
        obj.put("isDeleted", e.isDeleted)
        obj.put("deletedAt", e.deletedAt ?: 0L)
        return obj
    }

    private fun parseExpenses(jsonStr: String): List<Expense> {
        val list = mutableListOf<Expense>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Expense(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        amount = obj.optDouble("amount", 0.0),
                        category = obj.optString("category", "Food"),
                        note = obj.optString("note", ""),
                        paymentMethod = obj.optString("paymentMethod", "Cash"),
                        dateIso = obj.optString("dateIso", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        person = obj.optString("person", ""),
                        given = obj.optDouble("given", 0.0),
                        returned = obj.optDouble("returned", 0.0),
                        remaining = obj.optDouble("remaining", 0.0),
                        isDeleted = obj.optBoolean("isDeleted", false),
                        deletedAt = if (obj.has("deletedAt")) obj.optLong("deletedAt") else null
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun serializeUserProfile(u: UserProfile): String {
        val obj = JSONObject()
        obj.put("username", u.username)
        obj.put("password", u.password)
        obj.put("securityQuestion", u.securityQuestion)
        obj.put("securityAnswer", u.securityAnswer)
        obj.put("mobile", u.mobile)
        obj.put("countryCode", u.countryCode)
        obj.put("isVerified", u.isVerified)
        obj.put("isBiometricEnabled", u.isBiometricEnabled)
        return obj.toString()
    }

    private fun parseUserProfile(str: String): UserProfile {
        val obj = JSONObject(str)
        return UserProfile(
            username = obj.optString("username", ""),
            password = obj.optString("password", ""),
            securityQuestion = obj.optString("securityQuestion", "First School Name?"),
            securityAnswer = obj.optString("securityAnswer", ""),
            mobile = obj.optString("mobile", ""),
            countryCode = obj.optString("countryCode", "+91"),
            isVerified = obj.optBoolean("isVerified", false),
            isBiometricEnabled = obj.optBoolean("isBiometricEnabled", false)
        )
    }
}
