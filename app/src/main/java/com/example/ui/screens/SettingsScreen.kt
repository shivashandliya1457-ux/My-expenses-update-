package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.Star
import com.example.util.UpdateManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseRepository
import com.example.model.AppConfig
import com.example.model.Expense
import com.example.model.FeedbackItem
import com.example.model.Translations
import com.example.model.WORLD_CURRENCIES
import com.example.ui.components.AppLogo
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Note100Yellow
import com.example.ui.theme.Note200Orange
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.RedDanger
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    repository: ExpenseRepository,
    config: AppConfig,
    deletedExpenses: List<Expense>,
    expenses: List<Expense>,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onTriggerBiometric: (onSuccess: () -> Unit) -> Unit,
    onCheckUpdate: () -> Unit = {}
) {
    val context = LocalContext.current
    var subScreen by remember { mutableStateOf<String?>(null) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val feedbacks by repository.feedbacks.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    // Handle back button for sub-screens
    BackHandler(enabled = subScreen != null) {
        subScreen = null
    }

    // Top-Level Router for Full Screen Sub-Views
    when (subScreen) {
        "currency" -> CurrencyPickerScreen(
            currentCurrency = config.currency,
            onCurrencySelected = { newCurr ->
                repository.updateConfig(config.copy(currency = newCurr))
                subScreen = null
                Toast.makeText(context, "Currency updated to $newCurr", Toast.LENGTH_SHORT).show()
            },
            onBack = { subScreen = null }
        )

        "budget" -> MonthlyBudgetScreen(
            currentBudget = config.monthlyBudget,
            currency = config.currency,
            onBudgetSaved = { newBudget ->
                repository.updateConfig(config.copy(monthlyBudget = newBudget))
                subScreen = null
                Toast.makeText(context, "Budget updated: ${config.currency}${"%,.0f".format(newBudget)}", Toast.LENGTH_SHORT).show()
            },
            onBack = { subScreen = null }
        )

        "change_password" -> ChangePasswordScreen(
            repository = repository,
            onBack = { subScreen = null }
        )

        "change_mobile" -> ChangeMobileScreen(
            repository = repository,
            currentUser = currentUser,
            onBack = { subScreen = null }
        )

        "export_import" -> ExportImportScreen(
            repository = repository,
            onBack = { subScreen = null }
        )

        "backup" -> BackupScreen(
            repository = repository,
            lastBackupTimestamp = config.lastBackupTimestamp,
            onBack = { subScreen = null }
        )

        "help_guide" -> HelpGuideScreen(
            initialLanguage = config.language,
            onBack = { subScreen = null }
        )

        "feedback" -> FeedbackScreen(
            feedbacks = feedbacks,
            onAddFeedback = { rating, comment ->
                repository.addFeedback(rating, comment)
                Toast.makeText(context, "Feedback submitted anonymously!", Toast.LENGTH_SHORT).show()
            },
            onBack = { subScreen = null }
        )

        "contact_us" -> ContactUsScreen(
            onBack = { subScreen = null }
        )

        "delete_expenses" -> DeleteExpensesScreen(
            repository = repository,
            deletedExpenses = deletedExpenses,
            currency = config.currency,
            onBack = { subScreen = null }
        )

        "trust_center" -> TrustCenterScreen(
            expenses = expenses,
            deletedExpenses = deletedExpenses,
            isBiometricEnabled = config.isBiometricEnabled,
            onBack = { subScreen = null }
        )

        else -> {
            // Main Settings Menu
            val colors = AppTheme.colors
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .testTag("settings_screen")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Header with Back Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = Translations.get("settings", config.language),
                            color = colors.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 1: Preferences & App Style
                    SectionHeader("Preferences")

                    // Dark / Light Mode Selector (Allows enabling both Dark and Light modes)
                    DarkLightModeSelector(
                        isDarkMode = config.isDarkMode,
                        language = config.language,
                        onModeChange = { isDark ->
                            repository.updateConfig(config.copy(isDarkMode = isDark))
                            Toast.makeText(
                                context,
                                if (isDark)
                                    (if (config.language == "hi") "डार्क मोड सक्रिय किया गया" else "Dark Mode enabled")
                                else
                                    (if (config.language == "hi") "लाइट मोड सक्रिय किया गया" else "Light Mode enabled"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    // Choose Currency
                    SettingsNavRow(
                        icon = Icons.Default.CurrencyExchange,
                        iconTint = Note100Yellow,
                        title = "Choose Currency",
                        subtitle = "Current: ${config.currency} (All world currencies available)",
                        onClick = { subScreen = "currency" }
                    )

                    // Monthly Budget
                    SettingsNavRow(
                        icon = Icons.Default.Wallet,
                        iconTint = EmeraldSuccess,
                        title = "Monthly Budget",
                        subtitle = "Current limit: ${config.currency}${"%,.0f".format(config.monthlyBudget)}",
                        onClick = { subScreen = "budget" }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 2: Account & Security
                    SectionHeader("Account & Security")

                    // Biometric Unlock
                    SettingsToggleItem(
                        icon = Icons.Default.Fingerprint,
                        iconTint = PurpleAccent,
                        title = "Biometric Unlock",
                        subtitle = if (config.isBiometricEnabled) "Fingerprint lock active" else "Tap to sync device fingerprint",
                        checked = config.isBiometricEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onTriggerBiometric {
                                    repository.updateConfig(config.copy(isBiometricEnabled = true))
                                    Toast.makeText(context, "Fingerprint lock enabled!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                repository.updateConfig(config.copy(isBiometricEnabled = false))
                                Toast.makeText(context, "Biometric unlock disabled", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    // Change Password
                    SettingsNavRow(
                        icon = Icons.Default.Lock,
                        iconTint = BlueAccent,
                        title = "Change Password",
                        subtitle = "Update your account login password",
                        onClick = { subScreen = "change_password" }
                    )

                    // Change Mobile Number
                    SettingsNavRow(
                        icon = Icons.Default.Phone,
                        iconTint = Note200Orange,
                        title = "Change Mobile Number",
                        subtitle = currentUser?.mobile?.ifBlank { "Not set" } ?: "Not set",
                        onClick = { subScreen = "change_mobile" }
                    )

                    // Trust & Security Center
                    SettingsNavRow(
                        icon = Icons.Default.Shield,
                        iconTint = EmeraldSuccess,
                        title = "Trust & Security Center",
                        subtitle = "Offline encryption, zero data collection audit & pledge",
                        onClick = { subScreen = "trust_center" }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 3: Data, Backup & Recycle Bin
                    SectionHeader("Data & Storage")

                    // Export / Import Data
                    SettingsNavRow(
                        icon = Icons.Default.ImportExport,
                        iconTint = BlueAccent,
                        title = "Export Data / Import Data",
                        subtitle = "Export or restore JSON data files",
                        onClick = { subScreen = "export_import" }
                    )

                    // Backup
                    SettingsNavRow(
                        icon = Icons.Default.Backup,
                        iconTint = EmeraldSuccess,
                        title = "Backup",
                        subtitle = if (config.lastBackupTimestamp > 0)
                            "Last: ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(config.lastBackupTimestamp))}"
                        else "No snapshot created yet",
                        onClick = { subScreen = "backup" }
                    )

                    // Delete Expenses (Recycle Bin / Trash)
                    SettingsNavRow(
                        icon = Icons.Default.DeleteSweep,
                        iconTint = RedDanger,
                        title = "Delete Expenses",
                        subtitle = "${deletedExpenses.size} items in bin (Auto-deleted after 30 days)",
                        badge = if (deletedExpenses.isNotEmpty()) "${deletedExpenses.size}" else null,
                        onClick = { subScreen = "delete_expenses" }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 4: Community, Support & About
                    SectionHeader("Community & Help")

                    // Share With Friends
                    SettingsNavRow(
                        icon = Icons.Default.Share,
                        iconTint = BlueAccent,
                        title = "Share with Friends",
                        subtitle = "Share direct app download link",
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "📊 My Expenses Report - Manage personal finances, monthly budgets, Udhar debts, and photo calendar privately & 100% offline.\n\n⬇️ Direct APK Download:\nhttps://raw.githubusercontent.com/shivashandliya1457-ux/version.json/main/My-Expenses-Report.apk"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share App"))
                        }
                    )

                    // Help Guide (User Manual [Hindi / English])
                    SettingsNavRow(
                        icon = Icons.Default.Help,
                        iconTint = Note100Yellow,
                        title = "Help Guide",
                        subtitle = "User manual in Hindi & English",
                        onClick = { subScreen = "help_guide" }
                    )

                    // Send Feedback
                    SettingsNavRow(
                        icon = Icons.Default.Feedback,
                        iconTint = PurpleAccent,
                        title = "Send Feedback",
                        subtitle = "Anonymous community reviews with plant names",
                        onClick = { subScreen = "feedback" }
                    )

                    // Contact Us
                    SettingsNavRow(
                        icon = Icons.Default.Email,
                        iconTint = BlueAccent,
                        title = "Contact Us",
                        subtitle = "yahimilegaadda@gmail.com",
                        onClick = { subScreen = "contact_us" }
                    )

                    // Check for Updates (OTA In-App Update)
                    SettingsNavRow(
                        icon = Icons.Default.SystemUpdate,
                        iconTint = EmeraldSuccess,
                        title = "Check for Updates",
                        subtitle = "Current: v${UpdateManager.getEffectiveVersionName(context)} • Tap to check for updates",
                        onClick = onCheckUpdate
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 5: Danger Zone
                    SectionHeader("Danger Zone")

                    // Clear All Data
                    SettingsNavRow(
                        icon = Icons.Default.Delete,
                        iconTint = RedDanger,
                        title = "Clear All Data",
                        subtitle = "Delete all active expenses & reset to 0",
                        onClick = { showClearDataDialog = true }
                    )

                    // Delete Account
                    SettingsNavRow(
                        icon = Icons.Default.DeleteForever,
                        iconTint = RedDanger,
                        title = "Delete Account",
                        subtitle = "Permanently remove your account and all data",
                        onClick = { showDeleteAccountDialog = true }
                    )

                    // Logout
                    SettingsNavRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        iconTint = RedDanger,
                        title = "Logout",
                        subtitle = "Signed in as ${currentUser?.username ?: "User"}",
                        onClick = onLogout
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // App Branding Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate800)
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppLogo(size = 40.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "My Expenses Report v15.0",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Designed by Shiva Sharma",
                                    color = BlueAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Private & Secure Financial Manager",
                                    color = Slate500,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }

                // Confirmation Dialogs
                if (showClearDataDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearDataDialog = false },
                        containerColor = Slate800,
                        title = { Text("Clear All Data?", color = Color.White, fontWeight = FontWeight.Bold) },
                        text = { Text("All your current expenses and trash will be deleted and reset to 0. This cannot be undone.", color = Slate300) },
                        confirmButton = {
                            TextButton(onClick = {
                                repository.clearAllData()
                                showClearDataDialog = false
                                Toast.makeText(context, "All data cleared", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Clear Everything", color = RedDanger, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearDataDialog = false }) {
                                Text("Cancel", color = Slate400)
                            }
                        }
                    )
                }

                if (showDeleteAccountDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteAccountDialog = false },
                        containerColor = Slate800,
                        title = { Text("Delete Account?", color = Color.White, fontWeight = FontWeight.Bold) },
                        text = { Text("Your user profile, passwords, and all expenses will be permanently deleted.", color = Slate300) },
                        confirmButton = {
                            TextButton(onClick = {
                                repository.deleteAccount()
                                showDeleteAccountDialog = false
                                onLogout()
                                Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Delete Account", color = RedDanger, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteAccountDialog = false }) {
                                Text("Cancel", color = Slate400)
                            }
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// FULL SCREEN SUB-COMPONENTS
// ==========================================

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = BlueAccent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun DarkLightModeSelector(
    isDarkMode: Boolean,
    language: String,
    onModeChange: (Boolean) -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("dark_light_mode_selector")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = "Dark / Light Mode",
                        tint = if (isDarkMode) PurpleAccent else Note200Orange,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Translations.get("dark_light_mode", language),
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isDarkMode)
                            (if (language == "hi") "डार्क मोड सक्रिय • लाइट मोड चुनें" else "Dark mode active • Select to switch")
                        else
                            (if (language == "hi") "लाइट मोड सक्रिय • डार्क मोड चुनें" else "Light mode active • Select to switch"),
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onModeChange(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PurpleAccent,
                        uncheckedThumbColor = Note200Orange,
                        uncheckedTrackColor = colors.surfaceSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Two interactive buttons to enable both modes directly
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dark Mode Option Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDarkMode) PurpleAccent.copy(alpha = 0.2f) else colors.surfaceSecondary)
                        .border(
                            1.5.dp,
                            if (isDarkMode) PurpleAccent else colors.border,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onModeChange(true) }
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                        .testTag("btn_mode_dark"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = "Dark Mode",
                            tint = if (isDarkMode) PurpleAccent else colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Translations.get("dark_mode", language),
                            color = if (isDarkMode) colors.textPrimary else colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isDarkMode) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                // Light Mode Option Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isDarkMode) Note200Orange.copy(alpha = 0.15f) else colors.surfaceSecondary)
                        .border(
                            1.5.dp,
                            if (!isDarkMode) Note200Orange else colors.border,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onModeChange(false) }
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                        .testTag("btn_mode_light"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = "Light Mode",
                            tint = if (!isDarkMode) Note200Orange else colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Translations.get("light_mode", language),
                            color = if (!isDarkMode) colors.textPrimary else colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (!isDarkMode) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(RedDanger)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(text = badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BlueAccent,
                    uncheckedThumbColor = colors.textMuted,
                    uncheckedTrackColor = colors.surfaceSecondary
                )
            )
        }
    }
}

// -------------------------------------------------------------
// 1. Choose Currency Full Screen
// -------------------------------------------------------------
@Composable
fun CurrencyPickerScreen(
    currentCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = WORLD_CURRENCIES.filter {
        searchQuery.isBlank() ||
                it.code.contains(searchQuery, ignoreCase = true) ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.country.contains(searchQuery, ignoreCase = true) ||
                it.symbol.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FullScreenHeader(title = "Choose Currency", onBack = onBack)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search currency, code, country...", color = Slate400, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueAccent,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.code }) { item ->
                    val isSelected = currentCurrency == item.symbol || currentCurrency == item.code
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) BlueAccent.copy(alpha = 0.2f) else Slate800)
                            .border(1.dp, if (isSelected) BlueAccent else Slate700, RoundedCornerShape(12.dp))
                            .clickable { onCurrencySelected(item.symbol) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) BlueAccent else Slate900),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.symbol,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${item.code} - ${item.name}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = item.country,
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = BlueAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Monthly Budget Full Screen
// -------------------------------------------------------------
@Composable
fun MonthlyBudgetScreen(
    currentBudget: Double,
    currency: String,
    onBudgetSaved: (Double) -> Unit,
    onBack: () -> Unit
) {
    var budgetText by remember { mutableStateOf(currentBudget.toInt().toString()) }
    val presets = listOf(10000.0, 25000.0, 50000.0, 75000.0, 100000.0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FullScreenHeader(title = "Monthly Budget", onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            // Current Limit Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "Current Monthly Limit", color = Slate400, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currency${"%,.0f".format(currentBudget)}",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Quick Presets", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.take(3).forEach { p ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate800)
                            .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                            .clickable { budgetText = p.toInt().toString() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "$currency${"%,.0f".format(p)}", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Set Custom Limit ($currency)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueAccent,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val b = budgetText.toDoubleOrNull() ?: 50000.0
                    onBudgetSaved(b)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save Budget", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Change Password Full Screen
// -------------------------------------------------------------
@Composable
fun ChangePasswordScreen(
    repository: ExpenseRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FullScreenHeader(title = "Change Password", onBack = onBack)

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = oldPass,
                onValueChange = { oldPass = it },
                label = { Text("Current Password", color = Slate400) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueAccent,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                label = { Text("New Password", color = Slate400) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueAccent,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPass,
                onValueChange = { confirmPass = it },
                label = { Text("Confirm New Password", color = Slate400) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueAccent,
                    unfocusedBorderColor = Slate700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (oldPass.isBlank() || newPass.isBlank()) {
                        Toast.makeText(context, "Fill all password fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (newPass != confirmPass) {
                        Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val (success, msg) = repository.changePassword(oldPass, newPass)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Update Password", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Change Mobile Full Screen
// -------------------------------------------------------------
@Composable
fun ChangeMobileScreen(
    repository: ExpenseRepository,
    currentUser: com.example.model.UserProfile?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var countryCode by remember { mutableStateOf(currentUser?.countryCode ?: "+91") }
    var mobile by remember { mutableStateOf(currentUser?.mobile ?: "") }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FullScreenHeader(title = "Change Mobile Number", onBack = onBack)

            Spacer(modifier = Modifier.height(18.dp))

            Text(text = "Enter your new phone number", color = Slate400, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = countryCode,
                    onValueChange = { countryCode = it },
                    label = { Text("Code", color = Slate400) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlueAccent,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(90.dp)
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number", color = Slate400) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlueAccent,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isOtpSent) {
                Button(
                    onClick = {
                        if (mobile.isBlank()) {
                            Toast.makeText(context, "Enter a valid mobile number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isOtpSent = true
                        Toast.makeText(context, "OTP sent to $countryCode $mobile (Use 123456)", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Send Verification OTP", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("Enter OTP (123456)", color = Slate400) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlueAccent,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (otp == "123456") {
                            repository.changeMobile(mobile, countryCode)
                            Toast.makeText(context, "Mobile number updated successfully!", Toast.LENGTH_SHORT).show()
                            onBack()
                        } else {
                            Toast.makeText(context, "Invalid OTP. Use 123456", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Verify & Save Number", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. Export / Import Data Full Screen
// -------------------------------------------------------------
@Composable
fun ExportImportScreen(
    repository: ExpenseRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var importText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FullScreenHeader(title = "Export / Import Data", onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            // Export Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "Export Offline Data", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generate a complete backup JSON containing your expenses, debt ledger, and settings.",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val json = repository.exportDataJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("ExpensesBackup", json)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Data JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BlueAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy JSON", color = BlueAccent, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val json = repository.exportDataJson()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, json)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export Expenses Data"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share File", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Import Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "Import Data", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste a previously exported JSON backup to merge records without overwriting current data.",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        placeholder = { Text("Paste JSON here...", color = Slate500, fontSize = 11.sp) },
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueAccent,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (importText.isBlank()) {
                                Toast.makeText(context, "Paste valid JSON first", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val success = repository.importDataJson(importText.trim())
                            if (success) {
                                Toast.makeText(context, "Data imported successfully!", Toast.LENGTH_SHORT).show()
                                importText = ""
                            } else {
                                Toast.makeText(context, "Invalid JSON format", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restore From Pasted JSON", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. Backup Full Screen
// -------------------------------------------------------------
@Composable
fun BackupScreen(
    repository: ExpenseRepository,
    lastBackupTimestamp: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var lastBackup by remember { mutableStateOf(lastBackupTimestamp) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FullScreenHeader(title = "Full Offline Backup", onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Offline Snapshot Status", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (lastBackup > 0)
                            "Last successful backup: ${SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date(lastBackup))}"
                        else "No offline snapshot created yet",
                        color = Slate300,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Full backup ensures all your expenses and debt ledger remain safe across logins. Nothing is deleted until you choose to delete it.",
                        color = Slate500,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val ts = repository.createBackup()
                    lastBackup = ts
                    Toast.makeText(context, "Backup snapshot created successfully!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Backup, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Full Backup Now", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val success = repository.restoreFromBackup()
                    if (success) {
                        Toast.makeText(context, "Data restored from backup!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No backup snapshot found to restore", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restore From Snapshot", color = EmeraldSuccess, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 7. Help Guide (User Manual [Hindi / English]) Full Screen
// -------------------------------------------------------------
@Composable
fun HelpGuideScreen(
    initialLanguage: String,
    onBack: () -> Unit
) {
    var guideLang by remember { mutableStateOf(initialLanguage) }
    val isHi = guideLang == "hi"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FullScreenHeader(title = if (isHi) "उपयोगकर्ता मार्गदर्शिका (User Manual)" else "Help Guide (User Manual)", onBack = onBack)

            Spacer(modifier = Modifier.height(12.dp))

            // Language Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (guideLang == "en") BlueAccent else Slate800)
                        .clickable { guideLang = "en" }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("English", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (guideLang == "hi") BlueAccent else Slate800)
                        .clickable { guideLang = "hi" }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("हिन्दी", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val guides = if (isHi) listOf(
                "1. खर्च कैसे जोड़ें (Adding Expenses)" to "निचले दाएं कोने में (+) बटन दबाएं। राशि, श्रेणी (भोजन, यात्रा, किराया आदि) और भुगतान विधि (नकद, यूपीआई आदि) चुनें। आप विवरण और तारीख भी दर्ज कर सकते हैं।",
                "2. उधार खाता प्रणाली (Udhar Debt Tracker)" to "जब आप श्रेणी 'Udhar' चुनते हैं, तो व्यक्ति का नाम, दी गई राशि (Given), और वापस मिली राशि (Returned) दर्ज करें। ऐप अपने आप बकाया राशि (Remaining = Given - Returned) की गणना करता है। समान व्यक्ति पर नया लेनदेन स्वतः जुड़ जाता है!",
                "3. मासिक फोटो कैलेंडर (Photo Calendar)" to "प्रत्येक महीने के लिए भारतीय सेना, नौसेना, वायुसेना और ऐतिहासिक स्थलों की सुंदर पृष्ठभूमि प्रदर्शित होती है। किसी भी दिन पर क्लिक करके उस दिन के खर्च देख सकते हैं।",
                "4. रिपोर्ट और बजट (Reports & Budget)" to "रिपोर्ट आइकन पर क्लिक करके मासिक बजट सीमा, श्रेणी-वार वितरण और उधार खाते की स्थिति देख सकते हैं।",
                "5. फिंगरप्रिंट सुरक्षा (Fingerprint Security)" to "अपने डिवाइस के असली फिंगरप्रिंट सेंसर से लॉगिन और ऐप अनलॉक कर सकते हैं। ऐप पूरी तरह ऑफ़लाइन और सुरक्षित है।",
                "6. रीसायकल बिन और बैकअप (Recycle Bin & Backup)" to "हटाए गए खर्च रीसायकल बिन में 30 दिनों तक सुरक्षित रहते हैं। आप उन्हें कभी भी पुनर्स्थापित कर सकते हैं।"
            ) else listOf(
                "1. Adding Expenses" to "Tap the (+) Floating Action Button on the main screen. Enter the amount, choose a category (Food, Travel, Rent, etc.), and select the payment mode. Add an optional description note and pick the date.",
                "2. Udhar Debt Ledger Tracker" to "Select 'Udhar' category. Enter Person Name, Amount Given, and Amount Returned. The app automatically computes Remaining = Given - Returned. If you add to an existing person again, balances merge seamlessly!",
                "3. Monthly Photo Calendar" to "Enjoy cultural, Indian Army, Navy, and Airforce backgrounds each month. Click any day number to filter transactions by that exact date.",
                "4. Analytics & Budgeting" to "Tap the bar chart icon to inspect budget consumption progress, category pie distributions, and person-wise pending debt amounts.",
                "5. Fingerprint Authentication" to "Enable biometric lock in Settings to guard your financial records using your phone's physical fingerprint reader.",
                "6. Recycle Bin & Data Safety" to "Deleted items stay in the Recycle Bin for 30 days before being automatically purged. You can restore or permanently delete them at any time."
            )

            guides.forEach { (title, desc) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(text = title, color = BlueAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = desc, color = Slate300, fontSize = 11.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. Send Feedback (Anonymous Plant Names) Full Screen
// -------------------------------------------------------------
@Composable
fun FeedbackScreen(
    feedbacks: List<FeedbackItem>,
    onAddFeedback: (Int, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FullScreenHeader(title = "Community Feedback", onBack = onBack)

            Spacer(modifier = Modifier.height(14.dp))

            // Submit Feedback Form
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "Leave Anonymous Review", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Your identity is protected; your name appears as an Indian plant/tree", color = Slate400, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // 5 Star Rating Selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "$i Stars",
                                tint = if (i <= rating) Note100Yellow else Slate500,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { rating = i }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        placeholder = { Text("Write your thoughts or suggestion...", color = Slate500, fontSize = 12.sp) },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueAccent,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (comment.isBlank()) {
                                Toast.makeText(context, "Please write a comment", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onAddFeedback(rating, comment)
                            comment = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Post Review", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "All User Reviews (${feedbacks.size})", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Review List
            feedbacks.forEach { fb ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = fb.plantName, color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row {
                                for (s in 1..fb.rating) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Note100Yellow, modifier = Modifier.size(13.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = fb.comment, color = Color.White, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(fb.timestamp)),
                            color = Slate500,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 9. Contact Us Full Screen
// -------------------------------------------------------------
@Composable
fun ContactUsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val contactEmail = "yahimilegaadda@gmail.com"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FullScreenHeader(title = "Contact Us", onBack = onBack)

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BlueAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Mail, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Official Support Email", color = Slate400, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = contactEmail, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$contactEmail")
                                    putExtra(Intent.EXTRA_SUBJECT, "Support: My Expenses Report")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Open Email App", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("ContactEmail", contactEmail)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Email copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Copy Email", color = Slate300, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 10. Delete Expenses (Recycle Bin) Full Screen
// -------------------------------------------------------------
@Composable
fun DeleteExpensesScreen(
    repository: ExpenseRepository,
    deletedExpenses: List<Expense>,
    currency: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()
    val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000L

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FullScreenHeader(title = "Delete Expenses", onBack = onBack, modifier = Modifier.weight(1f))
                if (deletedExpenses.isNotEmpty()) {
                    TextButton(onClick = {
                        repository.emptyTrash()
                        Toast.makeText(context, "All deleted expenses purged", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Empty Trash", color = RedDanger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Expenses deleted from your active lists stay here for 30 days before being automatically purged.",
                color = Slate400,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (deletedExpenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Slate500, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Recycle bin is completely empty", color = Slate400, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(deletedExpenses, key = { it.id }) { item ->
                        val delTime = item.deletedAt ?: item.timestamp
                        val elapsed = now - delTime
                        val daysLeft = maxOf(0, 30 - (elapsed / (24 * 60 * 60 * 1000L)).toInt())

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate800)
                                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.category} - $currency${"%,.0f".format(item.amount)}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (item.person.isNotBlank() || item.note.isNotBlank()) {
                                        Text(
                                            text = listOf(item.person, item.note).filter { it.isNotBlank() }.joinToString(" • "),
                                            color = Slate400,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$daysLeft days left before auto-purge",
                                        color = Note200Orange,
                                        fontSize = 10.sp
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Restore Button
                                    IconButton(
                                        onClick = {
                                            repository.restoreExpense(item)
                                            Toast.makeText(context, "Restored to active expenses!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.Restore, contentDescription = "Restore", tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                                    }

                                    // Permanently Delete Button
                                    IconButton(
                                        onClick = {
                                            repository.permanentlyDeleteExpense(item)
                                            Toast.makeText(context, "Permanently deleted", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteForever, contentDescription = "Delete", tint = RedDanger, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Top Header for Sub-screens
// -------------------------------------------------------------
@Composable
private fun FullScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.textPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

