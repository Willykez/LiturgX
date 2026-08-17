package com.willykez.liturgx

import android.app.Application
import com.willykez.liturgx.notifications.NotificationHelper

class LiturgXApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Channels must exist before the first notification is ever posted; cheapest place to
        // guarantee that is app start, and it's a no-op on every call after the first.
        NotificationHelper.ensureChannel(this)
    }
}
