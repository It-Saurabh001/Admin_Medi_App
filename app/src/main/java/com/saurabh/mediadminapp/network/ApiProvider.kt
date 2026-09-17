package com.saurabh.mediadminapp.network

import android.content.Context
import android.util.Log
import com.google.gson.annotations.SerializedName
import com.saurabh.mediadminapp.utils.BASE_URL1
import com.saurabh.mediadminapp.utils.BASE_URL
import dagger.Module
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



@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TempRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainApiService

@Module
@InstallIn(SingletonComponent::class)
object ApiProvider {

    // Provide TokenManager
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        Log.d("PERF_TRACE", "Hilt provideTokenManager [Thread: ${Thread.currentThread().name}]")
        return TokenManager.getInstance(context)
    }

    // Provide Logging Interceptor
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // Temporary Retrofit for Token Refresh (without auth to avoid circular dependency)
    @Provides
    @Singleton
    @TempRetrofit
    fun provideTempRetrofit(
        loggingInterceptor: HttpLoggingInterceptor
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL1)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Temporary ApiService for Token Refresh
    @Provides
    @Singleton
    fun provideTempApiService(@TempRetrofit retrofit: Retrofit): ApiServices {
        return retrofit.create(ApiServices::class.java)
    }

    // Provide AuthInterceptor
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    // Provide TokenAuthenticator
    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenManager: TokenManager,
        tempApiService: ApiServices
    ): TokenAuthenticator {
        return TokenAuthenticator(tokenManager, tempApiService)
    }

    // Main OkHttpClient with Auth
    @Provides
    @Singleton
    fun provideHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        Log.d("PERF_TRACE", "Hilt provideHttpClient (First OkHttpClient access) [Thread: ${Thread.currentThread().name}]")
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Main Retrofit with Auth
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

    // Main ApiService
    @Provides
    @Singleton
    @MainApiService
    fun provideApiServices(@MainRetrofit retrofit: Retrofit): ApiServices {
        return retrofit.create(ApiServices::class.java)
    }
}


data class RefreshTokenResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("accessToken")
    val accessToken: String
)

