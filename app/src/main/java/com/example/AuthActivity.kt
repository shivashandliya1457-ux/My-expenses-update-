package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.ExpenseRepository
import com.example.model.UserProfile
import com.example.ui.components.AppLogo
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.AuthStorage
import com.example.util.BiometricHelper

class AuthActivity : FragmentActivity() {

    private lateinit var repository: ExpenseRepository
    private var isBiometricActive = false
    private var userDismissedBiometric = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = ExpenseRepository(applicationContext)

        // Multi-account session check
        val hasAccount = AuthStorage.hasAnyAccount(this)
        val isLoggedIn = AuthStorage.isLoggedIn(this)
        val isFingerprintEnabled = AuthStorage.isFingerprintEnabledForActiveUser(this)

        // BUG 1: NO AUTO FINGERPRINT AFTER LOGOUT!
        // Automatic fingerprint prompt ONLY triggers if user was actively logged in (app was closed without logout)
        if (hasAccount && isLoggedIn && isFingerprintEnabled) {
            triggerBiometricPrompt()
        }

        setContent {
            val appConfig = repository.appConfig.value
            MyApplicationTheme(darkTheme = appConfig.isDarkMode) {
                AuthScreenContent(
                    hasAccount = hasAccount,
                    isLoggedIn = isLoggedIn,
                    isFingerprintEnabled = isFingerprintEnabled,
                    onTryFingerprintAgain = {
                        userDismissedBiometric = false
                        triggerBiometricPrompt()
                    },
                    onUnlockWithScreenLock = {
                        userDismissedBiometric = false
                        triggerScreenLockPrompt()
                    },
                    onLoginSuccess = { username ->
                        repository.switchUser(username)
                        navigateToMainActivity(username)
                    },
                    onSignupSuccess = { username, password ->
                        AuthStorage.saveUser(this, username, password, isFingerprintEnabled = false, markLoggedIn = true)
                        val profile = UserProfile(
                            username = username.trim(),
                            password = password,
                            securityQuestion = "Default",
                            securityAnswer = "Default",
                            isBiometricEnabled = false
                        )
                        repository.registerUser(profile)
                    },
                    onSetFingerprintChoice = { enabled ->
                        val activeUser = AuthStorage.getActiveUsername(this) ?: repository.currentUser.value?.username ?: ""
                        AuthStorage.setFingerprintEnabledForUser(this, activeUser, enabled)
                        repository.updateConfig(repository.appConfig.value.copy(isBiometricEnabled = enabled))
                        navigateToMainActivity(activeUser)
                    },
                    onPasswordUpdated = { username, newPassword ->
                        AuthStorage.updatePassword(this, username, newPassword)
                        val existing = repository.getUser(username)
                        if (existing != null) {
                            repository.registerUser(existing.copy(password = newPassword))
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val hasAccount = AuthStorage.hasAnyAccount(this)
        val isLoggedIn = AuthStorage.isLoggedIn(this)
        val isFingerprintEnabled = AuthStorage.isFingerprintEnabledForActiveUser(this)

        // BUG 1: Automatic trigger on onResume ONLY if actively logged in and not already running / not dismissed
        if (hasAccount && isLoggedIn && isFingerprintEnabled && !isBiometricActive && !userDismissedBiometric) {
            triggerBiometricPrompt()
        }
    }

    override fun onPause() {
        super.onPause()
        // Reset dismissal flag so when user returns to app later, sensor will auto-trigger on resume
        userDismissedBiometric = false
    }

    private fun triggerBiometricPrompt() {
        if (isBiometricActive) return
        isBiometricActive = true

        BiometricHelper.showBiometricPrompt(
            activity = this,
            title = "Fingerprint & Screen Lock",
            subtitle = "Touch fingerprint sensor to unlock",
            description = "Authenticate to securely access My Expenses Report",
            authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            onSuccess = {
                isBiometricActive = false
                userDismissedBiometric = false
                val active = AuthStorage.getActiveUsername(this) ?: AuthStorage.getLastUsername(this) ?: "User"
                navigateToMainActivity(active)
            },
            onError = { errMsg ->
                isBiometricActive = false
                userDismissedBiometric = true
                Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show()
            },
            onUserDismissed = {
                isBiometricActive = false
                userDismissedBiometric = true
            }
        )
    }

    private fun triggerScreenLockPrompt() {
        if (isBiometricActive) return
        isBiometricActive = true

        BiometricHelper.showBiometricPrompt(
            activity = this,
            title = "Unlock with Screen Lock",
            subtitle = "Enter Device PIN, Pattern, or Password",
            description = "Authenticate with your device screen lock to access My Expenses Report",
            authenticators = BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            onSuccess = {
                isBiometricActive = false
                userDismissedBiometric = false
                val active = AuthStorage.getActiveUsername(this) ?: AuthStorage.getLastUsername(this) ?: "User"
                navigateToMainActivity(active)
            },
            onError = { errMsg ->
                isBiometricActive = false
                userDismissedBiometric = true
                Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show()
            },
            onUserDismissed = {
                isBiometricActive = false
                userDismissedBiometric = true
            }
        )
    }

    private fun navigateToMainActivity(username: String? = null) {
        val targetUser = username ?: AuthStorage.getActiveUsername(this) ?: AuthStorage.getLastUsername(this) ?: "User"
        val existing = repository.getUser(targetUser)
        if (existing != null) {
            repository.loginUser(existing)
        } else {
            val pass = AuthStorage.getUserPassword(this, targetUser) ?: ""
            val fp = AuthStorage.isFingerprintEnabledForUser(this, targetUser)
            val profile = UserProfile(
                username = targetUser,
                password = pass,
                securityQuestion = "Default",
                securityAnswer = "Default",
                isBiometricEnabled = fp
            )
            repository.registerUser(profile)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("authenticated", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}

@Composable
fun AuthScreenContent(
    hasAccount: Boolean,
    isLoggedIn: Boolean,
    isFingerprintEnabled: Boolean,
    onTryFingerprintAgain: () -> Unit,
    onUnlockWithScreenLock: () -> Unit,
    onLoginSuccess: (username: String) -> Unit,
    onSignupSuccess: (username: String, password: String) -> Unit,
    onSetFingerprintChoice: (enabled: Boolean) -> Unit,
    onPasswordUpdated: (username: String, newPassword: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Tabs: "login", "signup", "forgot"
    var currentTab by remember { mutableStateOf(if (hasAccount) "login" else "signup") }

    // Login Fields
    var loginUsername by remember { mutableStateOf(AuthStorage.getSavedUsername(context) ?: "") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Signup Fields
    var signupUsername by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupConfirmPassword by remember { mutableStateOf("") }
    var signupPasswordVisible by remember { mutableStateOf(false) }
    var showFingerprintOptInDialog by remember { mutableStateOf(false) }

    // Forgot Password Fields
    var forgotUsername by remember { mutableStateOf("") }
    var forgotStep by remember { mutableStateOf(1) } // 1 = Verify Username, 2 = Set New Password
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }

    // Fingerprint Opt-in Dialog after Signup (Requirement 1)
    if (showFingerprintOptInDialog) {
        AlertDialog(
            onDismissRequest = {
                showFingerprintOptInDialog = false
                onSetFingerprintChoice(false)
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint",
                        tint = BlueAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Enable Fingerprint Unlock?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Would you like to enable quick & secure fingerprint / biometric unlock for future app launches?",
                    color = Slate400,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFingerprintOptInDialog = false
                        Toast.makeText(context, "Fingerprint Unlock Enabled!", Toast.LENGTH_SHORT).show()
                        onSetFingerprintChoice(true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    modifier = Modifier.testTag("btn_fp_enable_yes")
                ) {
                    Text("Yes, Enable", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showFingerprintOptInDialog = false
                        Toast.makeText(context, "Fingerprint unlock skipped.", Toast.LENGTH_SHORT).show()
                        onSetFingerprintChoice(false)
                    },
                    modifier = Modifier.testTag("btn_fp_enable_no")
                ) {
                    Text("No, Thanks", color = Slate400)
                }
            },
            containerColor = Slate800,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(20.dp)
            .testTag("auth_activity_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            AppLogo(size = 72.dp)

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "My Expenses Report",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Secure Personal Finance & Ledger",
                color = Slate500,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Tabs: [Login] [Sign-up] [Forgot Password]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                // Login Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (currentTab == "login") BlueAccent else Color.Transparent)
                        .clickable { currentTab = "login" }
                        .padding(vertical = 10.dp)
                        .testTag("tab_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Login",
                        color = if (currentTab == "login") Color.White else Slate400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Sign-up Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (currentTab == "signup") BlueAccent else Color.Transparent)
                        .clickable { currentTab = "signup" }
                        .padding(vertical = 10.dp)
                        .testTag("tab_signup"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign-up",
                        color = if (currentTab == "signup") Color.White else Slate400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Forgot Password Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (currentTab == "forgot") BlueAccent else Color.Transparent)
                        .clickable {
                            currentTab = "forgot"
                            forgotStep = 1
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_forgot"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Forgot",
                        color = if (currentTab == "forgot") Color.White else Slate400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TAB 1: LOGIN
            if (currentTab == "login") {
                // Fallback biometric buttons ONLY if returning to an active logged-in session (never after logout)
                if (hasAccount && isLoggedIn && isFingerprintEnabled) {
                    // a) [Try Fingerprint Again]
                    Button(
                        onClick = { onTryFingerprintAgain() },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, BlueAccent, RoundedCornerShape(12.dp))
                            .testTag("btn_try_fingerprint_again")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint",
                            tint = BlueAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Try Fingerprint Again",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // b) [Unlock with Screen Lock]
                    OutlinedButton(
                        onClick = { onUnlockWithScreenLock() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_unlock_screen_lock")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Screen Lock",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Unlock with Screen Lock (PIN/Pattern)",
                            color = Slate400,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Slate700)
                        )
                        Text(
                            text = "  OR LOGIN WITH PASSWORD  ",
                            color = Slate500,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Slate700)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // c) [Login with Username & Password]
                OutlinedTextField(
                    value = loginUsername,
                    onValueChange = { loginUsername = it },
                    label = { Text("Username", color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "User", tint = Slate400)
                    },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_login_username")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = { loginPassword = it },
                    label = { Text("Password", color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Pass", tint = Slate400)
                    },
                    trailingIcon = {
                        IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                            Icon(
                                imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = Slate400
                            )
                        }
                    },
                    visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_login_password")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val u = loginUsername.trim()
                        val p = loginPassword
                        if (u.isBlank() || p.isBlank()) {
                            Toast.makeText(context, "Please enter both Username & Password", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (AuthStorage.validateLogin(context, u, p)) {
                            Toast.makeText(context, "Welcome back, $u!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(u)
                        } else {
                            Toast.makeText(context, "Invalid Details", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_login_submit")
                ) {
                    Text("Login with Username & Password", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sign-up New Account",
                        color = BlueAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { currentTab = "signup" }
                            .padding(4.dp)
                            .testTag("link_signup")
                    )

                    Text(
                        text = "Forgot Password?",
                        color = Slate400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable {
                                currentTab = "forgot"
                                forgotStep = 1
                            }
                            .padding(4.dp)
                            .testTag("link_forgot")
                    )
                }
            }

            // TAB 2: SIGN-UP
            else if (currentTab == "signup") {
                Text(
                    text = "Create Your Account",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = "All data is securely encrypted & stored locally on this device.",
                    color = Slate500,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Username Field
                OutlinedTextField(
                    value = signupUsername,
                    onValueChange = { signupUsername = it },
                    label = { Text("Username", color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Username", tint = Slate400)
                    },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_signup_username")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Field
                OutlinedTextField(
                    value = signupPassword,
                    onValueChange = { signupPassword = it },
                    label = { Text("Password", color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Password", tint = Slate400)
                    },
                    trailingIcon = {
                        IconButton(onClick = { signupPasswordVisible = !signupPasswordVisible }) {
                            Icon(
                                imageVector = if (signupPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = Slate400
                            )
                        }
                    },
                    visualTransformation = if (signupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_signup_password")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm Password Field
                OutlinedTextField(
                    value = signupConfirmPassword,
                    onValueChange = { signupConfirmPassword = it },
                    label = { Text("Confirm Password", color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Confirm Password", tint = Slate400)
                    },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_signup_confirm_password")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val u = signupUsername.trim()
                        val p = signupPassword
                        val cp = signupConfirmPassword

                        if (u.isBlank()) {
                            Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (AuthStorage.userExists(context, u)) {
                            Toast.makeText(context, "Username '$u' already exists! Please choose another.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (p.isBlank()) {
                            Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (p != cp) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Save permanently (Requirement 1)
                        onSignupSuccess(u, p)
                        // Trigger Fingerprint Opt-in Dialog (Requirement 1)
                        showFingerprintOptInDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_signup_submit")
                ) {
                    Text("Sign-up with Username & Password", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Already have an account? Login here",
                    color = BlueAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { currentTab = "login" }
                        .padding(6.dp)
                        .testTag("link_back_to_login")
                )
            }

            // TAB 3: FORGOT PASSWORD (Requirement 3e)
            else if (currentTab == "forgot") {
                Text(
                    text = "Reset Your Password",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = if (forgotStep == 1) "Enter your registered Username to verify your account" else "Enter a new secure password for $forgotUsername",
                    color = Slate500,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (forgotStep == 1) {
                    OutlinedTextField(
                        value = forgotUsername,
                        onValueChange = { forgotUsername = it },
                        label = { Text("Registered Username", color = Slate400) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "User", tint = Slate400)
                        },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_forgot_username")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val u = forgotUsername.trim()
                            if (u.isBlank()) {
                                Toast.makeText(context, "Enter your Username", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (AuthStorage.userMatches(context, u)) {
                                forgotStep = 2
                                Toast.makeText(context, "Username verified! Enter your new password", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Username not found in saved records", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_verify_username")
                    ) {
                        Text("Verify Username", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else {
                    // Step 2: New Password & Confirm New Password
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password", color = Slate400) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "New Password", tint = Slate400)
                        },
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password",
                                    tint = Slate400
                                )
                            }
                        },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_password")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it },
                        label = { Text("Confirm New Password", color = Slate400) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Confirm New Password", tint = Slate400)
                        },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_confirm_new_password")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val u = forgotUsername.trim()
                            if (newPassword.isBlank()) {
                                Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (newPassword != confirmNewPassword) {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Permanently update password in SharedPreferences (Requirement 3e)
                            onPasswordUpdated(u, newPassword)
                            Toast.makeText(context, "Password updated successfully! Logging you in...", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(u)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_new_password")
                    ) {
                        Text("Save New Password & Login", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Back to Login",
                    color = BlueAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            currentTab = "login"
                            forgotStep = 1
                        }
                        .padding(6.dp)
                        .testTag("link_forgot_back_to_login")
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "100% Offline & Private • Zero Cloud Leakage • Biometric Hardware Secured",
                color = Slate500,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
