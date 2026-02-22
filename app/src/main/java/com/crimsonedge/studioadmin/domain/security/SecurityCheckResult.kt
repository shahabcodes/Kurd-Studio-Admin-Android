package com.crimsonedge.studioadmin.domain.security

data class SecurityCheckResult(
    val isDeviceRooted: Boolean = false,
    val isEmulator: Boolean = false,
    val isDebuggerAttached: Boolean = false,
    val isSignatureTampered: Boolean = false,
    val failureReasons: List<String> = emptyList()
) {
    val isCompromised: Boolean
        get() = isDeviceRooted || isEmulator || isDebuggerAttached || isSignatureTampered
}
