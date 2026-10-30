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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseRepository
import com.example.model.Translations
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.AuthStorage

@Composable
fun ForgotPasswordScreen(
    repository: ExpenseRepository,
    language: String,
    onBack: () -> Unit,
    onSuccessLogin: () -> Unit,
    onTriggerBiometric: (onSuccess: () -> Unit) -> Unit
) {
    val context = LocalContext.current

    // Option A: Username Reset (Requirement 3e)
    var resetUsername by remember { mutableStateOf("") }
    var resetStep by remember { mutableStateOf(1) } // 1 = Ask Username, 2 = Set New Password
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }

    // Option B: Mobile OTP
    var countryCode by remember { mutableStateOf("+91") }
    var mobile by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var countryExpanded by remember { mutableStateOf(false) }

    val countryCodes = listOf(
        "+91" to "+91 India (Default)",
        "+1" to "+1 USA",
        "+44" to "+44 UK",
        "+971" to "+971 UAE",
        "+61" to "+61 AUS",
        "+81" to "+81 Japan",
        "+49" to "+49 Germany"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(18.dp)
            .testTag("forgot_password_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Back Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBack() }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Translations.get("back_one_step", language),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Account Recovery & Reset",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Option 1 (PRIMARY): Reset Password with Username (Requirement 3e)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate800)
                    .border(1.dp, BlueAccent, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Username Reset",
                            tint = BlueAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reset with Username (Instant)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (resetStep == 1) {
                        OutlinedTextField(
                            value = resetUsername,
                            onValueChange = { resetUsername = it },
                            label = { Text("Enter your Username", color = Slate400) },
                            singleLine = true,
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
                                .testTag("input_reset_username")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val u = resetUsername.trim()
                                if (u.isBlank()) {
                                    Toast.makeText(context, "Enter your username", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (AuthStorage.userMatches(context, u)) {
                                    resetStep = 2
                                    Toast.makeText(context, "Username verified! Enter new password", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Username not found in saved records", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_verify_reset_username")
                        ) {
                            Text("Verify Username", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Step 2: New Password & Confirm
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password", color = Slate400) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Pass", tint = Slate400)
                            },
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle",
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
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = confirmNewPassword,
                            onValueChange = { confirmNewPassword = it },
                            label = { Text("Confirm New Password", color = Slate400) },
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
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (newPassword.isBlank()) {
                                    Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (newPassword != confirmNewPassword) {
                                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val targetUser = resetUsername.trim().ifBlank { AuthStorage.getLastUsername(context) ?: "" }
                                AuthStorage.updatePassword(context, targetUser, newPassword)
                                val u = targetUser.ifBlank { AuthStorage.getSavedUsername(context) ?: "User" }
                                val existing = repository.getUser(u)
                                if (existing != null) {
                                    repository.registerUser(existing.copy(password = newPassword))
                                }
                                repository.switchUser(u)
                                Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                onSuccessLogin()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_confirm_new_password")
                        ) {
                            Text("Save New Password & Login", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option 2: Mobile + OTP
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = "Mobile",
                            tint = BlueAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Login via Mobile Number",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate900)
                                .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                                .clickable { countryExpanded = true }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = countryCode, color = Color.White, fontSize = 12.sp)
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Country",
                                    tint = Slate400
                                )
                            }

                            DropdownMenu(
                                expanded = countryExpanded,
                                onDismissRequest = { countryExpanded = false },
                                modifier = Modifier.background(Slate800)
                            ) {
                                countryCodes.forEach { (code, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label, color = Color.White, fontSize = 12.sp) },
                                        onClick = {
                                             countryCode = code
                                            countryExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            placeholder = { Text("Mobile No", color = Slate400, fontSize = 13.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BlueAccent,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        placeholder = { Text("OTP - 123456", color = Slate400, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueAccent,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (otp == "123456") {
                                Toast.makeText(context, "Verified - Logged in successfully!", Toast.LENGTH_SHORT).show()
                                val user = repository.getAnyRegisteredUser()
                                if (user != null) {
                                    repository.loginUser(user)
                                }
                                onSuccessLogin()
                            } else {
                                Toast.makeText(context, "Enter correct OTP (123456)", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_verify_otp")
                    ) {
                        Text(text = "Verify OTP", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option 3: Real Screen Lock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                    .clickable {
                        onTriggerBiometric {
                            val user = repository.getAnyRegisteredUser()
                            if (user != null) {
                                repository.loginUser(user)
                            }
                            onSuccessLogin()
                        }
                    }
                    .padding(16.dp)
                    .testTag("btn_screen_lock_login")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Screen Lock",
                        tint = BlueAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Login via Screen Lock (Device PIN/Pattern)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option 4: Fingerprint
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(14.dp))
                    .clickable {
                        onTriggerBiometric {
                            val user = repository.getAnyRegisteredUser()
                            if (user != null) {
                                repository.loginUser(user)
                            }
                            onSuccessLogin()
                        }
                    }
                    .padding(16.dp)
                    .testTag("btn_fingerprint_recovery")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint",
                        tint = BlueAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Use Fingerprint - Biometric Unlock",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
