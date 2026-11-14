package com.saurabh.mediadminapp.data.v1.api

import com.saurabh.mediadminapp.data.v1.core.ApiEnvelope
import com.saurabh.mediadminapp.data.v1.core.PagedEnvelope
import com.saurabh.mediadminapp.data.v1.dto.Admin
import com.saurabh.mediadminapp.data.v1.dto.AdminCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.AdminUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.Device
import com.saurabh.mediadminapp.data.v1.dto.DeviceRegisterRequest
import com.saurabh.mediadminapp.data.v1.dto.Order
import com.saurabh.mediadminapp.data.v1.dto.User
import com.saurabh.mediadminapp.data.v1.dto.UserUpdateRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/** `page` = PageRequest.toQueryMap() (limit, offset, sort, order). Multipart variants: use textParts(...) + optional image. */
interface UsersApi {
    @GET("users")
    suspend fun listUsers(
        @Query("q") q: String? = null,
        @Query("is_approved") isApproved: Boolean? = null,
        @Query("is_blocked") isBlocked: Boolean? = null,
        @Query("include_deleted") includeDeleted: Boolean? = null,
        @Query("updated_since") updatedSince: String? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<User>>

    @GET("users/{user_id}")
    suspend fun getUser(@Path("user_id") userId: String): Response<ApiEnvelope<User>>

    @PATCH("users/{user_id}")
    suspend fun updateUser(
        @Path("user_id") userId: String,
        @Body body: UserUpdateRequest
    ): Response<ApiEnvelope<User>>

    @Multipart
    @PATCH("users/{user_id}")
    suspend fun updateUserMultipart(
        @Path("user_id") userId: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part? = null
    ): Response<ApiEnvelope<User>>

    /** Soft delete. */
    @DELETE("users/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: String): Response<ApiEnvelope<User>>

    @POST("users/{user_id}/approve")
    suspend fun approveUser(@Path("user_id") userId: String): Response<ApiEnvelope<User>>

    @POST("users/{user_id}/block")
    suspend fun blockUser(@Path("user_id") userId: String): Response<ApiEnvelope<User>>

    @POST("users/{user_id}/unblock")
    suspend fun unblockUser(@Path("user_id") userId: String): Response<ApiEnvelope<User>>

    @GET("users/{user_id}/orders")
    suspend fun listUserOrders(
        @Path("user_id") userId: String,
        @Query("status") status: String? = null,
        @Query("product_id") productId: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("updated_since") updatedSince: String? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<Order>>
}

interface AdminsApi {
    @GET("admins")
    suspend fun listAdmins(
        @Query("q") q: String? = null,
        @Query("role") role: String? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<Admin>>

    @POST("admins")
    suspend fun createAdmin(@Body body: AdminCreateRequest): Response<ApiEnvelope<Admin>>

    @Multipart
    @POST("admins")
    suspend fun createAdminMultipart(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part? = null
    ): Response<ApiEnvelope<Admin>>

    @GET("admins/{admin_id}")
    suspend fun getAdmin(@Path("admin_id") adminId: String): Response<ApiEnvelope<Admin>>

    @PATCH("admins/{admin_id}")
    suspend fun updateAdmin(
        @Path("admin_id") adminId: String,
        @Body body: AdminUpdateRequest
    ): Response<ApiEnvelope<Admin>>

    @Multipart
    @PATCH("admins/{admin_id}")
    suspend fun updateAdminMultipart(
        @Path("admin_id") adminId: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part? = null
    ): Response<ApiEnvelope<Admin>>

    /** Soft delete; last active admin -> 409 CONFLICT. */
    @DELETE("admins/{admin_id}")
    suspend fun deleteAdmin(@Path("admin_id") adminId: String): Response<ApiEnvelope<Admin>>
}

interface DevicesApi {
    @POST("devices")
    suspend fun registerDevice(@Body body: DeviceRegisterRequest): Response<ApiEnvelope<Device>>

    @DELETE("devices/{device_id}")
    suspend fun unregisterDevice(@Path("device_id") deviceId: String): Response<ApiEnvelope<Device>>
}
