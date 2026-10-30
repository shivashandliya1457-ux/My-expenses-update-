package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    fun hasFingerprintSensor(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }

    fun canAuthenticate(
        context: Context,
        authenticators: Int = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    ): Pair<Boolean, String?> {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Pair(true, null)
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Pair(false, "No biometric hardware found on this device")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Pair(false, "Biometric sensor is currently unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Pair(false, "No biometric or screen lock enrolled in device settings")
            else -> Pair(false, "Authentication not supported")
        }
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Fingerprint Authentication",
        subtitle: String = "Touch the fingerprint sensor",
        description: String = "Verify your identity to continue",
        authenticators: Int = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onUserDismissed: (() -> Unit)? = null
    ) {
        val (canAuth, errorMsg) = canAuthenticate(activity, authenticators)
        if (!canAuth && errorMsg != null) {
            onError(errorMsg)
            onUserDismissed?.invoke()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                ) {
                    onUserDismissed?.invoke()
                } else {
                    onError(errString.toString())
                    onUserDismissed?.invoke()
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Fingerprint not recognized. Please try again.")
            }
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)

        val hasDeviceCredential = (authenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL) != 0
        if (hasDeviceCredential) {
            // When DEVICE_CREDENTIAL is used, setNegativeButtonText MUST NOT be called
            promptInfoBuilder.setAllowedAuthenticators(authenticators)
        } else {
            promptInfoBuilder.setNegativeButtonText("Cancel")
            promptInfoBuilder.setAllowedAuthenticators(authenticators)
        }

        try {
            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfoBuilder.build())
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Biometric sensor error")
            onUserDismissed?.invoke()
        }
    }
}
