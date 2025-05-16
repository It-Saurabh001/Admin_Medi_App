package com.saurabh.mediadminapp.network

import com.saurabh.mediadminapp.network.response.DeleteUserResponse
import com.saurabh.mediadminapp.network.response.GetAllUserResponse
import com.saurabh.mediadminapp.network.response.IsApproveUserResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ApiServices {
    // need to define end points
//    @FormUrlEncoded
    @GET("getAllUsers")
    suspend fun getAllUser(): Response<GetAllUserResponse>

    @FormUrlEncoded
    @PATCH("approveUser")
    suspend fun isApprovedUser(
        @Field("user_id") userId: String,
        @Field("isApproved") isApproved: Boolean
    ): Response<IsApproveUserResponse>

    @FormUrlEncoded
    @POST("deleteUser")
    suspend fun deleteUser(
        @Field("user_id") userId: String
    ): Response<DeleteUserResponse>
}



