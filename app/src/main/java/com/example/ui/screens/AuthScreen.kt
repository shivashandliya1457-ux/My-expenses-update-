package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseRepository
import com.example.model.Translations
import com.example.model.UserProfile
import com.example.ui.components.AppLogo
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.AuthStorage

@Composable
fun AuthScreen(
    repository: ExpenseRepository,
    language: String,
    onLoginSuccess: () -> Unit,
    onNavigateToForgot: () -> Unit,
    onTriggerBiometric: (onSuccess: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val hasAccount = AuthStorage.hasAccount(context)

    var authTab by remember { mutableStateOf(if (hasAccount) "login" else "signup") } // "login", "signup"
    var username by remember { mutableStateOf(AuthStorage.getSavedUsername(context) ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showFingerprintOptInDialog by remember { mutableStateOf(false) }

    // Dialog for Fingerprint opt-in after signup
    if (showFingerprintOptInDialog) {
        AlertDialog(
            onDismissRequest = {
                showFingerprintOptInDialog = false
                AuthStorage.setFingerprintEnabled(context, false)
                repository.updateConfig(repository.appConfig.value.copy(isBiometricEnabled = false))
                onLoginSuccess()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint",
                        tint = BlueAccent,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enable Fingerprint Unlock?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Would you like to enable quick fingerprint & screen lock verification for your future logins?",
                    color = Slate400,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFingerprintOptInDialog = false
                        AuthStorage.setFingerprintEnabled(context, true)
                        repository.updateConfig(repository.appConfig.value.copy(isBiometricEnabled = true))
                        Toast.makeText(context, "Fingerprint unlock enabled!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                ) {
                    Text("Yes, Enable", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showFingerprintOptInDialog = false
                        AuthStorage.setFingerprintEnabled(context, false)
                        repository.updateConfig(repository.appConfig.value.copy(isBiometricEnabled = false))
                        onLoginSuccess()
                    }
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
            .testTag("auth_screen")
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
                text = Translations.get("app_title", language),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = Translations.get("designed_by", language),
                color = Slate500,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Auth Tab Switcher
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
                        .background(if (authTab == "login") BlueAccent else Color.Transparent)
                        .clickable { authTab = "login" }
                        .padding(vertical = 10.dp)
                        .testTag("tab_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Translations.get("login", language),
                        color = if (authTab == "login") Color.White else Slate400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Signup Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (authTab == "signup") BlueAccent else Color.Transparent)
                        .clickable { authTab = "signup" }
                        .padding(vertical = 10.dp)
                        .testTag("tab_signup"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Translations.get("signup", language),
                        color = if (authTab == "signup") Color.White else Slate400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Forgot Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToForgot() }
                        .padding(vertical = 10.dp)
                        .testTag("tab_forgot"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Translations.get("forgot", language),
                        color = Slate400,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Inputs: Username & Password
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(Translations.get("username", language), color = Slate400) },
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
                    .testTag("input_username")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(Translations.get("password", language), color = Slate400) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Password", tint = Slate400)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password",
                            tint = Slate400
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                    .testTag("input_password")
            )

            if (authTab == "signup") {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password", color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Confirm", tint = Slate400)
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
                        .testTag("input_confirm_password")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Action Button (Login or Signup)
            Button(
                onClick = {
                    if (authTab == "login") {
                        if (username.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Enter Username & Password", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (AuthStorage.validateLogin(context, username, password)) {
                            val user = repository.getUser(username.trim()) ?: UserProfile(
                                username = username.trim(),
                                password = password,
                                securityQuestion = "Default",
                                securityAnswer = "Default",
                                isBiometricEnabled = AuthStorage.isFingerprintEnabledForUser(context, username.trim())
                            )
                            repository.loginUser(user)
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "Invalid Details", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Signup (Requirement 1)
                        val u = username.trim()
                        val p = password
                        val cp = confirmPassword

                        if (u.isBlank() || p.isBlank() || cp.isBlank()) {
                            Toast.makeText(context, "Please fill Username, Password, and Confirm Password", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (AuthStorage.userExists(context, u)) {
                            Toast.makeText(context, "Username '$u' already exists! Please choose another.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (p != cp) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Save permanently in SharedPreferences
                        AuthStorage.saveUser(context, u, p)
                        val newUser = UserProfile(
                            username = u,
                            password = p,
                            securityQuestion = "Default",
                            securityAnswer = "Default",
                            isBiometricEnabled = false
                        )
                        repository.registerUser(newUser)

                        // Show dialog asking "Enable Fingerprint Unlock? Yes / No"
                        showFingerprintOptInDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_auth_primary")
            ) {
                Text(
                    text = if (authTab == "login") "Login with Username & Password" else "Sign-up with Username & Password",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (authTab == "login" && hasAccount) {
                Spacer(modifier = Modifier.height(12.dp))

                // Biometric Fingerprint Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .clickable {
                            onTriggerBiometric {
                                val savedUser = AuthStorage.getSavedUsername(context)
                                if (savedUser != null) {
                                    val user = repository.getUser(savedUser) ?: UserProfile(
                                        username = savedUser,
                                        password = AuthStorage.getSavedPassword(context) ?: "",
                                        securityQuestion = "Default",
                                        securityAnswer = "Default",
                                        isBiometricEnabled = true
                                    )
                                    repository.loginUser(user)
                                }
                                onLoginSuccess()
                            }
                        }
                        .testTag("btn_fingerprint_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Unlock with Fingerprint",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "100% Offline • Encrypted Locally • Username & Password Secured",
                color = Slate500,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
