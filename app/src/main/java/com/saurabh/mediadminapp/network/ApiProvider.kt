package com.saurabh.mediadminapp.network

import com.saurabh.mediadminapp.BuildConfig
import android.content.Context
import android.util.Log
import com.saurabh.mediadminapp.data.v1.core.AppEvents
import com.saurabh.mediadminapp.data.v1.core.V1Gson
import com.saurabh.mediadminapp.data.v1.network.ErrorEventInterceptor
import com.saurabh.mediadminapp.data.v1.network.RedactingLogger
import com.saurabh.mediadminapp.data.v1.network.V1AuthOkHttp
import com.saurabh.mediadminapp.data.v1.network.V1AuthRetrofit
import com.saurabh.mediadminapp.data.v1.network.V1OkHttp
import com.saurabh.mediadminapp.data.v1.network.V1RefreshApi
import com.saurabh.mediadminapp.data.v1.network.V1Retrofit
import com.saurabh.mediadminapp.data.v1.network.VersionInterceptor
import dagger.Module
import dagger.Lazy
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
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

    /** BuildConfig base + "api/v1/" (single trailing slash). */
    val V1_BASE_URL: String get() = BASE_URL1.trimEnd('/') + "/api/v1/"

    // ── TokenManager ──────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        Log.d("PERF_TRACE", "Hilt provideTokenManager [Thread: ${Thread.currentThread().name}]")
        return TokenManager.getInstance(context)
    }

    // Provide Custom Logging Interceptor
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): LoggingInterceptor {
        return LoggingInterceptor()
    }

    // ── Logging interceptor (tokens / otp / passwords / Authorization are redacted) ──

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(RedactingLogger { Log.d("OkHttp", it) }).apply {
            redactHeader("Authorization")
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
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
    fun provideAuthRetrofit(loggingInterceptor: HttpLoggingInterceptor,customLoggingInterceptor: LoggingInterceptor): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(customLoggingInterceptor)
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

    // ── TokenAuthenticator — legacy refresh via the isolated auth service, v1 via V1RefreshApi (lazy) ──

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenManager: TokenManager,
        @AuthApiService authApiServiceProvider: Lazy<ApiServices>,
        v1RefreshApi: Lazy<V1RefreshApi>,
        appEvents: AppEvents
    ): TokenAuthenticator {
        return TokenAuthenticator(tokenManager, authApiServiceProvider, { v1RefreshApi.get() }, appEvents)
    }

    // ── Main (authenticated) OkHttpClient ─────────────────────────────────────

    @Provides
    @Singleton
    fun provideHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        customLoggingInterceptor: LoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        appEvents: AppEvents
    ): OkHttpClient {
        Log.d("PERF_TRACE", "Hilt provideHttpClient [Thread: ${Thread.currentThread().name}]")
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(customLoggingInterceptor)
            .addInterceptor(VersionInterceptor(appEvents))      // v1 paths only: X-App-Version + 426
            .addInterceptor(authInterceptor)    // injects Bearer token on outgoing requests
            .addInterceptor(ErrorEventInterceptor(appEvents))   // v1 paths only: blocked / invalid / rate-limited events
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

    // ── v1 (derived from the existing main client: same timeouts, logging, interceptors) ──

    @Provides
    @Singleton
    @V1OkHttp
    fun provideV1OkHttp(client: OkHttpClient): OkHttpClient = client.newBuilder().build()

    /** Same client WITHOUT the authenticator (used for auth/refresh so it can never recurse). */
    @Provides
    @Singleton
    @V1AuthOkHttp
    fun provideV1AuthOkHttp(client: OkHttpClient): OkHttpClient =
        client.newBuilder().authenticator(Authenticator.NONE).build()

    @Provides
    @Singleton
    @V1Retrofit
    fun provideV1Retrofit(@V1OkHttp client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(V1_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(V1Gson.instance))
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
}
