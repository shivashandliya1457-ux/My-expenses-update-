package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.data.ExpenseRepository
import com.example.model.Expense
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.UpdateDialogState
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TrustCenterScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate900
import com.example.util.BiometricHelper
import com.example.util.UpdateCheckResult
import com.example.util.UpdateInfo
import com.example.util.UpdateManager
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : FragmentActivity() {

    private lateinit var repository: ExpenseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = ExpenseRepository(applicationContext)

        setContent {
            val appConfig by repository.appConfig.collectAsState()
            MyApplicationTheme(darkTheme = appConfig.isDarkMode) {
                AppRoot(
                    activity = this,
                    repository = repository
                )
            }
        }
    }
}

@Composable
fun AppRoot(
    activity: FragmentActivity,
    repository: ExpenseRepository
) {
    var currentView by remember { mutableStateOf("splash") } // splash, auth, forgot, main, add, reports, settings
    var addExpenseInitialDate by remember { mutableStateOf(Calendar.getInstance()) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }

    val expenses by repository.expenses.collectAsState()
    val deletedExpenses by repository.deletedExpenses.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val appConfig by repository.appConfig.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var updateDialogState by remember { mutableStateOf<UpdateDialogState>(UpdateDialogState.Idle) }

    fun performUpdateCheck(manual: Boolean) {
        if (manual) {
            updateDialogState = UpdateDialogState.Checking
        }
        coroutineScope.launch {
            val res = UpdateManager.checkForUpdates(activity, appConfig.updateServerUrl)
            when (res) {
                is UpdateCheckResult.UpdateAvailable -> {
                    updateDialogState = UpdateDialogState.Available(res.info)
                }
                is UpdateCheckResult.NoUpdate -> {
                    if (manual) {
                        updateDialogState = UpdateDialogState.UpToDate("v${res.currentVersion}")
                    }
                }
                is UpdateCheckResult.Error -> {
                    if (manual) {
                        updateDialogState = UpdateDialogState.Error(res.message)
                    }
                }
            }
        }
    }

    fun startUpdateDownload(info: UpdateInfo) {
        coroutineScope.launch {
            updateDialogState = UpdateDialogState.Downloading(0, info)
            val result = UpdateManager.downloadApk(activity, info.apkUrl) { progress ->
                updateDialogState = UpdateDialogState.Downloading(progress, info)
            }
            result.onSuccess { file ->
                updateDialogState = UpdateDialogState.ReadyToInstall(file, info)
                UpdateManager.installApk(activity, file)
            }.onFailure { err ->
                updateDialogState = UpdateDialogState.Error(err.localizedMessage ?: "Failed to download update package")
            }
        }
    }

    // Silently check updates on launch if auto-check enabled
    var hasCheckedUpdateOnLaunch by remember { mutableStateOf(false) }
    LaunchedEffect(currentUser) {
        if (!hasCheckedUpdateOnLaunch && currentUser != null && appConfig.autoCheckUpdates) {
            hasCheckedUpdateOnLaunch = true
            performUpdateCheck(manual = false)
        }
    }

    fun triggerBiometric(onSuccess: () -> Unit) {
        BiometricHelper.showBiometricPrompt(
            activity = activity,
            title = "Fingerprint Authentication",
            subtitle = "Touch the fingerprint sensor",
            description = "Scan your registered fingerprint to verify your identity",
            onSuccess = {
                onSuccess()
            },
            onError = { errMsg ->
                Toast.makeText(activity, errMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (appConfig.isDarkMode) Slate900 else androidx.compose.ui.graphics.Color(0xFFF8FAFC))
    ) {
        when (currentView) {
            "splash" -> {
                SplashScreen(
                    onSplashFinished = {
                        val isAuthenticated = activity.intent.getBooleanExtra("authenticated", false)
                        if (isAuthenticated || currentUser != null) {
                            currentView = "main"
                        } else {
                            val intent = android.content.Intent(activity, AuthActivity::class.java).apply {
                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            activity.startActivity(intent)
                            activity.finish()
                        }
                    }
                )
            }

            "auth" -> {
                AuthScreen(
                    repository = repository,
                    language = appConfig.language,
                    onLoginSuccess = { currentView = "main" },
                    onNavigateToForgot = { currentView = "forgot" },
                    onTriggerBiometric = { onSuccess ->
                        triggerBiometric {
                            onSuccess()
                        }
                    }
                )
            }

            "forgot" -> {
                BackHandler { currentView = "auth" }
                ForgotPasswordScreen(
                    repository = repository,
                    language = appConfig.language,
                    onBack = { currentView = "auth" },
                    onSuccessLogin = { currentView = "main" },
                    onTriggerBiometric = { onSuccess ->
                        triggerBiometric {
                            onSuccess()
                        }
                    }
                )
            }

            "main" -> {
                MainScreen(
                    repository = repository,
                    expenses = expenses,
                    config = appConfig,
                    onNavigateToAdd = { dateCal ->
                        addExpenseInitialDate = dateCal
                        expenseToEdit = null
                        currentView = "add"
                    },
                    onNavigateToEdit = { exp ->
                        expenseToEdit = exp
                        currentView = "add"
                    },
                    onNavigateToReports = { currentView = "reports" },
                    onNavigateToSettings = { currentView = "settings" },
                    onNavigateToTrustCenter = { currentView = "trust_center" }
                )
            }

            "add" -> {
                BackHandler {
                    expenseToEdit = null
                    currentView = "main"
                }
                AddExpenseScreen(
                    repository = repository,
                    initialDate = addExpenseInitialDate,
                    currency = appConfig.currency,
                    language = appConfig.language,
                    expenseToEdit = expenseToEdit,
                    onBack = {
                        expenseToEdit = null
                        currentView = "main"
                    },
                    onExpenseSaved = {
                        expenseToEdit = null
                        currentView = "main"
                    }
                )
            }

            "reports" -> {
                BackHandler { currentView = "main" }
                ReportsScreen(
                    expenses = expenses,
                    currency = appConfig.currency,
                    budget = appConfig.monthlyBudget,
                    language = appConfig.language,
                    onBack = { currentView = "main" }
                )
            }

            "settings" -> {
                BackHandler { currentView = "main" }
                SettingsScreen(
                    repository = repository,
                    config = appConfig,
                    deletedExpenses = deletedExpenses,
                    expenses = expenses,
                    onBack = { currentView = "main" },
                    onLogout = {
                        repository.logout()
                        val intent = android.content.Intent(activity, AuthActivity::class.java).apply {
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        activity.startActivity(intent)
                        activity.finish()
                    },
                    onTriggerBiometric = { onSuccess ->
                        triggerBiometric {
                            onSuccess()
                        }
                    },
                    onCheckUpdate = {
                        performUpdateCheck(manual = true)
                    }
                )
            }

            "trust_center" -> {
                BackHandler { currentView = "main" }
                TrustCenterScreen(
                    expenses = expenses,
                    deletedExpenses = deletedExpenses,
                    isBiometricEnabled = appConfig.isBiometricEnabled,
                    onBack = { currentView = "main" }
                )
            }
        }

        // In-App Update Dialog
        AppUpdateDialog(
            state = updateDialogState,
            onDismiss = { updateDialogState = UpdateDialogState.Idle },
            onStartDownload = { info -> startUpdateDownload(info) },
            onRetry = { performUpdateCheck(manual = true) }
        )
    }
}
