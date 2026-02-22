package com.crimsonedge.studioadmin.data.remote.interceptor

import com.crimsonedge.studioadmin.data.security.SecurityManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityCheckInterceptor @Inject constructor(
    private val securityManager: SecurityManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val result = securityManager.performChecks()

        if (result.isCompromised) {
            throw SecurityException(
                "Network request blocked: ${result.failureReasons.joinToString(", ")}"
            )
        }

        return chain.proceed(chain.request())
    }

    class SecurityException(message: String) : IOException(message)
}
