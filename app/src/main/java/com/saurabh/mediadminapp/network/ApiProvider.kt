package com.saurabh.mediadminapp.network

import com.saurabh.mediadminapp.BuildConfig
import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Lazy
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


// ── Qualifiers ────────────────────────────────────────────────────────────────


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

/**
 * ApiServices instance backed by [AuthRetrofit] — safe to inject into
 * TokenAuthenticator without creating a recursive loop.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthApiService

/** The primary authenticated Retrofit instance. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainRetrofit

/** The primary authenticated ApiServices instance. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainApiService

@Module
@InstallIn(SingletonComponent::class)
object ApiProvider {
    val BASE_URL1 = BuildConfig.WirelessPhysicalDevice

    // ── TokenManager ──────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        Log.d("PERF_TRACE", "Hilt provideTokenManager [Thread: ${Thread.currentThread().name}]")
        return TokenManager.getInstance(context)
    }

    // ── Logging interceptor ───────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // ── Auth (unauthenticated) client — used ONLY for login + token refresh ──
    //
    // Why a separate client?
    // OkHttp calls Authenticator.authenticate() on a response that came from the
    // same OkHttpClient instance.  If the refresh request were sent through that
    // same client, a 401 on the refresh endpoint would re-enter authenticate(),
    // which would call refresh again, ad infinitum — a classic deadlock/loop.
    // By isolating refresh on its own bare OkHttpClient (no Authenticator, no
    // AuthInterceptor), that code path can never recurse.
    // ─────────────────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(loggingInterceptor: HttpLoggingInterceptor): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            // Intentionally NO authenticator and NO auth header interceptor.
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL1)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @AuthApiService
    fun provideAuthApiService(@AuthRetrofit retrofit: Retrofit): ApiServices {
        return retrofit.create(ApiServices::class.java)
    }

    // ── Auth header interceptor ───────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    // ── TokenAuthenticator — injected with the isolated auth service ──────────

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenManager: TokenManager,
        @AuthApiService authApiServiceProvider: Lazy<ApiServices>
    ): TokenAuthenticator {
        return TokenAuthenticator(tokenManager, authApiServiceProvider)
    }

    // ── Main (authenticated) OkHttpClient ─────────────────────────────────────

    @Provides
    @Singleton
    fun provideHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        Log.d("PERF_TRACE", "Hilt provideHttpClient [Thread: ${Thread.currentThread().name}]")
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)    // injects Bearer token on outgoing requests
            .authenticator(tokenAuthenticator)  // retries with fresh token on 401 responses
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    // ── Main Retrofit ─────────────────────────────────────────────────────────

    @Provides
    @Singleton
    @MainRetrofit
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        Log.d("PERF_TRACE", "Hilt provideRetrofit [Thread: ${Thread.currentThread().name}]")
        return Retrofit.Builder()
            .baseUrl(BASE_URL1)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ── Main ApiService ───────────────────────────────────────────────────────

    @Provides
    @Singleton
    @MainApiService
    fun provideApiServices(@MainRetrofit retrofit: Retrofit): ApiServices {
        return retrofit.create(ApiServices::class.java)
    }
}
