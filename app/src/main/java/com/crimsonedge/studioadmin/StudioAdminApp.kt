package com.crimsonedge.studioadmin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import com.crimsonedge.studioadmin.notification.PendingNotification
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class StudioAdminApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupCrashHandler()
        setupOneSignal()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CONTACT_CHANNEL_ID,
            "Contact Submissions",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new contact form submissions"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun setupOneSignal() {
        createNotificationChannel()

        if (BuildConfig.DEBUG) {
            OneSignal.Debug.logLevel = LogLevel.VERBOSE
        }
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }

        OneSignal.Notifications.addClickListener(object : INotificationClickListener {
            override fun onClick(event: INotificationClickEvent) {
                val data = event.notification.additionalData
                if (data != null && data.optString("type") == "contact_submission") {
                    val submissionId = data.optInt("submission_id", 0)
                    if (submissionId > 0) {
                        PendingNotification.navigateToContact(submissionId)
                    }
                }
            }
        })
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception on thread ${thread.name}", throwable)
            // Delegate to the default handler so the system can show the crash dialog
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        private const val TAG = "StudioAdminApp"
        const val CONTACT_CHANNEL_ID = "contact_submissions"
    }
}
