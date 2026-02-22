package com.crimsonedge.studioadmin.data.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import com.crimsonedge.studioadmin.BuildConfig
import com.crimsonedge.studioadmin.domain.security.SecurityCheckResult
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Volatile
    private var cachedResult: SecurityCheckResult? = null

    fun performChecks(): SecurityCheckResult {
        cachedResult?.let { return it }

        val reasons = mutableListOf<String>()
        val isDebug = BuildConfig.DEBUG

        // Root detection — always runs
        val isRooted = checkRoot()
        if (isRooted) reasons.add("Device is rooted")

        // Emulator detection — release only
        val isEmulator = if (!isDebug) checkEmulator() else false
        if (isEmulator) reasons.add("Running on an emulator")

        // Debugger detection — release only
        val isDebuggerAttached = if (!isDebug) checkDebugger() else false
        if (isDebuggerAttached) reasons.add("Debugger is attached")

        // Signature verification — release only
        val isSignatureTampered = if (!isDebug) checkSignatureTampered() else false
        if (isSignatureTampered) reasons.add("App signature has been tampered with")

        val result = SecurityCheckResult(
            isDeviceRooted = isRooted,
            isEmulator = isEmulator,
            isDebuggerAttached = isDebuggerAttached,
            isSignatureTampered = isSignatureTampered,
            failureReasons = reasons
        )

        cachedResult = result
        return result
    }

    fun clearCache() {
        cachedResult = null
    }

    private fun checkRoot(): Boolean {
        return try {
            RootBeer(context).isRooted
        } catch (_: Exception) {
            false
        }
    }

    private fun checkEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_gphone")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"))
    }

    private fun checkDebugger(): Boolean {
        if (Debug.isDebuggerConnected()) return true
        val flags = context.applicationInfo.flags
        if (flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) return true
        return false
    }

    private fun checkSignatureTampered(): Boolean {
        return try {
            val expectedHash = BuildConfig.EXPECTED_SIGNATURE_HASH
            if (expectedHash.isBlank()) return false

            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signingInfo = packageInfo.signingInfo ?: return true
            val signatures = if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }

            if (signatures.isNullOrEmpty()) return true

            val md = MessageDigest.getInstance("SHA-256")
            val currentHash = signatures[0].toByteArray().let { bytes ->
                md.digest(bytes).joinToString("") { "%02x".format(it) }
            }

            currentHash != expectedHash
        } catch (_: Exception) {
            false
        }
    }
}
