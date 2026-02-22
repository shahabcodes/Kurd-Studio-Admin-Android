package com.crimsonedge.studioadmin.notification

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object PendingNotification {
    private val _contactIdFlow = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val contactIdFlow = _contactIdFlow.asSharedFlow()

    fun navigateToContact(id: Int) {
        _contactIdFlow.tryEmit(id)
    }
}
