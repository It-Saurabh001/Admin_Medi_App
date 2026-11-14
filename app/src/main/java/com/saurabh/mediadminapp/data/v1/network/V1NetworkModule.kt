package com.saurabh.mediadminapp.data.v1.network

import com.saurabh.mediadminapp.BuildConfig
import com.saurabh.mediadminapp.data.v1.core.AppEvents
import com.saurabh.mediadminapp.data.v1.core.V1Gson
import com.saurabh.mediadminapp.network.TokenManager
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object V1NetworkModule {

    /** Existing BuildConfig base + "api/v1/" (single trailing slash). */
    val V1_BASE_URL: String get() = BuildConfig.WirelessPhysicalDevice.trimEnd('/') + "/api/v1/"

    @Provides
    @Singleton
    fun provideV1TokenStore(tokenManager: TokenManager): V1TokenStore = tokenManager

    private fun logging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor(RedactingLogger()).apply {
            redactHeader("Authorization")
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

    private fun OkHttpClient.Builder.timeouts() = apply {
        connectTimeout(90, TimeUnit.SECONDS)
        readTimeout(90, TimeUnit.SECONDS)
        writeTimeout(90, TimeUnit.SECONDS)
    }

    @Provides
    @Singleton
    @V1AuthOkHttp
    fun provideV1AuthOkHttp(events: AppEvents): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(VersionInterceptor(events))
        .addInterceptor(ErrorEventInterceptor(events))
        .addInterceptor(logging())
        .timeouts()
        .build()

    @Provides
    @Singleton
    @V1AuthRetrofit
    fun provideV1AuthRetrofit(@V1AuthOkHttp client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(V1_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(V1Gson.instance))
        .build()

    @Provides
    @Singleton
    fun provideV1RefreshApi(@V1AuthRetrofit retrofit: Retrofit): V1RefreshApi =
        retrofit.create(V1RefreshApi::class.java)

    @Provides
    @Singleton
    fun provideV1TokenAuthenticator(
        store: V1TokenStore,
        refreshApi: Lazy<V1RefreshApi>,
        events: AppEvents
    ): V1TokenAuthenticator = V1TokenAuthenticator(store, { refreshApi.get() }, events)

    @Provides
    @Singleton
    @V1OkHttp
    fun provideV1OkHttp(
        store: V1TokenStore,
        events: AppEvents,
        authenticator: V1TokenAuthenticator
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(VersionInterceptor(events))
        .addInterceptor(V1AuthInterceptor(store))
        .addInterceptor(ErrorEventInterceptor(events))
        .addInterceptor(logging())
        .authenticator(authenticator)
        .timeouts()
        .build()

    @Provides
    @Singleton
    @V1Retrofit
    fun provideV1Retrofit(@V1OkHttp client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(V1_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(V1Gson.instance))
        .build()
}
