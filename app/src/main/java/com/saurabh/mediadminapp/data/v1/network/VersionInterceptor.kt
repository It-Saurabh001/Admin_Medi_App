package com.saurabh.mediadminapp.data.v1.network

import com.saurabh.mediadminapp.BuildConfig
import com.saurabh.mediadminapp.data.v1.core.AppEvent
import com.saurabh.mediadminapp.data.v1.core.AppEvents
import okhttp3.Interceptor
import okhttp3.Response

class VersionInterceptor(
    private val events: AppEvents,
    private val versionName: String = BuildConfig.VERSION_NAME
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().header("X-App-Version", versionName).build()
        val response = chain.proceed(request)
        if (response.code == 426) {
            events.emit(AppEvent.UpgradeRequired(response.header("X-Min-App-Version")))
        }
        return response
    }
}
