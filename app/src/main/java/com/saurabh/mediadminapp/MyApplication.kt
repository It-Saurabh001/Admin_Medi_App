package com.saurabh.mediadminapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // FIX (Cause 7): Removed redundant Thread { TokenManager.getInstance(this) }.start()
        // Hilt already provides and initializes TokenManager as a @Singleton via ApiProvider,
        // so this manual warm-up was creating a raw OS thread for work that Hilt handles.
    }
}
