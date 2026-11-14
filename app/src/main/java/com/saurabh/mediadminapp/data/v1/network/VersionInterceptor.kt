package com.saurabh.mediadminapp.data.v1.network

import com.saurabh.mediadminapp.BuildConfig
import com.saurabh.mediadminapp.data.v1.core.AppEvent
import com.saurabh.mediadminapp.data.v1.core.AppEvents
import com.saurabh.mediadminapp.network.AuthInterceptor
import okhttp3.Interceptor
import okhttp3.Response

/** v1 paths only: adds X-App-Version; 426 -> UpgradeRequired. */
class VersionInterceptor(
    private val events: AppEvents,
    private val versionName: String = BuildConfig.VERSION_NAME
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (!AuthInterceptor.isV1Path(original.url.encodedPath)) return chain.proceed(original)
        val response = chain.proceed(original.newBuilder().header("X-App-Version", versionName).build())
        if (response.code == 426) {
            events.emit(AppEvent.UpgradeRequired(response.header("X-Min-App-Version")))
        }
        return response
    }
}
