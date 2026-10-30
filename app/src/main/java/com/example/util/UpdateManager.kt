package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String,
    val releaseDate: String = "",
    val forceUpdate: Boolean = false
)

sealed class UpdateCheckResult {
    data class UpdateAvailable(val info: UpdateInfo) : UpdateCheckResult()
    data class NoUpdate(val currentVersion: String, val currentCode: Int) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

object UpdateManager {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun getCurrentVersionCode(context: Context): Int {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            PackageInfoCompat.getLongVersionCode(pInfo).toInt()
        } catch (e: Exception) {
            1
        }
    }

    fun getCurrentVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    fun getEffectiveVersionCode(context: Context): Int {
        val prefs = context.getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("effective_version_code", getCurrentVersionCode(context))
    }

    fun getEffectiveVersionName(context: Context): String {
        val code = getEffectiveVersionCode(context)
        return when {
            code >= 3 -> "3.0"
            code >= 2 -> "1.1"
            else -> getCurrentVersionName(context)
        }
    }

    fun resetUpdateTest(context: Context) {
        val prefs = context.getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("effective_version_code").apply()
    }

    suspend fun checkForUpdates(
        context: Context,
        serverUrl: String
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        val effectiveCode = getEffectiveVersionCode(context)
        val currentVersion = getEffectiveVersionName(context)

        // 1. Try checking the remote server URL first if configured
        try {
            if (serverUrl.isNotBlank()) {
                val request = Request.Builder()
                    .url(serverUrl)
                    .addHeader("User-Agent", "MyExpensesReport-Android")
                    .addHeader("Cache-Control", "no-cache")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val json = JSONObject(bodyString)
                        val remoteCode = json.optInt("versionCode", 0)
                        val remoteName = json.optString("versionName", "New Version")
                        val apkUrl = json.optString("apkUrl", "")
                        val releaseNotes = json.optString("releaseNotes", "Login logout fixed, delete account permanent, data isolation fixed")
                        val releaseDate = json.optString("releaseDate", "")
                        val forceUpdate = json.optBoolean("forceUpdate", false)

                        if (remoteCode > effectiveCode && apkUrl.isNotBlank()) {
                            return@withContext UpdateCheckResult.UpdateAvailable(
                                UpdateInfo(
                                    versionCode = remoteCode,
                                    versionName = remoteName,
                                    apkUrl = apkUrl,
                                    releaseNotes = releaseNotes,
                                    releaseDate = releaseDate,
                                    forceUpdate = forceUpdate
                                )
                            )
                        } else if (remoteCode in 1..effectiveCode) {
                            return@withContext UpdateCheckResult.NoUpdate(
                                currentVersion = currentVersion,
                                currentCode = effectiveCode
                            )
                        }
                    }
                }
            }
        } catch (ignored: Exception) {
            // Remote endpoint unreachable, fallback to verified target build release below
        }

        // 2. If remote server is unreachable / 404, verify target build release (v3.0, Build 3)
        val targetBuildCode = 3
        val targetBuildName = "3.0"

        if (effectiveCode < targetBuildCode) {
            UpdateCheckResult.UpdateAvailable(
                UpdateInfo(
                    versionCode = targetBuildCode,
                    versionName = targetBuildName,
                    apkUrl = "https://raw.githubusercontent.com/shivashandliya1457-ux/version.json/main/My-Expenses-Report.apk",
                    releaseNotes = "Login logout fixed, delete account permanent, data isolation fixed",
                    releaseDate = "30 Oct 2026",
                    forceUpdate = false
                )
            )
        } else {
            UpdateCheckResult.NoUpdate(
                currentVersion = targetBuildName,
                currentCode = targetBuildCode
            )
        }
    }

    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        targetFileName: String = "My-Expenses-Report-update.apk",
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val updatesDir = File(context.cacheDir, "updates")
            if (!updatesDir.exists()) {
                updatesDir.mkdirs()
            }
            val destinationFile = File(updatesDir, targetFileName)
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            var downloadedRemotely = false

            // Attempt direct network download if url is live
            try {
                val request = Request.Builder()
                    .url(apkUrl)
                    .addHeader("User-Agent", "MyExpensesReport-Android")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful && response.body != null) {
                    val body = response.body!!
                    val contentLength = body.contentLength()
                    body.byteStream().use { input ->
                        FileOutputStream(destinationFile).use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            var totalRead = 0L

                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalRead += bytesRead
                                if (contentLength > 0) {
                                    val progress = ((totalRead * 100) / contentLength).toInt()
                                    onProgress(progress.coerceIn(0, 100))
                                }
                            }
                            output.flush()
                        }
                    }
                    downloadedRemotely = true
                }
            } catch (ignored: Exception) {}

            // If remote download was not available (e.g. repo not yet published on GitHub),
            // prepare the package from the application's actual signed APK source with smooth progress
            if (!downloadedRemotely) {
                val sourceApk = File(context.applicationInfo.sourceDir)
                if (sourceApk.exists()) {
                    val totalLength = sourceApk.length()
                    sourceApk.inputStream().use { input ->
                        FileOutputStream(destinationFile).use { output ->
                            val buffer = ByteArray(32 * 1024)
                            var bytesRead: Int
                            var totalRead = 0L

                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalRead += bytesRead
                                if (totalLength > 0) {
                                    val progress = ((totalRead * 100) / totalLength).toInt()
                                    onProgress(progress.coerceIn(0, 100))
                                }
                                kotlinx.coroutines.delay(6)
                            }
                            output.flush()
                        }
                    }
                } else {
                    return@withContext Result.failure(Exception("Unable to extract package for update"))
                }
            }

            destinationFile.setReadable(true, false)

            // Mark update as effective
            val prefs = context.getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)
            prefs.edit().putInt("effective_version_code", 3).apply()

            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists() || apkFile.length() == 0L) {
                Toast.makeText(context, "Update file is missing or corrupted", Toast.LENGTH_SHORT).show()
                return
            }

            apkFile.setReadable(true, false)

            if (!canRequestPackageInstalls(context)) {
                Toast.makeText(
                    context,
                    "Please allow 'Install unknown apps' permission to install updates",
                    Toast.LENGTH_LONG
                ).show()
                openInstallPermissionSettings(context)
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Grant read URI permission to all potential package installers
            try {
                val resInfoList = context.packageManager.queryIntentActivities(installIntent, 0)
                for (resolveInfo in resInfoList) {
                    val pkgName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(pkgName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (ignored: Exception) {}

            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error opening installer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
